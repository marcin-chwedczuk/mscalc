package mscalc.engine;

public interface Commands {

    // The following are the valid id's which can be passed to CCalcEngine::ProcessCommand

    int IDM_HEX = 313;
    int IDM_DEC = 314;
    int IDM_OCT = 315;
    int IDM_BIN = 316;
    int IDM_QWORD = 317;
    int IDM_DWORD = 318;
    int IDM_WORD = 319;
    int IDM_BYTE = 320;
    int IDM_DEG = 321;
    int IDM_RAD = 322;
    int IDM_GRAD = 323;
    int IDM_DEGREES = 324;

    int IDC_HEX = IDM_HEX;
    int IDC_DEC = IDM_DEC;
    int IDC_OCT = IDM_OCT;
    int IDC_BIN = IDM_BIN;

    int IDC_DEG = IDM_DEG;
    int IDC_RAD = IDM_RAD;
    int IDC_GRAD = IDM_GRAD;
    int IDC_DEGREES = IDM_DEGREES;

    int IDC_QWORD = IDM_QWORD;
    int IDC_DWORD = IDM_DWORD;
    int IDC_WORD = IDM_WORD;
    int IDC_BYTE = IDM_BYTE;

    // Key IDs:
    // These id's must be consecutive from IDC_FIRSTCONTROL to IDC_LASTCONTROL.
    // The actual values don't matter but the order and sequence are very important.
    // Also, the order of the controls must match the order of the control names
    // in the string table.
    // For example you want to declare the color for the control IDC_ST_AVE
    // Find the string id for that control from the rc file
    // Now define the control's id as IDC_FRISTCONTROL+stringID(IDC_ST_AVE)
    int IDC_SIGN = 80;
    int IDC_FIRSTCONTROL = IDC_SIGN;
    int IDC_CLEAR = 81;
    int IDC_CENTR = 82;
    int IDC_BACK = 83;

    int IDC_PNT = 84;

    // Hole  85

    int IDC_AND = 86; // Binary operators must be between IDC_AND and IDC_PWR
    int IDC_OR = 87;
    int IDC_XOR = 88;
    int IDC_LSHF = 89;
    int IDC_RSHF = 90;
    int IDC_DIV = 91;
    int IDC_MUL = 92;
    int IDC_ADD = 93;
    int IDC_SUB = 94;
    int IDC_MOD = 95;
    int IDC_ROOT = 96;
    int IDC_PWR = 97;

    int IDC_CHOP = 98; // Unary operators must be between IDC_CHOP and IDC_EQU
    int IDC_UNARYFIRST = IDC_CHOP;
    int IDC_ROL = 99;
    int IDC_ROR = 100;
    int IDC_COM = 101;

    int IDC_SIN = 102;
    int IDC_COS = 103;
    int IDC_TAN = 104;

    int IDC_SINH = 105;
    int IDC_COSH = 106;
    int IDC_TANH = 107;

    int IDC_LN = 108;
    int IDC_LOG = 109;
    int IDC_SQRT = 110;
    int IDC_SQR = 111;
    int IDC_CUB = 112;
    int IDC_FAC = 113;
    int IDC_REC = 114;
    int IDC_DMS = 115;
    int IDC_CUBEROOT = 116; // x ^ 1/3
    int IDC_POW10 = 117;    // 10 ^ x
    int IDC_PERCENT = 118;
    int IDC_UNARYLAST = IDC_PERCENT;

    int IDC_FE = 119;
    int IDC_PI = 120;
    int IDC_EQU = 121;

    int IDC_MCLEAR = 122;
    int IDC_RECALL = 123;
    int IDC_STORE = 124;
    int IDC_MPLUS = 125;
    int IDC_MMINUS = 126;

    int IDC_EXP = 127;

    int IDC_OPENP = 128;
    int IDC_CLOSEP = 129;

    int IDC_0 = 130; // The controls for 0 through F must be consecutive and in order
    int IDC_1 = 131;
    int IDC_2 = 132;
    int IDC_3 = 133;
    int IDC_4 = 134;
    int IDC_5 = 135;
    int IDC_6 = 136;
    int IDC_7 = 137;
    int IDC_8 = 138;
    int IDC_9 = 139;
    int IDC_A = 140;
    int IDC_B = 141;
    int IDC_C = 142;
    int IDC_D = 143;
    int IDC_E = 144;
    int IDC_F = 145; // this is last control ID which must match the string table
    int IDC_INV = 146;
    int IDC_SET_RESULT = 147;

    int IDC_STRING_MAPPED_VALUES = 400;
    int IDC_UNARYEXTENDEDFIRST = IDC_STRING_MAPPED_VALUES;
    int IDC_SEC = 400; // Secant
    // 401 reserved for inverse
    int IDC_CSC = 402; // Cosecant
    // 403 reserved for inverse
    int IDC_COT = 404; // Cotangent
// 405 reserved for inverse

    int IDC_SECH = 406; // Hyperbolic Secant
    // 407 reserved for inverse
    int IDC_CSCH = 408; // Hyperbolic Cosecant
    // 409 reserved for inverse
    int IDC_COTH = 410; // Hyperbolic Cotangent
// 411 reserved for inverse

    int IDC_POW2 = 412;  // 2 ^ x
    int IDC_ABS = 413;   // Absolute Value
    int IDC_FLOOR = 414; // Floor
    int IDC_CEIL = 415;  // Ceiling

    int IDC_ROLC = 416; // Rotate Left Circular
    int IDC_RORC = 417; // Rotate Right Circular

    int IDC_UNARYEXTENDEDLAST = IDC_RORC;

    int IDC_LASTCONTROL = IDC_CEIL;

    int IDC_BINARYEXTENDEDFIRST = 500;
    int IDC_LOGBASEY = 500; // logy(x)
    int IDC_NAND = 501;     // Nand
    int IDC_NOR = 502;      // Nor

    int IDC_RSHFL = 505; // Right Shift Logical
    int IDC_BINARYEXTENDEDLAST = IDC_RSHFL;

    int IDC_RAND = 600;  // Random
    int IDC_EULER = 601; // e Constant

    int IDC_BINEDITSTART = 700;
    int IDC_BINPOS0 = 700;
    int IDC_BINPOS1 = 701;
    int IDC_BINPOS2 = 702;
    int IDC_BINPOS3 = 703;
    int IDC_BINPOS4 = 704;
    int IDC_BINPOS5 = 705;
    int IDC_BINPOS6 = 706;
    int IDC_BINPOS7 = 707;
    int IDC_BINPOS8 = 708;
    int IDC_BINPOS9 = 709;
    int IDC_BINPOS10 = 710;
    int IDC_BINPOS11 = 711;
    int IDC_BINPOS12 = 712;
    int IDC_BINPOS13 = 713;
    int IDC_BINPOS14 = 714;
    int IDC_BINPOS15 = 715;
    int IDC_BINPOS16 = 716;
    int IDC_BINPOS17 = 717;
    int IDC_BINPOS18 = 718;
    int IDC_BINPOS19 = 719;
    int IDC_BINPOS20 = 720;
    int IDC_BINPOS21 = 721;
    int IDC_BINPOS22 = 722;
    int IDC_BINPOS23 = 723;
    int IDC_BINPOS24 = 724;
    int IDC_BINPOS25 = 725;
    int IDC_BINPOS26 = 726;
    int IDC_BINPOS27 = 727;
    int IDC_BINPOS28 = 728;
    int IDC_BINPOS29 = 729;
    int IDC_BINPOS30 = 730;
    int IDC_BINPOS31 = 731;
    int IDC_BINPOS32 = 732;
    int IDC_BINPOS33 = 733;
    int IDC_BINPOS34 = 734;
    int IDC_BINPOS35 = 735;
    int IDC_BINPOS36 = 736;
    int IDC_BINPOS37 = 737;
    int IDC_BINPOS38 = 738;
    int IDC_BINPOS39 = 739;
    int IDC_BINPOS40 = 740;
    int IDC_BINPOS41 = 741;
    int IDC_BINPOS42 = 742;
    int IDC_BINPOS43 = 743;
    int IDC_BINPOS44 = 744;
    int IDC_BINPOS45 = 745;
    int IDC_BINPOS46 = 746;
    int IDC_BINPOS47 = 747;
    int IDC_BINPOS48 = 748;
    int IDC_BINPOS49 = 749;
    int IDC_BINPOS50 = 750;
    int IDC_BINPOS51 = 751;
    int IDC_BINPOS52 = 752;
    int IDC_BINPOS53 = 753;
    int IDC_BINPOS54 = 754;
    int IDC_BINPOS55 = 755;
    int IDC_BINPOS56 = 756;
    int IDC_BINPOS57 = 757;
    int IDC_BINPOS58 = 758;
    int IDC_BINPOS59 = 759;
    int IDC_BINPOS60 = 760;
    int IDC_BINPOS61 = 761;
    int IDC_BINPOS62 = 762;
    int IDC_BINPOS63 = 763;
    int IDC_BINEDITEND = 763;

    // The strings in the following range IDS_ENGINESTR_FIRST ... IDS_ENGINESTR_MAX are strings allocated in the
    // resource for the purpose internal to Engine and cant be used by the clients
    int IDS_ENGINESTR_FIRST = 0;
    int IDS_ENGINESTR_MAX = 200;

}
