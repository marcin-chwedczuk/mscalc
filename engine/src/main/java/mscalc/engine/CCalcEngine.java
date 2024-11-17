package mscalc.engine;

import mscalc.engine.commands.IExpressionCommand;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.RatPack;
import mscalc.engine.ratpack.RatPack.AngleType;
import mscalc.engine.resource.JavaBundleResourceProvider;
import mscalc.engine.resource.ResourceProvider;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Map.entry;
import static mscalc.engine.Commands.*;
import static mscalc.engine.EngineStrings.*;
import static mscalc.engine.History.MAXPRECDEPTH;
import static mscalc.engine.ratpack.Conv.SetDecimalSeparator;
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
}
