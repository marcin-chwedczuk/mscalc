package mscalc;

import mscalc.cpp.Ptr;
import mscalc.cpp.uint;
import mscalc.ratpack.Conv;
import mscalc.ratpack.Exp;
import mscalc.ratpack.RatPack;
import mscalc.ratpack.RatPack.RAT;
import mscalc.ratpack.Support;
import org.junit.jupiter.api.Test;

import static mscalc.ratpack.Conv.createrat;
import static mscalc.ratpack.RatPack.BASEX;
import static mscalc.ratpack.RatPack.DUPRAT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FooTest {
    @Test
    public void foo() {
        uint BASE_10 = uint.of(10);
        Support.ChangeConstants(BASE_10, 20);

        RAT t = createrat();
        for (int i = 0; i < 10; i++) {
            t = Conv.StringToRat(false, Double.toString(i), false, "0", BASE_10, 20);

            Ptr<RAT> res = new Ptr<>(DUPRAT(t));
            Exp.exprat(res, BASE_10, 20);

            System.out.println(Conv.RatToString(res, RatPack.NumberFormat.Float, BASE_10, 20));
        }
    }
}
