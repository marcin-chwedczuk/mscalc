package mscalc.engine;

import mscalc.engine.ratpack.RatPack;
import mscalc.engine.ratpack.RatPack.AngleType;
import mscalc.engine.resource.JavaBundleResourceProvider;
import mscalc.engine.resource.ResourceProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;
import static mscalc.engine.Commands.*;
import static mscalc.engine.EngineStrings.*;

public class CCalcEngine {
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
    };

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

    static void loadEngineStrings(ResourceProvider resourceProvider)
    {
        for (var sid : g_sids)
        {
            var locString = resourceProvider.getCEngineString(sid);
            if (!locString.isEmpty())
            {
                System.out.printf("Loaded: %s -> %s%n", sid, locString);
                engineStrings.put(sid, locString);
            }
        }
    }

    // returns the ptr to string representing the operator. Mostly same as the button, but few special cases for x^y etc.
    private static String GetString(int ids)
    {
        return engineStrings.get(Integer.toString(ids));
    }

    private static String GetString(String ids)
    {
        return engineStrings.get(ids);
    }

    public static String OpCodeToBinaryString(int nOpCode, boolean isIntegerMode)
    {
        // Try to lookup the ID in the UFNE table
        String ids = "";
        var fne = operatorStringTable.get(nOpCode);
        if (fne != null)
        {
            if (isIntegerMode && !fne.programmerModeString().isEmpty())
            {
                ids = fne.programmerModeString();
            }
            else
            {
                ids = fne.degreeString();
            }
        }

        if (!ids.isEmpty())
        {
            return GetString(ids);
        }

        // If we didn't find an ID in the table, use the op code.
        return OpCodeToString(nOpCode);
    }

    private static int IdStrFromCmdId(int id)
    {
        return id - IDC_FIRSTCONTROL + IDS_ENGINESTR_FIRST;
    }

    public static String OpCodeToString(int nOpCode) {
        return GetString(IdStrFromCmdId(nOpCode));
    }

    public static String OpCodeToUnaryString(int nOpCode, boolean fInv, AngleType angletype)
    {
        // Try to lookup the ID in the UFNE table
        String ids = "";
        var pair = operatorStringTable.get(nOpCode);
        if (pair != null)
        {
            FunctionNameElement element = pair;
            if (!element.hasAngleStrings() || AngleType.Degrees == angletype)
            {
                if (fInv)
                {
                    ids = element.inverseDegreeString;
                }

                if (ids.isEmpty())
                {
                    ids = element.degreeString;
                }
            }
            else if (AngleType.Radians == angletype)
            {
                if (fInv)
                {
                    ids = element.inverseRadString;
                }
                if (ids.isEmpty())
                {
                    ids = element.radString;
                }
            }
            else if (AngleType.Gradians == angletype)
            {
                if (fInv)
                {
                    ids = element.inverseGradString;
                }
                if (ids.isEmpty())
                {
                    ids = element.gradString;
                }
            }
        }

        if (!ids.isEmpty())
        {
            return GetString(ids);
        }

        // If we didn't find an ID in the table, use the op code.
        return OpCodeToString(nOpCode);
    }

}
