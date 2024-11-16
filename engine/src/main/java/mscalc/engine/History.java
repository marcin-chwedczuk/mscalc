package mscalc.engine;

import mscalc.engine.commands.*;
import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack;
import mscalc.engine.ratpack.RatPack.AngleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mscalc.engine.Commands.*;
import static mscalc.engine.WinErrorCrossPlatform.E_BOUNDS;

// Helper class really a internal class to CCalcEngine, to accumulate each history line of text by collecting the
// operands, operator, unary operator etc. Since it is a separate entity, it can be unit tested on its own but does
// rely on CCalcEngine calling it in appropriate order.
public class History {
    // maximum depth you can get by precedence. It is just an array's size limit.
    private static final int MAXPRECDEPTH = 25;
    private static final int ASCII_0 = 48;

    private HistoryDisplay historyDisplay;
    private CalcDisplay calcDisplay;

    private int iCurLineHistStart; // index of the beginning of the current equation
    // a sort of state, set to the index before 2 after 2 in the expression 2 + 3 say. Useful for auto correct portion of history and for
    // attaching the unary op around the last operand
    private int lastOpStartIndex;    // index of the beginning of the last operand added to the history
    private int lastBinOpStartIndex; // index of the beginning of the last binary operator added to the history
    private final int[] operandIndices = new int[MAXPRECDEPTH];  // Stack of index of opnd's beginning for each '('. A parallel array to m_hnoParNum, but abstracted independently of that
    private int curOperandIndex; // Stack index for the above stack
    private boolean bLastOpndBrace; // iff the last opnd in history is already braced so we can avoid putting another one for unary operator
    private char decimalSymbol;

    private List<Pair<String, Integer>> spTokens;
    private List<IExpressionCommand> spCommands;

    public History(CalcDisplay calcDisplay, HistoryDisplay historyDisplay, char decimalSymbol) {
        this.calcDisplay = calcDisplay;
        this.historyDisplay = historyDisplay;
        this.decimalSymbol = decimalSymbol;
        this.iCurLineHistStart = -1;

        reinitHistory();
    }

    private static <T> void truncate(List<T> v, int index) {
        if (index >= v.size()) {
            throw new ErrorCodeException(E_BOUNDS);
        }

        while (v.size() > index)
            v.removeLast();
    }

    public void reinitHistory() {
        lastOpStartIndex = -1;
        lastBinOpStartIndex = -1;
        curOperandIndex = 0;
        bLastOpndBrace = false;

        if (spTokens != null) {
            spTokens.clear();
        }

        if (spCommands != null) {
            spCommands.clear();
        }
    }

    void addOpndToHistory(String numStr, Rational rat, boolean fRepetition) {
        int iCommandEnd = addCommand(getOperandCommandsFromString(numStr, rat));
        lastOpStartIndex = ichAddSzToEquationSz(numStr, iCommandEnd);

        if (fRepetition) {
            setExpressionDisplay();
        }
        bLastOpndBrace = false;
        lastBinOpStartIndex = -1;
    }

    void removeLastOpndFromHistory() {
        truncateEquationSzFromIch(lastOpStartIndex);
        setExpressionDisplay();
        lastOpStartIndex = -1;
        // This will not restore the m_lastBinOpStartIndex, as it isn't possible to remove that also later
    }

    void addBinOpToHistory(int nOpCode, boolean isIntegerMode) {
        addBinOpToHistory(nOpCode, isIntegerMode, true);
    }

    void addBinOpToHistory(int nOpCode, boolean isIntegerMode, boolean fNoRepetition) {
        int iCommandEnd = addCommand(new CBinaryCommand(nOpCode));
        lastBinOpStartIndex = ichAddSzToEquationSz(" ", -1);

        ichAddSzToEquationSz(CCalcEngine.OpCodeToBinaryString(nOpCode, isIntegerMode), iCommandEnd);
        ichAddSzToEquationSz(" ", -1);

        if (fNoRepetition) {
            setExpressionDisplay();
        }
        lastOpStartIndex = -1;
    }

    // This is expected to be called when a binary op in the last say 1+2+ is changing to another one say 1+2* (+ changed to *)
    // It needs to know by this change a Precedence inversion happened. i.e. previous op was lower or equal to its previous op, but the new
    // one isn't. (Eg. 1*2* to 1*2^). It can add explicit brackets to ensure the precedence is inverted. (Eg. (1*2) ^)
    void changeLastBinOp(int nOpCode, boolean fPrecInvToHigher, boolean isIntegerMode) {
        truncateEquationSzFromIch(lastBinOpStartIndex);
        if (fPrecInvToHigher) {
            enclosePrecInversionBrackets();
        }
        addBinOpToHistory(nOpCode, isIntegerMode);
    }

    void pushLastOpndStart(int ichOpndStart) {
        int ich = (ichOpndStart == -1) ? lastOpStartIndex : ichOpndStart;

        if (curOperandIndex < (int) (operandIndices.length)) {
            operandIndices[curOperandIndex++] = ich;
        }
    }

    void popLastOpndStart() {
        if (curOperandIndex > 0) {
            lastOpStartIndex = operandIndices[--curOperandIndex];
        }
    }

    void addOpenBraceToHistory() {
        addCommand(new CParentheses(IDC_OPENP));
        int ichOpndStart = ichAddSzToEquationSz(CCalcEngine.OpCodeToString(IDC_OPENP), -1);
        pushLastOpndStart(ichOpndStart);

        setExpressionDisplay();
        lastBinOpStartIndex = -1;
    }

    void addCloseBraceToHistory() {
        addCommand(new CParentheses(IDC_CLOSEP));
        ichAddSzToEquationSz(CCalcEngine.OpCodeToString(IDC_CLOSEP), -1);
        setExpressionDisplay();
        popLastOpndStart();

        lastBinOpStartIndex = -1;
        bLastOpndBrace = true;
    }

    void enclosePrecInversionBrackets() {
        // Top of the Opnd starts index or 0 is nothing is in top
        int ichStart = (curOperandIndex > 0) ? operandIndices[curOperandIndex - 1] : 0;

        insertSzInEquationSz(CCalcEngine.OpCodeToString(IDC_OPENP), -1, ichStart);
        ichAddSzToEquationSz(CCalcEngine.OpCodeToString(IDC_CLOSEP), -1);
    }

    boolean fOpndAddedToHistory() {
        return (-1 != lastOpStartIndex);
    }

    // AddUnaryOpToHistory
    //
    // This is does the postfix to prefix translation of the input and adds the text to the history. Eg. doing 2 + 4 (sqrt),
    // this routine will ensure the last sqrt call unary operator, actually goes back in history and wraps 4 in sqrt(4)
    //
    void addUnaryOpToHistory(int nOpCode, boolean fInv, AngleType angletype) {
        int iCommandEnd;
        // When successfully applying a unary op, there should be an opnd already
        // A very special case of % which is a funny post op unary op.
        if (IDC_PERCENT == nOpCode) {
            iCommandEnd = addCommand(new CUnaryCommand(nOpCode));
            ichAddSzToEquationSz(CCalcEngine.OpCodeToString(nOpCode), iCommandEnd);
        } else // all the other unary ops
        {
            IOperatorCommand spExpressionCommand;
            if (IDC_SIGN == nOpCode) {
                spExpressionCommand = new CUnaryCommand(nOpCode);
            } else {
                CalculationManagerCommand angleOpCode;
                if (angletype == AngleType.Degrees) {
                    angleOpCode = CalculationManagerCommand.CommandDEG;
                } else if (angletype == AngleType.Radians) {
                    angleOpCode = CalculationManagerCommand.CommandRAD;
                } else // (angletype == AngleType::Gradians)
                {
                    angleOpCode = CalculationManagerCommand.CommandGRAD;
                }

                int command = nOpCode;
                switch (nOpCode) {
                    case IDC_SIN:
                        command = fInv ? CalculationManagerCommand.CommandASIN.toCommandInt() : IDC_SIN;
                        spExpressionCommand = new CUnaryCommand(angleOpCode.toCommandInt(), command);
                        break;
                    case IDC_COS:
                        command = fInv ? CalculationManagerCommand.CommandACOS.toCommandInt() : IDC_COS;
                        spExpressionCommand = new CUnaryCommand((angleOpCode.toCommandInt()), command);
                        break;
                    case IDC_TAN:
                        command = fInv ? CalculationManagerCommand.CommandATAN.toCommandInt() : IDC_TAN;
                        spExpressionCommand = new CUnaryCommand((angleOpCode.toCommandInt()), command);
                        break;
                    case IDC_SINH:
                        command = fInv ? CalculationManagerCommand.CommandASINH.toCommandInt() : IDC_SINH;
                        spExpressionCommand = new CUnaryCommand(command);
                        break;
                    case IDC_COSH:
                        command = fInv ? CalculationManagerCommand.CommandACOSH.toCommandInt() : IDC_COSH;
                        spExpressionCommand = new CUnaryCommand(command);
                        break;
                    case IDC_TANH:
                        command = fInv ? CalculationManagerCommand.CommandATANH.toCommandInt() : IDC_TANH;
                        spExpressionCommand = new CUnaryCommand(command);
                        break;
                    case IDC_SEC:
                        command = fInv ? CalculationManagerCommand.CommandASEC.toCommandInt() : IDC_SEC;
                        spExpressionCommand = new CUnaryCommand(angleOpCode.toCommandInt(), command);
                        break;
                    case IDC_CSC:
                        command = fInv ? CalculationManagerCommand.CommandACSC.toCommandInt() : IDC_CSC;
                        spExpressionCommand = new CUnaryCommand(angleOpCode.toCommandInt(), command);
                        break;
                    case IDC_COT:
                        command = fInv ? CalculationManagerCommand.CommandACOT.toCommandInt() : IDC_COT;
                        spExpressionCommand = new CUnaryCommand(angleOpCode.toCommandInt(), command);
                        break;
                    case IDC_SECH:
                        command = fInv ? CalculationManagerCommand.CommandASECH.toCommandInt() : IDC_SECH;
                        spExpressionCommand = new CUnaryCommand(command);
                        break;
                    case IDC_CSCH:
                        command = fInv ? CalculationManagerCommand.CommandACSCH.toCommandInt() : IDC_CSCH;
                        spExpressionCommand = new CUnaryCommand(command);
                        break;
                    case IDC_COTH:
                        command = fInv ? CalculationManagerCommand.CommandACOTH.toCommandInt() : IDC_COTH;
                        spExpressionCommand = new CUnaryCommand(command);
                        break;
                    case IDC_LN:
                        command = fInv ? CalculationManagerCommand.CommandPOWE.toCommandInt() : IDC_LN;
                        spExpressionCommand = new CUnaryCommand(command);
                        break;
                    default:
                        spExpressionCommand = new CUnaryCommand(nOpCode);
                }
            }

            iCommandEnd = addCommand(spExpressionCommand);

            StringBuilder operandStr = new StringBuilder();
            operandStr.append(CCalcEngine.OpCodeToUnaryString(nOpCode, fInv, angletype));

            if (!bLastOpndBrace) // The opnd is already covered in braces. No need for additional braces around it
            {
                operandStr.append(CCalcEngine.OpCodeToString(IDC_OPENP));
            }
            insertSzInEquationSz(operandStr.toString(), iCommandEnd, lastOpStartIndex);

            if (!bLastOpndBrace) {
                ichAddSzToEquationSz(CCalcEngine.OpCodeToString(IDC_CLOSEP), -1);
            }
        }

        setExpressionDisplay();
        bLastOpndBrace = false;
        // m_lastOpStartIndex remains the same as last opnd is just replaced by unaryop(lastopnd)
        lastBinOpStartIndex = -1;
    }

    // Called after = with the result of the equation
    // Responsible for clearing the top line of current running history display, as well as adding yet another element to
    // history of equations
    void completeHistoryLine(String numStr) {
        if (null != historyDisplay) {
            int addedItemIndex = historyDisplay.addToHistory(spTokens, spCommands, numStr);
            calcDisplay.onHistoryItemAdded(addedItemIndex);
        }

        spTokens = null;
        spCommands = null;
        iCurLineHistStart = -1; // It will get recomputed at the first Opnd
        reinitHistory();
    }

    void completeEquation(String numStr) {
        // Add only '=' token and not add EQU command, because
        // EQU command breaks loading from history (it duplicate history entries).
        ichAddSzToEquationSz(CCalcEngine.OpCodeToString(IDC_EQU), -1);

        setExpressionDisplay();
        completeHistoryLine(numStr);
    }

    void clearHistoryLine(String errStr) {
        if (errStr.isEmpty()) // in case of error let the display stay as it is
        {
            if (null != calcDisplay) {
                calcDisplay.setExpressionDisplay(
                        new ArrayList<Pair<String, Integer>>(),
                        new ArrayList<IExpressionCommand>());
            }
            iCurLineHistStart = -1; // It will get recomputed at the first Opnd
            reinitHistory();
        }
    }

    // Adds the given string psz to the globally maintained current equation string at the end.
    //  Also returns the 0 based index in the string just added. Can throw out of memory error
    int ichAddSzToEquationSz(String str, int icommandIndex) {
        if (spTokens == null) {
            spTokens = new ArrayList<Pair<String, Integer>>();
        }

        spTokens.add(Pair.entry(str, icommandIndex));
        return (spTokens.size() - 1);
    }

    // Inserts a given string into the global m_pszEquation at the given index ich taking care of reallocations etc.
    void insertSzInEquationSz(String str, int icommandIndex, int ich) {
        spTokens.add(ich, Pair.entry(str, icommandIndex));
    }

    // Chops off the current equation string from the given index
    void truncateEquationSzFromIch(int ich) {
        // Truncate commands
        int minIdx = -1;
        int nTokens = spTokens.size();

        for (int i = ich; i < nTokens; i++) {
            var currentPair = spTokens.get(i);
            int curTokenId = currentPair.getValue();
            if (curTokenId != -1) {
                if ((minIdx != -1) || (curTokenId < minIdx)) {
                    minIdx = curTokenId;
                    truncate(spCommands, minIdx);
                }
            }
        }

        truncate(spTokens, ich);
    }

    // Adds the m_pszEquation into the running history text
    void setExpressionDisplay() {
        if (null != calcDisplay) {
            calcDisplay.setExpressionDisplay(spTokens, spCommands);
        }
    }

    int addCommand(IExpressionCommand spCommand) {
        if (this.spCommands == null) {
            this.spCommands = new ArrayList<>();
        }

        this.spCommands.add(spCommand);
        return (spCommands.size() - 1);
    }

    // To Update the operands in the Expression according to the current Radix
    void updateHistoryExpression(uint radix, int precision) {
        if (spTokens == null) {
            return;
        }

        for (var token : spTokens) {
            int commandPosition = token.getValue();
            if (commandPosition != -1) {
                IExpressionCommand expCommand = spCommands.get(commandPosition);

                if (expCommand != null && CommandType.OperandCommand == expCommand.getCommandType()) {
                    COpndCommand opndCommand = (COpndCommand) expCommand;
                    if (opndCommand != null) {
                        token.setKey(opndCommand.getString(radix, precision));
                        opndCommand.setCommands(getOperandCommandsFromString(token.getKey()));
                    }
                }
            }
        }

        setExpressionDisplay();
    }

    void setDecimalSymbol(char decimalSymbol) {
        this.decimalSymbol = decimalSymbol;
    }

    // Update the commands corresponding to the passed string Number
    List<Integer> getOperandCommandsFromString(String numStr) {
        List<Integer> commands = new ArrayList<>();
        // Check for negate
        boolean fNegative = (numStr.charAt(0) == '-');

        for (int i = (fNegative ? 1 : 0); i < numStr.length(); i++) {
            if (numStr.charAt(i) == decimalSymbol) {
                commands.add(IDC_PNT);
            } else if (numStr.charAt(i) == 'e') {
                commands.add(IDC_EXP);
            } else if (numStr.charAt(i) == '-') {
                commands.add(IDC_SIGN);
            } else if (numStr.charAt(i) == '+') {
                // Ignore.
            }
            // Number
            else {
                int num = numStr.charAt(i) - ASCII_0;
                num += IDC_0;
                commands.add(num);
            }
        }

        // If the number is negative, append a sign command at the end.
        if (fNegative) {
            commands.add(IDC_SIGN);
        }
        return commands;
    }

    COpndCommand getOperandCommandsFromString(String numStr, Rational rat)
    {
        List<Integer> commands = new ArrayList<>();
        // Check for negate
        boolean fNegative = (numStr.charAt(0) == '-');
        boolean fSciFmt = false;
        boolean fDecimal = false;

        for (int i = (fNegative ? 1 : 0); i < numStr.length(); i++)
        {
            if (numStr.charAt(i) == decimalSymbol)
            {
                commands.add(IDC_PNT);
                if (!fSciFmt)
                {
                    fDecimal = true;
                }
            }
            else if (numStr.charAt(i) == 'e')
            {
                commands.add(IDC_EXP);
                fSciFmt = true;
            }
        else if (numStr.charAt(i) == '-')
            {
                commands.add(IDC_SIGN);
            }
        else if (numStr.charAt(i) == '+')
            {
                // Ignore.
            }
            // Number
        else
            {
                int num = (numStr.charAt(i)) - ASCII_0;
                num += IDC_0;
                commands.add(num);
            }
        }

        var operandCommand = new COpndCommand(commands, fNegative, fDecimal, fSciFmt);
        operandCommand.initialize(rat);
        return operandCommand;
    }

    List<IExpressionCommand> getCommands()
    {
        return new ArrayList<>(this.spCommands);
    }
}
