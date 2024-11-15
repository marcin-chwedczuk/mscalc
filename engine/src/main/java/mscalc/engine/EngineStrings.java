package mscalc.engine;

public interface EngineStrings {

    int IDS_ERRORS_FIRST = 99;

// This is the list of error strings corresponding to SCERR_DIVIDEZERO..

    int IDS_DIVBYZERO = IDS_ERRORS_FIRST;
    int IDS_DOMAIN = IDS_ERRORS_FIRST + 1;
    int IDS_UNDEFINED = IDS_ERRORS_FIRST + 2;
    int IDS_POS_INFINITY = IDS_ERRORS_FIRST + 3;
    int IDS_NEG_INFINITY = IDS_ERRORS_FIRST + 4;
    int IDS_NOMEM = IDS_ERRORS_FIRST + 6;
    int IDS_TOOMANY = IDS_ERRORS_FIRST + 7;
    int IDS_OVERFLOW = IDS_ERRORS_FIRST + 8;
    int IDS_NORESULT = IDS_ERRORS_FIRST + 9;
    int IDS_INSUFFICIENT_DATA = IDS_ERRORS_FIRST + 10;

    int CSTRINGSENGMAX = IDS_INSUFFICIENT_DATA + 1;

    // Arithmetic expression evaluator error strings
    int IDS_ERR_UNK_CH = CSTRINGSENGMAX + 1;
    int IDS_ERR_UNK_FN = CSTRINGSENGMAX + 2;
    int IDS_ERR_UNEX_NUM = CSTRINGSENGMAX + 3;
    int IDS_ERR_UNEX_CH = CSTRINGSENGMAX + 4;
    int IDS_ERR_UNEX_SZ = CSTRINGSENGMAX + 5;
    int IDS_ERR_MISMATCH_CLOSE = CSTRINGSENGMAX + 6;
    int IDS_ERR_UNEX_END = CSTRINGSENGMAX + 7;
    int IDS_ERR_SG_INV_ERROR = CSTRINGSENGMAX + 8;
    int IDS_ERR_INPUT_OVERFLOW = CSTRINGSENGMAX + 9;
    int IDS_ERR_OUTPUT_OVERFLOW = CSTRINGSENGMAX + 10;

    // Resource keys for CEngineStrings.resw
    String SIDS_PLUS_MINUS = "0";
    String SIDS_CLEAR = "1";
    String SIDS_CE = "2";
    String SIDS_BACKSPACE = "3";
    String SIDS_DECIMAL_SEPARATOR = "4";
    String SIDS_EMPTY_STRING = "5";
    String SIDS_AND = "6";
    String SIDS_OR = "7";
    String SIDS_XOR = "8";
    String SIDS_LSH = "9";
    String SIDS_RSH = "10";
    String SIDS_DIVIDE = "11";
    String SIDS_MULTIPLY = "12";
    String SIDS_PLUS = "13";
    String SIDS_MINUS = "14";
    String SIDS_MOD = "15";
    String SIDS_YROOT = "16";
    String SIDS_POW_HAT = "17";
    String SIDS_INT = "18";
    String SIDS_ROL = "19";
    String SIDS_ROR = "20";
    String SIDS_NOT = "21";
    String SIDS_SIN = "22";
    String SIDS_COS = "23";
    String SIDS_TAN = "24";
    String SIDS_SINH = "25";
    String SIDS_COSH = "26";
    String SIDS_TANH = "27";
    String SIDS_LN = "28";
    String SIDS_LOG = "29";
    String SIDS_SQRT = "30";
    String SIDS_XPOW2 = "31";
    String SIDS_XPOW3 = "32";
    String SIDS_NFACTORIAL = "33";
    String SIDS_RECIPROCAL = "34";
    String SIDS_DMS = "35";
    String SIDS_POWTEN = "37";
    String SIDS_PERCENT = "38";
    String SIDS_SCIENTIFIC_NOTATION = "39";
    String SIDS_PI = "40";
    String SIDS_EQUAL = "41";
    String SIDS_MC = "42";
    String SIDS_MR = "43";
    String SIDS_MS = "44";
    String SIDS_MPLUS = "45";
    String SIDS_MMINUS = "46";
    String SIDS_EXP = "47";
    String SIDS_OPEN_PAREN = "48";
    String SIDS_CLOSE_PAREN = "49";
    String SIDS_0 = "50";
    String SIDS_1 = "51";
    String SIDS_2 = "52";
    String SIDS_3 = "53";
    String SIDS_4 = "54";
    String SIDS_5 = "55";
    String SIDS_6 = "56";
    String SIDS_7 = "57";
    String SIDS_8 = "58";
    String SIDS_9 = "59";
    String SIDS_A = "60";
    String SIDS_B = "61";
    String SIDS_C = "62";
    String SIDS_D = "63";
    String SIDS_E = "64";
    String SIDS_F = "65";
    String SIDS_FRAC = "66";
    String SIDS_SIND = "67";
    String SIDS_COSD = "68";
    String SIDS_TAND = "69";
    String SIDS_ASIND = "70";
    String SIDS_ACOSD = "71";
    String SIDS_ATAND = "72";
    String SIDS_SINR = "73";
    String SIDS_COSR = "74";
    String SIDS_TANR = "75";
    String SIDS_ASINR = "76";
    String SIDS_ACOSR = "77";
    String SIDS_ATANR = "78";
    String SIDS_SING = "79";
    String SIDS_COSG = "80";
    String SIDS_TANG = "81";
    String SIDS_ASING = "82";
    String SIDS_ACOSG = "83";
    String SIDS_ATANG = "84";
    String SIDS_ASINH = "85";
    String SIDS_ACOSH = "86";
    String SIDS_ATANH = "87";
    String SIDS_POWE = "88";
    String SIDS_POWTEN2 = "89";
    String SIDS_SQRT2 = "90";
    String SIDS_SQR = "91";
    String SIDS_CUBE = "92";
    String SIDS_CUBERT = "93";
    String SIDS_FACT = "94";
    String SIDS_RECIPROC = "95";
    String SIDS_DEGREES = "96";
    String SIDS_NEGATE = "97";
    String SIDS_RSH2 = "98";
    String SIDS_DIVIDEBYZERO = "99";
    String SIDS_DOMAIN = "100";
    String SIDS_UNDEFINED = "101";
    String SIDS_POS_INFINITY = "102";
    String SIDS_NEG_INFINITY = "103";
    String SIDS_ABORTED = "104";
    String SIDS_NOMEM = "105";
    String SIDS_TOOMANY = "106";
    String SIDS_OVERFLOW = "107";
    String SIDS_NORESULT = "108";
    String SIDS_INSUFFICIENT_DATA = "109";
    // 110 is skipped by CSTRINGSENGMAX
    String SIDS_ERR_UNK_CH = "111";
    String SIDS_ERR_UNK_FN = "112";
    String SIDS_ERR_UNEX_NUM = "113";
    String SIDS_ERR_UNEX_CH = "114";
    String SIDS_ERR_UNEX_SZ = "115";
    String SIDS_ERR_MISMATCH_CLOSE = "116";
    String SIDS_ERR_UNEX_END = "117";
    String SIDS_ERR_SG_INV_ERROR = "118";
    String SIDS_ERR_INPUT_OVERFLOW = "119";
    String SIDS_ERR_OUTPUT_OVERFLOW = "120";
    String SIDS_SECD = "SecDeg";
    String SIDS_SECR = "SecRad";
    String SIDS_SECG = "SecGrad";
    String SIDS_ASECD = "InverseSecDeg";
    String SIDS_ASECR = "InverseSecRad";
    String SIDS_ASECG = "InverseSecGrad";
    String SIDS_CSCD = "CscDeg";
    String SIDS_CSCR = "CscRad";
    String SIDS_CSCG = "CscGrad";
    String SIDS_ACSCD = "InverseCscDeg";
    String SIDS_ACSCR = "InverseCscRad";
    String SIDS_ACSCG = "InverseCscGrad";
    String SIDS_COTD = "CotDeg";
    String SIDS_COTR = "CotRad";
    String SIDS_COTG = "CotGrad";
    String SIDS_ACOTD = "InverseCotDeg";
    String SIDS_ACOTR = "InverseCotRad";
    String SIDS_ACOTG = "InverseCotGrad";
    String SIDS_SECH = "Sech";
    String SIDS_ASECH = "InverseSech";
    String SIDS_CSCH = "Csch";
    String SIDS_ACSCH = "InverseCsch";
    String SIDS_COTH = "Coth";
    String SIDS_ACOTH = "InverseCoth";
    String SIDS_TWOPOWX = "TwoPowX";
    String SIDS_LOGBASEY = "LogBaseY";
    String SIDS_ABS = "Abs";
    String SIDS_FLOOR = "Floor";
    String SIDS_CEIL = "Ceil";
    String SIDS_NAND = "Nand";
    String SIDS_NOR = "Nor";
    String SIDS_CUBEROOT = "CubeRoot";
    String SIDS_PROGRAMMER_MOD = "ProgrammerMod";

    // Include the resource key ID from above into this vector to load it into memory for the engine to use
    String[] g_sids = new String[]{
            SIDS_PLUS_MINUS,
            SIDS_C,
            SIDS_CE,
            SIDS_BACKSPACE,
            SIDS_DECIMAL_SEPARATOR,
            SIDS_EMPTY_STRING,
            SIDS_AND,
            SIDS_OR,
            SIDS_XOR,
            SIDS_LSH,
            SIDS_RSH,
            SIDS_DIVIDE,
            SIDS_MULTIPLY,
            SIDS_PLUS,
            SIDS_MINUS,
            SIDS_MOD,
            SIDS_YROOT,
            SIDS_POW_HAT,
            SIDS_INT,
            SIDS_ROL,
            SIDS_ROR,
            SIDS_NOT,
            SIDS_SIN,
            SIDS_COS,
            SIDS_TAN,
            SIDS_SINH,
            SIDS_COSH,
            SIDS_TANH,
            SIDS_LN,
            SIDS_LOG,
            SIDS_SQRT,
            SIDS_XPOW2,
            SIDS_XPOW3,
            SIDS_NFACTORIAL,
            SIDS_RECIPROCAL,
            SIDS_DMS,
            SIDS_POWTEN,
            SIDS_PERCENT,
            SIDS_SCIENTIFIC_NOTATION,
            SIDS_PI,
            SIDS_EQUAL,
            SIDS_MC,
            SIDS_MR,
            SIDS_MS,
            SIDS_MPLUS,
            SIDS_MMINUS,
            SIDS_EXP,
            SIDS_OPEN_PAREN,
            SIDS_CLOSE_PAREN,
            SIDS_0,
            SIDS_1,
            SIDS_2,
            SIDS_3,
            SIDS_4,
            SIDS_5,
            SIDS_6,
            SIDS_7,
            SIDS_8,
            SIDS_9,
            SIDS_A,
            SIDS_B,
            SIDS_C,
            SIDS_D,
            SIDS_E,
            SIDS_F,
            SIDS_FRAC,
            SIDS_SIND,
            SIDS_COSD,
            SIDS_TAND,
            SIDS_ASIND,
            SIDS_ACOSD,
            SIDS_ATAND,
            SIDS_SINR,
            SIDS_COSR,
            SIDS_TANR,
            SIDS_ASINR,
            SIDS_ACOSR,
            SIDS_ATANR,
            SIDS_SING,
            SIDS_COSG,
            SIDS_TANG,
            SIDS_ASING,
            SIDS_ACOSG,
            SIDS_ATANG,
            SIDS_ASINH,
            SIDS_ACOSH,
            SIDS_ATANH,
            SIDS_POWE,
            SIDS_POWTEN2,
            SIDS_SQRT2,
            SIDS_SQR,
            SIDS_CUBE,
            SIDS_CUBERT,
            SIDS_FACT,
            SIDS_RECIPROC,
            SIDS_DEGREES,
            SIDS_NEGATE,
            SIDS_RSH,
            SIDS_DIVIDEBYZERO,
            SIDS_DOMAIN,
            SIDS_UNDEFINED,
            SIDS_POS_INFINITY,
            SIDS_NEG_INFINITY,
            SIDS_ABORTED,
            SIDS_NOMEM,
            SIDS_TOOMANY,
            SIDS_OVERFLOW,
            SIDS_NORESULT,
            SIDS_INSUFFICIENT_DATA,
            SIDS_ERR_UNK_CH,
            SIDS_ERR_UNK_FN,
            SIDS_ERR_UNEX_NUM,
            SIDS_ERR_UNEX_CH,
            SIDS_ERR_UNEX_SZ,
            SIDS_ERR_MISMATCH_CLOSE,
            SIDS_ERR_UNEX_END,
            SIDS_ERR_SG_INV_ERROR,
            SIDS_ERR_INPUT_OVERFLOW,
            SIDS_ERR_OUTPUT_OVERFLOW,
            SIDS_SECD,
            SIDS_SECG,
            SIDS_SECR,
            SIDS_ASECD,
            SIDS_ASECR,
            SIDS_ASECG,
            SIDS_CSCD,
            SIDS_CSCR,
            SIDS_CSCG,
            SIDS_ACSCD,
            SIDS_ACSCR,
            SIDS_ACSCG,
            SIDS_COTD,
            SIDS_COTR,
            SIDS_COTG,
            SIDS_ACOTD,
            SIDS_ACOTR,
            SIDS_ACOTG,
            SIDS_SECH,
            SIDS_ASECH,
            SIDS_CSCH,
            SIDS_ACSCH,
            SIDS_COTH,
            SIDS_ACOTH,
            SIDS_TWOPOWX,
            SIDS_LOGBASEY,
            SIDS_ABS,
            SIDS_FLOOR,
            SIDS_CEIL,
            SIDS_NAND,
            SIDS_NOR,
            SIDS_CUBEROOT,
            SIDS_PROGRAMMER_MOD,
    };

}
