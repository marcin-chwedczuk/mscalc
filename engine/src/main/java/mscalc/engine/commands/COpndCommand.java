package mscalc.engine.commands;

import mscalc.engine.Rational;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack;
import mscalc.engine.ratpack.RatPack.NumberFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static mscalc.engine.Commands.*;

public class COpndCommand implements IOpndCommand {
    private static final char chNegate = '-';
    private static final char chExp = 'e';
    private static final char chPlus = '+';

    private final List<Integer> commands = new ArrayList<>();
    boolean fNegative;
    boolean fSciFmt;
    boolean fDecimal;
    boolean fInitialized;
    private final StringBuilder token = new StringBuilder();
    private Rational value;

    public COpndCommand(List<Integer> commands, boolean fNegative, boolean fDecimal, boolean fSciFmt) {
        this.commands.addAll(commands);
        this.fNegative = fNegative;
        this.fDecimal = fDecimal;
        this.fSciFmt = fSciFmt;
        this.fInitialized = false;
        this.value = new Rational();
    }

    public void initialize(Rational r) {
        this.value = r;
        this.fInitialized = true;
    }

    @Override
    public List<Integer> getCommands() {
        return Collections.unmodifiableList(this.commands);
    }

    @Override
    public void appendCommand(int command) {
        if (fSciFmt)
        {
            ClearAllAndAppendCommand(Command.fromInt(command));
        }
        else
        {
            commands.add(command);
        }

        if (command == IDC_PNT)
        {
            fDecimal = true;
        }
    }

    void ClearAllAndAppendCommand(Command command)
    {
        commands.clear();
        commands.add(command.toInt());
        fSciFmt = false;
        fNegative = false;
        fDecimal = false;
    }

    @Override
    public void toggleSign() {
        for (int nOpCode : commands)
        {
            if (nOpCode != IDC_0)
            {
                fNegative = !fNegative;
                break;
            }
        }
    }

    @Override
    public void removeFromEnd() {
        if (fSciFmt)
        {
            ClearAllAndAppendCommand(Command.Command0);
        }
        else
        {
            int nCommands = commands.size();

            if (nCommands == 1)
            {
                ClearAllAndAppendCommand(Command.Command0);
            }
            else
            {
                int nOpCode = commands.get(nCommands - 1);

                if (nOpCode == IDC_PNT)
                {
                    fDecimal = false;
                }

                commands.removeLast();
            }
        }
    }

    @Override
    public boolean isNegative() {
        return fNegative;
    }

    @Override
    public boolean isSciFmt() {
        return fSciFmt;
    }

    @Override
    public boolean isDecimalPresent() {
        return fDecimal;
    }

    @Override
    public String getToken(char decimalSymbol) {
        final char chZero = '0';

        int nCommands = commands.size();
        token.setLength(0);

        for (int i = 0; i < nCommands; i++)
        {
            int nOpCode = commands.get(i);

            if (nOpCode == IDC_PNT)
            {
                token.append(decimalSymbol);
            }
            else if (nOpCode == IDC_EXP)
            {
                token.append(chExp);
                int nextOpCode = commands.get(i + 1);
                if (nextOpCode != IDC_SIGN)
                {
                    token.append(chPlus);
                }
            }
            else if (nOpCode == IDC_SIGN)
            {
                token.append(chNegate);
            }
            else
            {
                char num = (char)(nOpCode - IDC_0);
                token.append(num);
            }
        }

        // Remove zeros
        for (int i = 0; i < token.length(); i++)
        {
            if (token.charAt(i) != chZero)
            {
                if (token.charAt(i) == decimalSymbol)
                {
                    // token.erase(0, i - 1);
                    token.delete(0, i-1);
                }
                else
                {
                    // token.erase(0, i);
                    token.delete(0, i);
                }

                if (fNegative)
                {
                    token.insert(0, chNegate);
                }

                return token.toString();
            }
        }

        token.setLength(0);
        token.append(chZero);

        return token.toString();
    }

    @Override
    public void setCommands(List<Integer> commands) {
        this.commands.clear();
        this.commands.addAll(commands);
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.OperandCommand;
    }

    public String getString(uint radix, int precision)
    {
        if (fInitialized)
        {
            return value.toString(radix, NumberFormat.Float, precision);
        }

        return "";
    }

    @Override
    public void accept(ISerializeCommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
