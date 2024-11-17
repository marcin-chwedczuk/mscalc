package mscalc.engine;

import mscalc.engine.commands.IExpressionCommand;
import mscalc.engine.cpp.ErrorCodeException;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack;
import mscalc.engine.ratpack.RatPack.AngleType;
import mscalc.engine.resource.JavaBundleResourceProvider;
import mscalc.engine.resource.ResourceProvider;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Map.entry;
import static mscalc.engine.CalcUtils.*;
import static mscalc.engine.Commands.*;
import static mscalc.engine.EngineStrings.*;
import static mscalc.engine.History.MAXPRECDEPTH;
import static mscalc.engine.ratpack.CalcErr.CALC_E_DOMAIN;
import static mscalc.engine.ratpack.CalcErr.CALC_E_OVERFLOW;
import static mscalc.engine.ratpack.Conv.SetDecimalSeparator;
import static mscalc.engine.ratpack.Conv.StringToRat;
import static mscalc.engine.ratpack.Support.ChangeConstants;
import static mscalc.engine.ratpack.Support.Global.*;

public class CCalcEngine {
    private static final int NUM_WIDTH_LENGTH = 4;

    private static final int DEFAULT_MAX_DIGITS = 32;
    private static final int DEFAULT_PRECISION = 32;
    private static final int DEFAULT_RADIX = 10;

    private static final char DEFAULT_DEC_SEPARATOR = '.';
    private static final char DEFAULT_GRP_SEPARATOR = ',';
    private static final String DEFAULT_GRP_STR = "3;0";
    private static final String DEFAULT_NUMBER_STR = "0";

    private static final Map<String, String> engineStrings = new HashMap<>();

    // TODO: Remove
    static {
        loadEngineStrings(new JavaBundleResourceProvider());
    }


    // Unary operator Function Name table Element
    // since unary operators button names aren't exactly friendly for history purpose,
    // we have this separate table to get its localized name and for its Inv function if it exists.
    record FunctionNameElement(
            String degreeString,        // Used by default if there are no rad or grad specific strings.
            String inverseDegreeString, // Will fall back to degreeString if empty

            String radString,
            String inverseRadString, // Will fall back to radString if empty

            String gradString,
            String inverseGradString, // Will fall back to gradString if empty

            String programmerModeString) {

        public FunctionNameElement(String degreeString) {
            this(degreeString, "", "", "", "", "", "");
        }

        public FunctionNameElement(String degreeString, String inverseDegreeString) {
            this(degreeString, inverseDegreeString, "", "", "", "", "");
        }

        public FunctionNameElement(String degreeString, String inverseDegreeString, String radString, String inverseRadString, String gradString, String inverseGradString) {
            this(degreeString, inverseDegreeString, radString, inverseRadString, gradString, inverseGradString, "");
        }

        public boolean hasAngleStrings() {
            return ((!radString.isEmpty()) || (!inverseRadString.isEmpty()) || (!gradString.isEmpty()) || (!inverseGradString.isEmpty()));
        }
    }

    ;

    // Table for each unary operator
    static final Map<Integer, FunctionNameElement> operatorStringTable = Map.ofEntries(
            entry(IDC_CHOP, new FunctionNameElement("", SIDS_FRAC)),

            entry(IDC_SIN, new FunctionNameElement(SIDS_SIND, SIDS_ASIND, SIDS_SINR, SIDS_ASINR, SIDS_SING, SIDS_ASING)),
            entry(IDC_COS, new FunctionNameElement(SIDS_COSD, SIDS_ACOSD, SIDS_COSR, SIDS_ACOSR, SIDS_COSG, SIDS_ACOSG)),
            entry(IDC_TAN, new FunctionNameElement(SIDS_TAND, SIDS_ATAND, SIDS_TANR, SIDS_ATANR, SIDS_TANG, SIDS_ATANG)),

            entry(IDC_SINH, new FunctionNameElement("", SIDS_ASINH)),
            entry(IDC_COSH, new FunctionNameElement("", SIDS_ACOSH)),
            entry(IDC_TANH, new FunctionNameElement("", SIDS_ATANH)),

            entry(IDC_SEC, new FunctionNameElement(SIDS_SECD, SIDS_ASECD, SIDS_SECR, SIDS_ASECR, SIDS_SECG, SIDS_ASECG)),
            entry(IDC_CSC, new FunctionNameElement(SIDS_CSCD, SIDS_ACSCD, SIDS_CSCR, SIDS_ACSCR, SIDS_CSCG, SIDS_ACSCG)),
            entry(IDC_COT, new FunctionNameElement(SIDS_COTD, SIDS_ACOTD, SIDS_COTR, SIDS_ACOTR, SIDS_COTG, SIDS_ACOTG)),

            entry(IDC_SECH, new FunctionNameElement(SIDS_SECH, SIDS_ASECH)),
            entry(IDC_CSCH, new FunctionNameElement(SIDS_CSCH, SIDS_ACSCH)),
            entry(IDC_COTH, new FunctionNameElement(SIDS_COTH, SIDS_ACOTH)),

            entry(IDC_LN, new FunctionNameElement("", SIDS_POWE)),
            entry(IDC_SQR, new FunctionNameElement(SIDS_SQR)),
            entry(IDC_CUB, new FunctionNameElement(SIDS_CUBE)),
            entry(IDC_FAC, new FunctionNameElement(SIDS_FACT)),
            entry(IDC_REC, new FunctionNameElement(SIDS_RECIPROC)),
            entry(IDC_DMS, new FunctionNameElement("", SIDS_DEGREES)),
            entry(IDC_SIGN, new FunctionNameElement(SIDS_NEGATE)),
            entry(IDC_DEGREES, new FunctionNameElement(SIDS_DEGREES)),
            entry(IDC_POW2, new FunctionNameElement(SIDS_TWOPOWX)),
            entry(IDC_LOGBASEY, new FunctionNameElement(SIDS_LOGBASEY)),
            entry(IDC_ABS, new FunctionNameElement(SIDS_ABS)),
            entry(IDC_CEIL, new FunctionNameElement(SIDS_CEIL)),
            entry(IDC_FLOOR, new FunctionNameElement(SIDS_FLOOR)),
            entry(IDC_NAND, new FunctionNameElement(SIDS_NAND)),
            entry(IDC_NOR, new FunctionNameElement(SIDS_NOR)),
            entry(IDC_RSHFL, new FunctionNameElement(SIDS_RSH)),
            entry(IDC_RORC, new FunctionNameElement(SIDS_ROR)),
            entry(IDC_ROLC, new FunctionNameElement(SIDS_ROL)),
            entry(IDC_CUBEROOT, new FunctionNameElement(SIDS_CUBEROOT)),
            entry(IDC_MOD, new FunctionNameElement(SIDS_MOD, "", "", "", "", "", SIDS_PROGRAMMER_MOD))
    );

    static void loadEngineStrings(ResourceProvider resourceProvider) {
        for (var sid : g_sids) {
            var locString = resourceProvider.getCEngineString(sid);
            if (!locString.isEmpty()) {
                System.out.printf("Loaded: %s -> %s%n", sid, locString);
                engineStrings.put(sid, locString);
            }
        }
    }

    // returns the ptr to string representing the operator. Mostly same as the button, but few special cases for x^y etc.
    private static String GetString(int ids) {
        return engineStrings.get(Integer.toString(ids));
    }

    private static String GetString(String ids) {
        return engineStrings.get(ids);
    }

    public static String OpCodeToBinaryString(int nOpCode, boolean isIntegerMode) {
        // Try to lookup the ID in the UFNE table
        String ids = "";
        var fne = operatorStringTable.get(nOpCode);
        if (fne != null) {
            if (isIntegerMode && !fne.programmerModeString().isEmpty()) {
                ids = fne.programmerModeString();
            } else {
                ids = fne.degreeString();
            }
        }

        if (!ids.isEmpty()) {
            return GetString(ids);
        }

        // If we didn't find an ID in the table, use the op code.
        return OpCodeToString(nOpCode);
    }

    private static int IdStrFromCmdId(int id) {
        return id - IDC_FIRSTCONTROL + IDS_ENGINESTR_FIRST;
    }

    public static String OpCodeToString(int nOpCode) {
        return GetString(IdStrFromCmdId(nOpCode));
    }

    public static String OpCodeToUnaryString(int nOpCode, boolean fInv, AngleType angletype) {
        // Try to lookup the ID in the UFNE table
        String ids = "";
        var pair = operatorStringTable.get(nOpCode);
        if (pair != null) {
            FunctionNameElement element = pair;
            if (!element.hasAngleStrings() || AngleType.Degrees == angletype) {
                if (fInv) {
                    ids = element.inverseDegreeString;
                }

                if (ids.isEmpty()) {
                    ids = element.degreeString;
                }
            } else if (AngleType.Radians == angletype) {
                if (fInv) {
                    ids = element.inverseRadString;
                }
                if (ids.isEmpty()) {
                    ids = element.radString;
                }
            } else if (AngleType.Gradians == angletype) {
                if (fInv) {
                    ids = element.inverseGradString;
                }
                if (ids.isEmpty()) {
                    ids = element.gradString;
                }
            }
        }

        if (!ids.isEmpty()) {
            return GetString(ids);
        }

        // If we didn't find an ID in the table, use the op code.
        return OpCodeToString(nOpCode);
    }

    boolean m_fPrecedence;
    boolean m_fIntegerMode; /* This is true if engine is explicitly called to be in integer mode. All bases are restricted to be in integers only */
    CalcDisplay m_pCalcDisplay;
    ResourceProvider m_resourceProvider;
    int m_nOpCode;     /* ID value of operation.                       */
    int m_nPrevOpCode; // opcode which computed the number in m_currentVal. 0 if it is already bracketed or plain number or
    // if it hasn't yet been computed
    boolean m_bChangeOp;              // Flag for changing operation
    boolean m_bRecord;                // Global mode: recording or displaying
    boolean m_bSetCalcState;          // Flag for setting the engine result state
    CalcInput m_input; // Global calc input object for decimal strings
    RatPack.NumberFormat m_nFE;            // Scientific notation conversion flag
    Rational m_maxTrigonometricNum;
    Rational m_memoryValue; // Current memory value.

    Rational m_holdVal; // For holding the second operand in repetitive calculations ( pressing "=" continuously)

    Rational m_currentVal;                               // Currently displayed number used everywhere.
    Rational m_lastVal;                                  // Number before operation (left operand).
    Rational[] m_parenVals = new Rational[MAXPRECDEPTH];      // Holding array for parenthesis values.
    Rational[] m_precedenceVals = new Rational[MAXPRECDEPTH]; // Holding array for precedence values.
    boolean m_bError;                                                   // Error flag.
    boolean m_bInv;                                                     // Inverse on/off flag.
    boolean m_bNoPrevEqu;                                               /* Flag for previous equals.          */

    uint m_radix;
    int m_precision;
    int m_cIntDigitsSav;
    List<Integer> m_decGrouping = new ArrayList<>(); // Holds the decimal digit grouping number

    String m_numberString;

    int m_nTempCom;                          /* Holding place for the last command.          */
    int m_openParenCount;                 // Number of open parentheses.
    int[] m_nOp = new int[MAXPRECDEPTH];     /* Holding array for parenthesis operations.    */
    int[] m_nPrecOp = new int[MAXPRECDEPTH]; /* Holding array for precedence  operations.    */
    int m_precedenceOpCount;              /* Current number of precedence ops in holding. */
    int m_nLastCom;                          // Last command entered.
    AngleType m_angletype;                   // Current Angle type when in dec mode. one of deg, rad or grad
    NUM_WIDTH m_numwidth;                    // one of qword, dword, word or byte mode.
    int m_dwWordBitWidth;                // # of bits in currently selected word size

    Random m_randomGeneratorEngine = new Random();

    long m_carryBit;

    History m_HistoryCollector; // Accumulator of each line of history as various commands are processed

    Rational[] m_chopNumbers = new Rational[NUM_WIDTH_LENGTH];           // word size enforcement
    String[] m_maxDecimalValueStrings = new String[NUM_WIDTH_LENGTH];        // maximum values represented by a given word width based off m_chopNumbers
    char m_decimalSeparator;
    char m_groupSeparator;

    public CCalcEngine(boolean fPrecedence,
                       boolean fIntegerMode,
                       ResourceProvider pResourceProvider,
                       CalcDisplay pCalcDisplay,
                       HistoryDisplay pHistoryDisplay) {

        this.m_fPrecedence = fPrecedence;
        this.m_fIntegerMode = fIntegerMode;
        this.m_pCalcDisplay = pCalcDisplay;
        this.m_resourceProvider = pResourceProvider;
        m_nOpCode = (0);
        m_nPrevOpCode = (0);
        m_bChangeOp = (false);
        m_bRecord = (false);
        m_bSetCalcState = (false);
        m_input = new CalcInput(DEFAULT_DEC_SEPARATOR);
        m_nFE = (RatPack.NumberFormat.Float);
        m_memoryValue = Rational.of(0);
        m_holdVal = Rational.of(0);
        m_currentVal = Rational.of(0);
        m_lastVal = Rational.of(0);

        m_bError = (false);
        m_bInv = (false);
        m_bNoPrevEqu = (true);
        m_radix = uint.of(DEFAULT_RADIX);
        m_precision = (DEFAULT_PRECISION);
        m_cIntDigitsSav = (DEFAULT_MAX_DIGITS);
        m_numberString = (DEFAULT_NUMBER_STR);
        m_nTempCom = (0);
        m_openParenCount = (0);
        m_precedenceOpCount = (0);
        m_nLastCom = (0);
        m_angletype = (AngleType.Degrees);
        m_numwidth = (NUM_WIDTH.QWORD_WIDTH);
        m_HistoryCollector = new History(pCalcDisplay, pHistoryDisplay, DEFAULT_DEC_SEPARATOR);
        m_groupSeparator = (DEFAULT_GRP_SEPARATOR);

        InitChopNumbers();

        m_dwWordBitWidth = DwWordBitWidthFromNumWidth(m_numwidth);

        m_maxTrigonometricNum = RationalMath.pow(Rational.of(10), Rational.of(100));

        SetRadixTypeAndNumWidth(RadixType::Decimal, m_numwidth);
        SettingsChanged();
        DisplayNum();
    }

    void InitChopNumbers()
    {
        // these rat numbers are set only once and then never change regardless of
        // base or precision changes
        assert(m_chopNumbers.length >= 4);
        m_chopNumbers[0] = Rational.fromCRational(rat_qword);
        m_chopNumbers[1] = Rational.fromCRational(rat_dword);
        m_chopNumbers[2] = Rational.fromCRational(rat_word);
        m_chopNumbers[3] = Rational.fromCRational(rat_byte);

        // initialize the max dec number you can support for each of the supported bit lengths
        // this is basically max num in that width / 2 in integer
        assert(m_chopNumbers.length == m_maxDecimalValueStrings.length);
        for (int i = 0; i < m_chopNumbers.length; i++)
        {
            var maxVal = m_chopNumbers[i].dividedBy(Rational.of(2));
            maxVal = RationalMath.integer(maxVal);

            m_maxDecimalValueStrings[i] = maxVal.toString(uint.of(10), RatPack.NumberFormat.Float, m_precision);
        }
    }

    Rational GetChopNumber()
    {
        return m_chopNumbers[m_numwidth.toInt()];
    }

    String GetMaxDecimalValueString()
    {
        return m_maxDecimalValueStrings[m_numwidth.toInt()];
    }

    // Gets the number in memory for UI to keep it persisted and set it again to a different instance
// of CCalcEngine. Otherwise it will get destructed with the CalcEngine
    Rational PersistedMemObject()
    {
        return m_memoryValue;
    }

    void PersistedMemObject(Rational memObject)
    {
        m_memoryValue = memObject;
    }

    public boolean FInErrorState() {
        return m_bError;
    }

    public boolean IsInputEmpty() {
        return m_input.isEmpty() && (m_numberString.isEmpty() || "0".equals(m_numberString));
    }

    public boolean FInRecordingState() {
        return m_bRecord;
    }

    void ChangePrecision(int precision) {
        m_precision = precision;
        ChangeConstants(m_radix, precision);
    }

    void SettingsChanged()
    {
        char lastDec = m_decimalSeparator;
        String decStr = m_resourceProvider.getCEngineString("sDecimal");
        m_decimalSeparator = decStr.isEmpty() ? DEFAULT_DEC_SEPARATOR : decStr.charAt(0);
        // Until it can be removed, continue to set ratpak decimal here
        SetDecimalSeparator(m_decimalSeparator);

        char lastSep = m_groupSeparator;
        String sepStr = m_resourceProvider.getCEngineString("sThousand");
        m_groupSeparator = sepStr.isEmpty() ? DEFAULT_GRP_SEPARATOR : sepStr.charAt(0);

        var lastDecGrouping = m_decGrouping;
        String grpStr = m_resourceProvider.getCEngineString("sGrouping");
        m_decGrouping = DigitGroupingStringToGroupingVector(grpStr.isEmpty() ? DEFAULT_GRP_STR : grpStr);

        boolean numChanged = false;

        // if the grouping pattern or thousands symbol changed we need to refresh the display
        if (m_decGrouping != lastDecGrouping || m_groupSeparator != lastSep)
        {
            numChanged = true;
        }

        // if the decimal symbol has changed we always do the following things
        if (m_decimalSeparator != lastDec)
        {
            // Re-initialize member variables' decimal point.
            m_input.setDecimalSymbol(m_decimalSeparator);
            m_HistoryCollector.setDecimalSymbol(m_decimalSeparator);

            // put the new decimal symbol into the table used to draw the decimal key
            engineStrings.put(SIDS_DECIMAL_SEPARATOR, Character.toString(m_decimalSeparator));

            // we need to redraw to update the decimal point button
            numChanged = true;
        }

        if (numChanged)
        {
            DisplayNum();
        }
    }

    char DecimalSeparator()
    {
        return m_decimalSeparator;
    }

    List<IExpressionCommand> GetHistoryCollectorCommandsSnapshot()
    {
        var commands = m_HistoryCollector.getCommands();
        if (!m_HistoryCollector.fOpndAddedToHistory() && m_bRecord)
        {
            commands.add(m_HistoryCollector.getOperandCommandsFromString(m_numberString, m_currentVal));
        }
        return commands;
    }

    public static void InitialOneTimeOnlySetup(ResourceProvider resourceProvider) {
        loadEngineStrings(resourceProvider);

        // we must now set up all the ratpak constants and our arrayed pointers
        // to these constants.
        ChangeBaseConstants(DEFAULT_RADIX, DEFAULT_MAX_DIGITS, DEFAULT_PRECISION);
    }

    // NPrecedenceOfOp
    //
    // returns a virtual number for precedence for the operator. We expect binary operator only, otherwise the lowest number
    // 0 is returned. Higher the number, higher the precedence of the operator.
    public static int NPrecedenceOfOp(int nopCode)
    {
        switch (nopCode)
        {
            default:
            case IDC_OR:
            case IDC_XOR:
                return 0;
            case IDC_AND:
            case IDC_NAND:
            case IDC_NOR:
                return 1;
            case IDC_ADD:
            case IDC_SUB:
                return 2;
            case IDC_LSHF:
            case IDC_RSHF:
            case IDC_RSHFL:
            case IDC_MOD:
            case IDC_DIV:
            case IDC_MUL:
                return 3;
            case IDC_PWR:
            case IDC_ROOT:
            case IDC_LOGBASEY:
                return 4;
        }
    }

    // HandleErrorCommand
//
// When it is discovered by the state machine that at this point the input is not valid (eg. "1+)"), we want to proceed as though this input never
// occurred and may be some feedback to user like Beep. The rest of input can then continue by just ignoring this command.
    void HandleErrorCommand(int idc)
    {
        if (!IsGuiSettingOpCode(idc))
        {
            // We would have saved the prev command. Need to forget this state
            m_nTempCom = m_nLastCom;
        }
    }

    void HandleMaxDigitsReached()
    {
        if (null != m_pCalcDisplay)
        {
            m_pCalcDisplay.maxDigitsReached();
        }
    }

    void ClearTemporaryValues()
    {
        m_bInv = false;
        m_input.clear();
        m_bRecord = true;
        CheckAndAddLastBinOpToHistory();
        DisplayNum();
        m_bError = false;
    }

    void ClearDisplay()
    {
        if (null != m_pCalcDisplay)
        {
            m_pCalcDisplay.setExpressionDisplay(new ArrayList<>(), new ArrayList<>());
        }
    }

    void ProcessCommand(int wParam)
    {
        if (wParam == IDC_SET_RESULT)
        {
            wParam = IDC_RECALL;
            m_bSetCalcState = true;
        }

        ProcessCommandWorker(wParam);
    }

    void CCalcEngine::ProcessCommandWorker(int wParam)
    {
        // Save the last command.  Some commands are not saved in this manor, these
        // commands are:
        // Inv, Deg, Rad, Grad, Stat, FE, MClear, Back, and Exp.  The excluded
        // commands are not
        // really mathematical operations, rather they are GUI mode settings.

        if (!IsGuiSettingOpCode(wParam))
        {
            m_nLastCom = m_nTempCom;
            m_nTempCom = (int)wParam;
        }

        // Clear expression shown after = sign, when user do any action.
        if (!m_bNoPrevEqu)
        {
            ClearDisplay();
        }

        if (m_bError)
        {
            if (wParam == IDC_CLEAR)
            {
                // handle "C" normally
            }
            else if (wParam == IDC_CENTR)
            {
                // treat "CE" as "C"
                wParam = IDC_CLEAR;
            }
            else
            {
                HandleErrorCommand(wParam);
                return;
            }
        }

        // Toggle Record/Display mode if appropriate.
        if (m_bRecord)
        {
            if (IsBinOpCode(wParam) || IsUnaryOpCode(wParam) || IsOpInRange(wParam, IDC_FE, IDC_MMINUS) || IsOpInRange(wParam, IDC_OPENP, IDC_CLOSEP)
                    || IsOpInRange(wParam, IDM_HEX, IDM_BIN) || IsOpInRange(wParam, IDM_QWORD, IDM_BYTE) || IsOpInRange(wParam, IDM_DEG, IDM_GRAD)
                    || IsOpInRange(wParam, IDC_BINEDITSTART, IDC_BINEDITEND) || (IDC_INV == wParam) || (IDC_SIGN == wParam && 10 != m_radix) || (IDC_RAND == wParam)
                    || (IDC_EULER == wParam))
            {
                m_bRecord = false;
                m_currentVal = m_input.toRational(m_radix, m_precision);
                DisplayNum(); // Causes 3.000 to shrink to 3. on first op.
            }
        }
        else if (IsDigitOpCode(wParam) || wParam == IDC_PNT)
        {
            m_bRecord = true;
            m_input.clear();
            CheckAndAddLastBinOpToHistory();
        }

        // Interpret digit keys.
        if (IsDigitOpCode(wParam))
        {
            int iValue = (int)(wParam - IDC_0);

            // this is redundant, illegal keys are disabled
            if (iValue >= m_radix.toInt())
            {
                HandleErrorCommand(wParam);
                return;
            }

            if (!m_input.tryAddDigit(iValue, m_radix, m_fIntegerMode, GetMaxDecimalValueString(), m_dwWordBitWidth, m_cIntDigitsSav))
            {
                HandleErrorCommand(wParam);
                HandleMaxDigitsReached();
                return;
            }

            DisplayNum();

            return;
        }

        // BINARY OPERATORS:
        if (IsBinOpCode(wParam))
        {
            // Change the operation if last input was operation.
            if (IsBinOpCode(m_nLastCom))
            {
                boolean fPrecInvToHigher = false; // Is Precedence Inversion from lower to higher precedence happening ??

                m_nOpCode = (int)wParam;

                // Check to see if by changing this binop, a Precedence inversion is happening.
                // Eg. 1 * 2  + and + is getting changed to ^. The previous precedence rules would have already computed
                // 1*2, so we will put additional brackets to cover for precedence inversion and it will become (1 * 2) ^
                // Here * is m_nPrevOpCode, m_currentVal is 2  (by 1*2), m_nLastCom is +, m_nOpCode is ^
                if (m_fPrecedence && 0 != m_nPrevOpCode)
                {
                    int nPrev = NPrecedenceOfOp(m_nPrevOpCode);
                    int nx = NPrecedenceOfOp(m_nLastCom);
                    int ni = NPrecedenceOfOp(m_nOpCode);
                    if (nx <= nPrev && ni > nPrev) // condition for Precedence Inversion
                    {
                        fPrecInvToHigher = true;
                        m_nPrevOpCode = 0; // Once the precedence inversion has put additional brackets, its no longer required
                    }
                }
                m_HistoryCollector.changeLastBinOp(m_nOpCode, fPrecInvToHigher, m_fIntegerMode);
                DisplayAnnounceBinaryOperator();
                return;
            }

            if (!m_HistoryCollector.fOpndAddedToHistory())
            {
                // if the prev command was ) or unop then it is already in history as a opnd form (...)
                m_HistoryCollector.addOpndToHistory(m_numberString, m_currentVal);
            }

            /* m_bChangeOp is true if there was an operation done and the   */
            /* current m_currentVal is the result of that operation.  This is so */
            /* entering 3+4+5= gives 7 after the first + and 12 after the */
            /* the =.  The rest of this stuff attempts to do precedence in*/
            /* Scientific mode.                                           */
            if (m_bChangeOp)
            {
                DoPrecedenceCheckAgain: while(false) { } // Fake statement for the label

                int nx = NPrecedenceOfOp((int)wParam);
                int ni = NPrecedenceOfOp(m_nOpCode);

                if ((nx > ni) && m_fPrecedence)
                {
                    if (m_precedenceOpCount < MAXPRECDEPTH)
                    {
                        m_precedenceVals[m_precedenceOpCount] = m_lastVal;

                        m_nPrecOp[m_precedenceOpCount] = m_nOpCode;
                        m_HistoryCollector.pushLastOpndStart(); // Eg. 1 + 2  *, Need to remember the start of 2 to do Precedence inversion if need to
                    }
                    else
                    {
                        m_precedenceOpCount = MAXPRECDEPTH - 1;
                        HandleErrorCommand(wParam);
                    }
                    m_precedenceOpCount++;
                }
                else
                {
                    /* do the last operation and then if the precedence array is not
                     * empty or the top is not the '(' demarcator then pop the top
                     * of the array and recheck precedence against the new operator
                     */
                    m_currentVal = DoOperation(m_nOpCode, m_currentVal, m_lastVal);
                    m_nPrevOpCode = m_nOpCode;

                    if (!m_bError)
                    {
                        DisplayNum();
                        if (!m_fPrecedence)
                        {
                            String groupedString = GroupDigitsPerRadix(m_numberString, m_radix);
                            m_HistoryCollector.CompleteEquation(groupedString);
                            m_HistoryCollector.AddOpndToHistory(m_numberString, m_currentVal);
                        }
                    }

                    if ((m_precedenceOpCount != 0) && (m_nPrecOp[m_precedenceOpCount - 1] != 0))
                    {
                        m_precedenceOpCount--;
                        m_nOpCode = m_nPrecOp[m_precedenceOpCount];

                        m_lastVal = m_precedenceVals[m_precedenceOpCount];

                        nx = NPrecedenceOfOp(m_nOpCode);
                        // Precedence Inversion Higher to lower can happen which needs explicit enclosure of brackets
                        // Eg.  1 + 2 * Or 3 Or.  We would have pushed 1+ before, and now last + forces 2 Or 3 to be evaluated
                        // because last Or is less or equal to first + (after 1). But we see that 1+ is in stack and we evaluated to 2 Or 3
                        // This is precedence inversion happened because of operator changed in between. We put extra brackets like
                        // 1 + (2 Or 3)
                        if (ni <= nx)
                        {
                            m_HistoryCollector.enclosePrecInversionBrackets();
                        }
                        m_HistoryCollector.popLastOpndStart();
                        goto DoPrecedenceCheckAgain; // GOTO Won't work in Java
                    }
                }
            }

            DisplayAnnounceBinaryOperator();
            m_lastVal = m_currentVal;
            m_nOpCode = (int)wParam;
            m_HistoryCollector.addBinOpToHistory(m_nOpCode, m_fIntegerMode);
            m_bNoPrevEqu = m_bChangeOp = true;
            return;
        }

        // UNARY OPERATORS:
        if (IsUnaryOpCode(wParam) || (wParam == IDC_DEGREES))
        {
            /* Functions are unary operations.                            */
            /* If the last thing done was an operator, m_currentVal was cleared. */
            /* In that case we better use the number before the operator  */
            /* was entered, otherwise, things like 5+ 1/x give Divide By  */
            /* zero.  This way 5+=gives 10 like most calculators do.      */
            if (IsBinOpCode(m_nLastCom))
            {
                m_currentVal = m_lastVal;
            }

            // we do not add percent sign to history or to two line display.
            // instead, we add the result of applying %.
            if (wParam != IDC_PERCENT)
            {
                if (!m_HistoryCollector.fOpndAddedToHistory())
                {
                    m_HistoryCollector.addOpndToHistory(m_numberString, m_currentVal);
                }

                m_HistoryCollector.addUnaryOpToHistory((int)wParam, m_bInv, m_angletype);
            }

            if ((wParam == IDC_SIN) || (wParam == IDC_COS) || (wParam == IDC_TAN) || (wParam == IDC_SINH) || (wParam == IDC_COSH) || (wParam == IDC_TANH)
                    || (wParam == IDC_SEC) || (wParam == IDC_CSC) || (wParam == IDC_COT) || (wParam == IDC_SECH) || (wParam == IDC_CSCH) || (wParam == IDC_COTH))
            {
                if (IsCurrentTooBigForTrig())
                {
                    m_currentVal = Rational.of(0);
                    DisplayError(CALC_E_DOMAIN);
                    return;
                }
            }

            m_currentVal = SciCalcFunctions(m_currentVal, (int)wParam);

            if (m_bError)
                return;

            /* Display the result, reset flags, and reset indicators.     */
            DisplayNum();

            if (wParam == IDC_PERCENT)
            {
                CheckAndAddLastBinOpToHistory();
                m_HistoryCollector.addOpndToHistory(m_numberString, m_currentVal, true /* Add to primary and secondary display */);
            }

        /* reset the m_bInv flag and indicators if it is set
        and have been used */

            if (m_bInv
                    && ((wParam == IDC_CHOP) || (wParam == IDC_SIN) || (wParam == IDC_COS) || (wParam == IDC_TAN) || (wParam == IDC_LN) || (wParam == IDC_DMS)
                    || (wParam == IDC_DEGREES) || (wParam == IDC_SINH) || (wParam == IDC_COSH) || (wParam == IDC_TANH) || (wParam == IDC_SEC) || (wParam == IDC_CSC)
                    || (wParam == IDC_COT) || (wParam == IDC_SECH) || (wParam == IDC_CSCH) || (wParam == IDC_COTH)))
            {
                m_bInv = false;
            }

            return;
        }

        // Tiny binary edit windows clicked. Toggle that bit and update display
        if (IsOpInRange(wParam, IDC_BINEDITSTART, IDC_BINEDITEND))
        {
            // Same reasoning as for unary operators. We need to seed it previous number
            if (IsBinOpCode(m_nLastCom))
            {
                m_currentVal = m_lastVal;
            }

            CheckAndAddLastBinOpToHistory();

            if (TryToggleBit(m_currentVal, (int)wParam - IDC_BINEDITSTART))
            {
                DisplayNum();
            }

            return;
        }

        /* Now branch off to do other commands and functions.                 */
        switch (wParam)
        {
            case IDC_CLEAR: /* Total clear.                                       */
            {
                if (!m_bChangeOp)
                {
                    // Preserve history, if everything done before was a series of unary operations.
                    CheckAndAddLastBinOpToHistory(false);
                }

                m_lastVal = Rational.of(0);

                m_bChangeOp = false;
                m_openParenCount = 0;
                m_precedenceOpCount = m_nTempCom = m_nLastCom = m_nOpCode = 0;
                m_nPrevOpCode = 0;
                m_bNoPrevEqu = true;
                m_carryBit = 0;

        /* clear the parenthesis status box indicator, this will not be
        cleared for CENTR */
                if (null != m_pCalcDisplay)
                {
                    m_pCalcDisplay.setParenthesisNumber(0);
                    ClearDisplay();
                }

                m_HistoryCollector.clearHistoryLine("");
                ClearTemporaryValues();
            }
            break;

            case IDC_CENTR: /* Clear only temporary values.                       */
            {
                // Clear the INV & leave (=xx indicator active
                ClearTemporaryValues();
            }

            break;

            case IDC_BACK:
                // Divide number by the current radix and truncate.
                // Only allow backspace if we're recording.
                if (m_bRecord)
                {
                    m_input.backspace();
                    DisplayNum();
                }
                else
                {
                    HandleErrorCommand(wParam);
                }
                break;

            /* EQU enables the user to press it multiple times after and      */
            /* operation to enable repeats of the last operation.             */
            case IDC_EQU:
                while (m_openParenCount > 0)
                {
                    // when m_bError is set and m_ParNum is non-zero it goes into infinite loop
                    if (m_bError)
                    {
                        break;
                    }
                    // automatic closing of all the parenthesis to get a meaningful result as well as ensure data integrity
                    m_nTempCom = m_nLastCom; // Put back this last saved command to the prev state so ) can be handled properly
                    ProcessCommand(IDC_CLOSEP);
                    m_nLastCom = m_nTempCom;  // Actually this is IDC_CLOSEP
                    m_nTempCom = (int)wParam; // put back in the state where last op seen was IDC_CLOSEP, and current op is IDC_EQU
                }

                if (!m_bNoPrevEqu)
                {
                    // It is possible now unary op changed the num in screen, but still m_lastVal hasn't changed.
                    m_lastVal = m_currentVal;
                }

                /* Last thing keyed in was an operator.  Lets do the op on*/
                /* a duplicate of the last entry.                     */
                if (IsBinOpCode(m_nLastCom))
                {
                    m_currentVal = m_lastVal;
                }

                if (!m_HistoryCollector.fOpndAddedToHistory())
                {
                    m_HistoryCollector.addOpndToHistory(m_numberString, m_currentVal);
                }

                // Evaluate the precedence stack.
                ResolveHighestPrecedenceOperation();
                while (m_fPrecedence && m_precedenceOpCount > 0)
                {
                    m_precedenceOpCount--;
                    m_nOpCode = m_nPrecOp[m_precedenceOpCount];
                    m_lastVal = m_precedenceVals[m_precedenceOpCount];

                    // Precedence Inversion check
                    int ni = NPrecedenceOfOp(m_nPrevOpCode);
                    int nx = NPrecedenceOfOp(m_nOpCode);
                    if (ni <= nx)
                    {
                        m_HistoryCollector.enclosePrecInversionBrackets();
                    }
                    m_HistoryCollector.popLastOpndStart();

                    m_bNoPrevEqu = true;

                    ResolveHighestPrecedenceOperation();
                }

                if (!m_bError)
                {
                    String groupedString = GroupDigitsPerRadix(m_numberString, m_radix);
                    m_HistoryCollector.completeEquation(groupedString);
                }

                m_bChangeOp = false;
                m_nPrevOpCode = 0;

                break;

            case IDC_OPENP:
            case IDC_CLOSEP:

                // -IF- the Paren holding array is full and we try to add a paren
                // -OR- the paren holding array is empty and we try to remove a
                //      paren
                // -OR- the precedence holding array is full
                if ((m_openParenCount >= MAXPRECDEPTH && (wParam == IDC_OPENP)) || (!m_openParenCount && (wParam != IDC_OPENP))
                        || ((m_precedenceOpCount >= MAXPRECDEPTH && m_nPrecOp[m_precedenceOpCount - 1] != 0)))
                {
                    if (m_openParenCount == 0 && (wParam != IDC_OPENP))
                    {
                        m_pCalcDisplay.onNoRightParenAdded();
                    }

                    HandleErrorCommand(wParam);
                    break;
                }

                if (wParam == IDC_OPENP)
                {
                    // if there's an omitted multiplication sign
                    if (IsDigitOpCode(m_nLastCom) || IsUnaryOpCode(m_nLastCom) || m_nLastCom == IDC_PNT || m_nLastCom == IDC_CLOSEP)
                    {
                        ProcessCommand(IDC_MUL);
                    }

                    CheckAndAddLastBinOpToHistory();
                    m_HistoryCollector.addOpenBraceToHistory();

                    // Open level of parentheses, save number and operation.
                    m_parenVals[m_openParenCount] = m_lastVal;

                    m_nOp[m_openParenCount++] = (m_bChangeOp ? m_nOpCode : 0);

                    /* save a special marker on the precedence array */
                    if (m_precedenceOpCount < m_nPrecOp.length)
                    {
                        m_nPrecOp[m_precedenceOpCount++] = 0;
                    }

                    m_lastVal = Rational.of(0);
                    if (IsBinOpCode(m_nLastCom))
                    {
                        // We want 1 + ( to start as 1 + (0. Any number you type replaces 0. But if it is 1 + 3 (, it is
                        // treated as 1 + (3
                        m_currentVal = Rational.of(0);
                    }
                    m_nTempCom = 0;
                    m_nOpCode = 0;
                    m_bChangeOp = false; // a ( is like starting a fresh sub equation
                }
                else
                {
                    // Last thing keyed in was an operator. Lets do the op on a duplicate of the last entry.
                    if (IsBinOpCode(m_nLastCom))
                    {
                        m_currentVal = m_lastVal;
                    }

                    if (!m_HistoryCollector.fOpndAddedToHistory())
                    {
                        m_HistoryCollector.addOpndToHistory(m_numberString, m_currentVal);
                    }

                    // Get the operation and number and return result.
                    m_currentVal = DoOperation(m_nOpCode, m_currentVal, m_lastVal);
                    m_nPrevOpCode = m_nOpCode;

                    // Now process the precedence stack till we get to an opcode which is zero.
                    for (m_nOpCode = m_nPrecOp[--m_precedenceOpCount]; m_nOpCode != 0; m_nOpCode = m_nPrecOp[--m_precedenceOpCount])
                    {
                        // Precedence Inversion check
                        int ni = NPrecedenceOfOp(m_nPrevOpCode);
                        int nx = NPrecedenceOfOp(m_nOpCode);
                        if (ni <= nx)
                        {
                            m_HistoryCollector.enclosePrecInversionBrackets();
                        }
                        m_HistoryCollector.popLastOpndStart();

                        m_lastVal = m_precedenceVals[m_precedenceOpCount];

                        m_currentVal = DoOperation(m_nOpCode, m_currentVal, m_lastVal);
                        m_nPrevOpCode = m_nOpCode;
                    }

                    m_HistoryCollector.addCloseBraceToHistory();

                    // Now get back the operation and opcode at the beginning of this parenthesis pair

                    m_openParenCount -= 1;
                    m_lastVal = m_parenVals[m_openParenCount];
                    m_nOpCode = m_nOp[m_openParenCount];

                    // m_bChangeOp should be true if m_nOpCode is valid
                    m_bChangeOp = (m_nOpCode != 0);
                }

                // Set the "(=xx" indicator.
                if (null != m_pCalcDisplay)
                {
                    m_pCalcDisplay.setParenthesisNumber(m_openParenCount);
                }

                if (!m_bError)
                {
                    DisplayNum();
                }

                break;

            // BASE CHANGES:
            case IDM_HEX:
            case IDM_DEC:
            case IDM_OCT:
            case IDM_BIN:
            {
                SetRadixTypeAndNumWidth((RadixType)(wParam - IDM_HEX), (NUM_WIDTH)(-1)); // TODO: cast -1 to enum; Add undef value to enum
                m_HistoryCollector.updateHistoryExpression(m_radix, m_precision);
                break;
            }

            case IDM_QWORD:
            case IDM_DWORD:
            case IDM_WORD:
            case IDM_BYTE:
                if (m_bRecord)
                {
                    m_currentVal = m_input.toRational(m_radix, m_precision);
                    m_bRecord = false;
                }

                // Compat. mode BaseX: Qword, Dword, Word, Byte
                SetRadixTypeAndNumWidth((RadixType)-1, (NUM_WIDTH)(wParam - IDM_QWORD));
                break;

            case IDM_DEG:
            case IDM_RAD:
            case IDM_GRAD:
                m_angletype = (AngleType)(wParam - IDM_DEG);
                break;

            case IDC_SIGN:
            {
                if (m_bRecord)
                {
                    if (m_input.tryToggleSign(m_fIntegerMode, GetMaxDecimalValueString()))
                    {
                        DisplayNum();
                    }
                    else
                    {
                        HandleErrorCommand(wParam);
                    }
                    break;
                }

                // Doing +/- while in Record mode is not a unary operation
                if (IsBinOpCode(m_nLastCom))
                {
                    m_currentVal = m_lastVal;
                }

                if (!m_HistoryCollector.fOpndAddedToHistory())
                {
                    m_HistoryCollector.addOpndToHistory(m_numberString, m_currentVal);
                }

                m_currentVal = m_currentVal.negated();

                DisplayNum();
                m_HistoryCollector.addUnaryOpToHistory(IDC_SIGN, m_bInv, m_angletype);
            }
            break;

            case IDC_RECALL:
                if (m_bSetCalcState)
                {
                    // Not a Memory recall. set the result
                    m_bSetCalcState = false;
                }
                else
                {
                    // Recall immediate memory value.
                    m_currentVal = m_memoryValue;
                }
                CheckAndAddLastBinOpToHistory();
                DisplayNum();
                break;

            case IDC_MPLUS:
            {
                /* MPLUS adds m_currentVal to immediate memory and kills the "mem"   */
                /* indicator if the result is zero.                           */
                Rational result = m_memoryValue.plus(m_currentVal);
                m_memoryValue = TruncateNumForIntMath(result); // Memory should follow the current int mode

                break;
            }
            case IDC_MMINUS:
            {
                /* MMINUS subtracts m_currentVal to immediate memory and kills the "mem"   */
                /* indicator if the result is zero.                           */
                Rational result = m_memoryValue.minus(m_currentVal);
                m_memoryValue = TruncateNumForIntMath(result);

                break;
            }
            case IDC_STORE:
            case IDC_MCLEAR:
                m_memoryValue = (wParam == IDC_STORE ? TruncateNumForIntMath(m_currentVal) : Rational.of(0));
                break;
            case IDC_PI:
                if (!m_fIntegerMode)
                {
                    CheckAndAddLastBinOpToHistory(); // pi is like entering the number
                    m_currentVal = Rational.fromCRational( (m_bInv ? two_pi : pi) );

                    DisplayNum();
                    m_bInv = false;
                    break;
                }
                HandleErrorCommand(wParam);
                break;
            case IDC_RAND:
                if (!m_fIntegerMode)
                {
                    CheckAndAddLastBinOpToHistory(); // rand is like entering the number

                    // TODO Use BigDecimal with precission...
                    wstringstream str;
                    str << fixed << setprecision(m_precision) << GenerateRandomNumber();

                    RatPack.RAT rat = StringToRat(false, str.str(), false, "", m_radix, m_precision);
                    if (rat != nullptr)
                    {
                        m_currentVal = Rational.fromCRational( rat );
                    }
                    else
                    {
                        m_currentVal = Rational.of( 0 );
                    }

                    DisplayNum();
                    m_bInv = false;
                    break;
                }
                HandleErrorCommand(wParam);
                break;
            case IDC_EULER:
                if (!m_fIntegerMode)
                {
                    CheckAndAddLastBinOpToHistory(); // e is like entering the number
                    m_currentVal = Rational.fromCRational(rat_exp);

                    DisplayNum();
                    m_bInv = false;
                    break;
                }
                HandleErrorCommand(wParam);
                break;
            case IDC_FE:
                // Toggle exponential notation display.
                m_nFE = m_nFE == RatPack.NumberFormat.Float ? RatPack.NumberFormat.Scientific : RatPack.NumberFormat.Float;
                DisplayNum();
                break;

            case IDC_EXP:
                if (m_bRecord && !m_fIntegerMode && m_input.tryBeginExponent())
                {
                    DisplayNum();
                    break;
                }
                HandleErrorCommand(wParam);
                break;

            case IDC_PNT:
                if (m_bRecord && !m_fIntegerMode && m_input.tryAddDecimalPt())
                {
                    DisplayNum();
                    break;
                }
                HandleErrorCommand(wParam);
                break;

            case IDC_INV:
                m_bInv = !m_bInv;
                break;
        }
    }

    // Helper function to resolve one item on the precedence stack.
    void ResolveHighestPrecedenceOperation()
    {
        // Is there a valid operation around?
        if (m_nOpCode != 0)
        {
            // If this is the first EQU in a string, set m_holdVal=m_currentVal
            // Otherwise let m_currentVal=m_holdVal.  This keeps m_currentVal constant
            // through all EQUs in a row.
            if (m_bNoPrevEqu)
            {
                m_holdVal = m_currentVal;
            }
            else
            {
                m_currentVal = m_holdVal;
                DisplayNum(); // to update the m_numberString
                m_HistoryCollector.addBinOpToHistory(m_nOpCode, m_fIntegerMode, false);
                m_HistoryCollector.addOpndToHistory(m_numberString, m_currentVal); // Adding the repeated last op to history
            }

            // Do the current or last operation.
            m_currentVal = DoOperation(m_nOpCode, m_currentVal, m_lastVal);
            m_nPrevOpCode = m_nOpCode;
            m_lastVal = m_currentVal;

            // Check for errors.  If this wasn't done, DisplayNum
            // would immediately overwrite any error message.
            if (!m_bError)
            {
                DisplayNum();
            }

            // No longer the first EQU.
            m_bNoPrevEqu = false;
        }
        else if (!m_bError)
        {
            DisplayNum();
        }
    }

    // CheckAndAddLastBinOpToHistory
    //
    //  This is a very confusing helper routine to add the last entered binary operator to the history. This is expected to
    // leave the history with <exp> <binop> state. It can really add the last entered binary op, or it can actually remove
    // the last operand from history. This happens because you can 'type' or 'compute' over last operand in some cases, thereby
    // effectively removing only it from the equation but still keeping the previous portion of the equation. Eg. 1 + 4 sqrt 5. The last
    // 5 will remove sqrt(4) as it is not used anymore to participate in 1 + 5
    // If you are messing with this, test cases like this CE, statistical functions, ( & MR buttons
    void CheckAndAddLastBinOpToHistory(boolean addToHistory)
    {
        if (m_bChangeOp)
        {
            if (m_HistoryCollector.fOpndAddedToHistory())
            {
                // if last time opnd was added but the last command was not a binary operator, then it must have come
                // from commands which add the operand, like unary operator. So history at this is showing 1 + sqrt(4)
                // but in reality the sqrt(4) is getting replaced by new number (may be unary op, or MR or SUM etc.)
                // So erase the last operand
                m_HistoryCollector.removeLastOpndFromHistory();
            }
        }
        else if (m_HistoryCollector.fOpndAddedToHistory() && !m_bError)
        {
            // Corner case, where opnd is already in history but still a new opnd starting (1 + 4 sqrt 5). This is yet another
            // special casing of previous case under if (m_bChangeOp), but this time we can do better than just removing it
            // Let us make a current value =. So in case of 4 SQRT (or a equation under braces) and then a new equation is started, we can just form
            // a useful equation of sqrt(4) = 2 and continue a new equation from now on. But no point in doing this for things like
            // MR, SUM etc. All you will get is 5 = 5 kind of no useful equation.
            if ((IsUnaryOpCode(m_nLastCom) || IDC_SIGN == m_nLastCom || IDC_CLOSEP == m_nLastCom) && 0 == m_openParenCount)
            {
                if (addToHistory)
                {
                    m_HistoryCollector.completeHistoryLine(GroupDigitsPerRadix(m_numberString, m_radix));
                }
            }
            else
            {
                m_HistoryCollector.removeLastOpndFromHistory();
            }
        }
    }

    // change the display area from a static text to an editbox, which has the focus can make
    // Magnifier (Accessibility tool) work
    void SetPrimaryDisplay(String szText, boolean isError)
    {
        if (m_pCalcDisplay != null)
        {
            m_pCalcDisplay.setPrimaryDisplay(szText, isError);
            m_pCalcDisplay.setIsInError(isError);
        }
    }

    void DisplayAnnounceBinaryOperator()
    {
        // If m_pCalcDisplay is null, this is not a high priority function
        // and should not be the reason we crash.
        if (m_pCalcDisplay != null)
        {
            m_pCalcDisplay.binaryOperatorReceived();
        }
    }

    boolean IsCurrentTooBigForTrig()
    {
        return m_currentVal.isGreaterOrEqual( m_maxTrigonometricNum );
    }

    uint GetCurrentRadix()
    {
        return m_radix;
    }

    String GetCurrentResultForRadix(uint radix, int precision, boolean groupDigitsPerRadix)
    {
        Rational rat = (m_bRecord ? m_input.toRational(m_radix, m_precision) : m_currentVal);

        ChangeConstants(m_radix, precision);

        String numberString = GetStringForDisplay(rat, radix);
        if (!numberString.isEmpty())
        {
            // Revert the precision to previously stored precision
            ChangeConstants(m_radix, m_precision);
        }

        if (groupDigitsPerRadix)
        {
            return GroupDigitsPerRadix(numberString, radix);
        }
        else
        {
            return numberString;
        }
    }

    String GetStringForDisplay(Rational rat, uint radix)
    {
        String result = "";
        // Check for standard\scientific mode
        if (!m_fIntegerMode)
        {
            result = rat.toString(radix, m_nFE, m_precision);
        }
        else
        {
            // Programmer mode
            // Find most significant bit to determine if number is negative
            var tempRat = TruncateNumForIntMath(rat);

            try
            {
                uint64_t w64Bits = tempRat.ToUInt64_t();
                boolean fMsb = ((w64Bits >> (m_dwWordBitWidth - 1)) & 1);
                if ((radix.toInt() == 10) && fMsb)
                {
                    // If high bit is set, then get the decimal number in negative 2's complement form.
                    tempRat = -((tempRat ^ GetChopNumber()) + 1);
                }

                result = tempRat.ToString(radix, m_nFE, m_precision);
            }
            catch (ErrorCodeException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    double GenerateRandomNumber()
    {
        return m_randomGeneratorEngine.nextDouble();
    }

    private static final int MAX_EXPONENT = 4;
    private static final int MAX_GROUPING_SIZE = 16;
    private static final String c_decPreSepStr = "[+-]?(\\d*)[";
    private static final String c_decPostSepStr = "]?(\\d*)(?:e[+-]?(\\d*))?$";

    /****************************************************************************\
     * void DisplayNum(void)
     *
     * Convert m_currentVal to a string in the current radix.
     *
     * Updates the following variables:
     *   m_currentVal, m_numberString
     \****************************************************************************/
    //
    // State of calc last time DisplayNum was called
    //
    class LASTDISP {
        Rational value;
        int precision;
        uint radix;
        int nFE;
        NUM_WIDTH numwidth;
        boolean fIntMath;
        boolean bRecord;
        boolean bUseSep;

        public LASTDISP(
            Rational value,
            int precision,
            uint radix,
            int nFE,
            NUM_WIDTH numwidth,
            boolean fIntMath,
            boolean bRecord,
            boolean bUseSep
        ) {

            this.value = value;
            this.precision = precision;
            this.radix = radix;
            this.nFE = nFE;
            this.numwidth = numwidth;
            this.fIntMath = fIntMath;
            this.bRecord = bRecord;
            this.bUseSep = bUseSep;
        }
    }

    static LASTDISP gldPrevious = new LASTDISP( 0, -1, 0, -1, (NUM_WIDTH)(-1), false, false, false );

    // Truncates if too big, makes it a non negative - the number in rat. Doesn't do anything if not in INT mode
    Rational TruncateNumForIntMath(Rational rat)
    {
        if (!m_fIntegerMode)
        {
            return rat;
        }

        // Truncate to an integer. Do not round here.
        var result = RationalMath.integer(rat);

        // Can be converting a dec negative number to Hex/Oct/Bin rep. Use 2's complement form
        // Check the range.
        if (result.isLessThan(Rational.of(0)))
        {
            // if negative make positive by doing a twos complement
            result = result.negated().minus(Rational.of(1));
            result = result.bitXor(GetChopNumber());
        }

        result = result.bitAnd( GetChopNumber() );

        return result;
    }

    void DisplayNum()
    {
        //
        // Only change the display if
        //  we are in record mode                               -OR-
        //  this is the first time DisplayNum has been called,  -OR-
        //  something important has changed since the last time DisplayNum was
        //  called.
        //
        if (m_bRecord || gldPrevious.value != m_currentVal || gldPrevious.precision != m_precision || gldPrevious.radix != m_radix || gldPrevious.nFE != m_nFE.ordinal()
                || !gldPrevious.bUseSep || gldPrevious.numwidth != m_numwidth || gldPrevious.fIntMath != m_fIntegerMode || gldPrevious.bRecord != m_bRecord)
        {
            gldPrevious.precision = m_precision;
            gldPrevious.radix = m_radix;
            gldPrevious.nFE = (int)m_nFE.ordinal(); // TODO: Ordinal vs C++
            gldPrevious.numwidth = m_numwidth;

            gldPrevious.fIntMath = m_fIntegerMode;
            gldPrevious.bRecord = m_bRecord;
            gldPrevious.bUseSep = true;

            if (m_bRecord)
            {
                // Display the string and return.
                m_numberString = m_input.toString(m_radix);
            }
            else
            {
                // If we're in Programmer mode, perform integer truncation so e.g. 5 / 2 * 2 results in 4, not 5.
                if (m_fIntegerMode)
                {
                    m_currentVal = TruncateNumForIntMath(m_currentVal);
                }
                m_numberString = GetStringForDisplay(m_currentVal, m_radix);
            }

            // Displayed number can go through transformation. So copy it after transformation
            gldPrevious.value = m_currentVal;

            if ((m_radix.toInt() == 10) && IsNumberInvalid(m_numberString, MAX_EXPONENT, m_precision, m_radix))
            {
                DisplayError(CALC_E_OVERFLOW);
            }
            else
            {
                // Display the string and return.
                SetPrimaryDisplay(GroupDigitsPerRadix(m_numberString, m_radix));
            }
        }
    }

    int IsNumberInvalid(String numberString, int iMaxExp, int iMaxMantissa, uint radix)
    {
        int iError = 0;

        if (radix.toInt() == 10)
        {
            // start with an optional + or -
            // followed by zero or more digits
            // followed by an optional decimal point
            // followed by zero or more digits
            // followed by an optional exponent
            // in case there's an exponent:
            //      its optionally followed by a + or -
            //      which is followed by zero or more digits
            wregex rx(wstring{ c_decPreSepStr } + m_decimalSeparator + wstring{ c_decPostSepStr });
            wsmatch matches;
            if (regex_match(numberString, matches, rx))
            {
                // Check that exponent isn't too long
                if (matches.length(3) > iMaxExp)
                {
                    iError = IDS_ERR_INPUT_OVERFLOW;
                }
                else
                {
                    wstring exp = matches.str(1);
                    auto intItr = exp.begin();
                    auto intEnd = exp.end();
                    while (intItr != intEnd && *intItr == L'0')
                    {
                        intItr++;
                    }

                    auto iMantissa = distance(intItr, intEnd) + matches.length(2);
                    if (iMantissa > iMaxMantissa)
                    {
                        iError = IDS_ERR_INPUT_OVERFLOW;
                    }
                }
            }
            else
            {
                iError = IDS_ERR_UNK_CH;
            }
        }
        else
        {
            for (char c : numberString.toCharArray())
            {
                if (radix.toInt() == 16)
                {
                    if (!(Character.isDigit(c) || (c >= 'A' && c <= 'F')))
                    {
                        iError = IDS_ERR_UNK_CH;
                    }
                }
                else if (c < '0' || c >= '0' + radix.toInt())
                {
                    iError = IDS_ERR_UNK_CH;
                }
            }
        }

        return iError;
    }

    /****************************************************************************\
     *
     * DigitGroupingStringToGroupingVector
     *
     * Description:
     *   This will take the digit grouping string found in the regional applet and
     *   represent this string as a vector.
     *
     *   groupingString
     *   0;0      - no grouping
     *   3;0      - group every 3 digits
     *   3        - group 1st 3, then no grouping after
     *   3;0;0    - group 1st 3, then no grouping after
     *   3;2;0    - group 1st 3 and then every 2 digits
     *   4;0      - group every 4 digits
     *   5;3;2;0  - group 5, then 3, then every 2
     *   5;3;2    - group 5, then 3, then 2, then no grouping after
     *
     * Returns: the groupings as a vector
     *
     \****************************************************************************/
    List<Integer> DigitGroupingStringToGroupingVector(String groupingString)
    {
        List<Integer> grouping;
        int currentGroup = 0;
        wchar_t* next = nullptr;
        const wchar_t* begin = groupingString.data();
        const wchar_t* end = begin + groupingString.length();
        for (auto itr = begin; itr != end; ++itr)
        {
            // Try to parse a grouping number from the string
            currentGroup = wcstoul(itr, &next, 10);

            // If we successfully parsed a group, add it to the grouping.
            if (currentGroup < MAX_GROUPING_SIZE)
            {
                grouping.emplace_back(currentGroup);
            }

            // If we found a grouping and aren't at the end of the string yet,
            // jump to the next position in the string (the ';').
            // The loop will then increment us to the next character, which should be a number.
            if (next && (static_cast<size_t>(next - begin) < groupingString.length()))
            {
                itr = next;
            }
        }

        return grouping;
    }
}
