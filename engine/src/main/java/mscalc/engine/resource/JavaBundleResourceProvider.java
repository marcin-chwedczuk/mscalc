package mscalc.engine.resource;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;

public class JavaBundleResourceProvider implements ResourceProvider {
    private final ResourceBundle bundle;
    private final DecimalFormatSymbols symbols;

    public JavaBundleResourceProvider() {
        var locale = Locale.US;

        this.bundle = ResourceBundle.getBundle("mscalc.engine.calc-engine", locale);
        this.symbols = new DecimalFormatSymbols(locale);
    }

    @Override
    public String getCEngineString(String id) {
        if (id.equals("sDecimal")) {
            return Character.toString(symbols.getDecimalSeparator());
        }

        if (id.equals("sThousand")) {
            return Character.toString(symbols.getGroupingSeparator());
        }

        if (id.equals("sGrouping"))
        {
            // The following groupings are the onces that CalcEngine supports.
            //   0;0             0x000          - no grouping
            //   3;0             0x003          - group every 3 digits
            //   3;2;0           0x023          - group 1st 3 and then every 2 digits
            //   4;0             0x004          - group every 4 digits
            //   5;3;2;0         0x235          - group 5, then 3, then every 2
            return "3;0"; // Currently hardcoded, TODO: Fix it, check LocalizationSettings in original Calc
        }

        return bundle.containsKey(id) ? bundle.getString(id) : "";
    }
}
