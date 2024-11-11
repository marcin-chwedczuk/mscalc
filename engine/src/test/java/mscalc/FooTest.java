package mscalc;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import mscalc.engine.ratpack.Conv;
import mscalc.engine.ratpack.Exp;
import mscalc.engine.ratpack.RatPack;
import mscalc.engine.ratpack.RatPack.RAT;
import mscalc.engine.ratpack.Support;
import org.junit.jupiter.api.Test;

import static mscalc.engine.ratpack.Conv.createrat;
import static mscalc.engine.ratpack.RatPack.DUPRAT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FooTest {
    @Test
    public void foo() {
        uint BASE_10 = uint.of(10);
        Support.ChangeConstants(BASE_10, 20);

        RAT t = createrat();
        for (int i = 0; i < 10; i++) {
            t = Conv.StringToRat(false, "0." + Double.toString(i), false, "0", BASE_10, 20);

            Ptr<RAT> res = new Ptr<>(DUPRAT(t));
            Exp.exprat(res, BASE_10, 20);

            System.out.println(Conv.RatToString(res, RatPack.NumberFormat.Float, BASE_10, 20));
        }
    }
}
