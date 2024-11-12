package mscalc.engine.ratpack;

import mscalc.engine.cpp.Ptr;
import mscalc.engine.cpp.uint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogicTests {
    private static final uint RADIX_2 = uint.of(2);
    private static final uint RADIX_10 = uint.of(10);
    private static final int PRECISION = 64;

    @BeforeAll
    public static void beforeAll() {
        Support.ChangeConstants(RADIX_2, PRECISION);
    }

    @Test
    public void lshrat_non_zero_shift() {
        String input = "1111000011110000";
        String actual = lsh(input, 6);
        assertEquals("1111000011110000000000", actual);
    }

    @Test
    public void lshrat_zero_shift() {
        String input = "1111000011110000";
        String actual = lsh(input, 0);
        assertEquals("1111000011110000", actual);
    }

    private static String lsh(String input, int shift) {
        Ptr<RatPack.RAT> xRat = new Ptr<>(
                Conv.StringToRat(false, input, false, "0", RADIX_2, PRECISION)
        );

        RatPack.RAT shiftRat = Conv.StringToRat(false, Integer.toString(shift), false, "0", RADIX_10, PRECISION);

        Logic.lshrat(xRat, shiftRat, RADIX_2, PRECISION);

        return Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_2, PRECISION);
    }

    @Test
    public void rshrat_non_zero_shift() {
        String input = "1111000011110000";
        String actual = rsh(input, 6);
        assertEquals("1111000011.11", actual);
    }

    @Test
    public void rshrat_zero_shift() {
        String input = "1111000011110000";
        String actual = rsh(input, 0);
        assertEquals("1111000011110000", actual);
    }

    private static String rsh(String input, int shift) {
        Ptr<RatPack.RAT> xRat = new Ptr<>(
                Conv.StringToRat(false, input, false, "0", RADIX_2, PRECISION)
        );

        RatPack.RAT shiftRat = Conv.StringToRat(false, Integer.toString(shift), false, "0", RADIX_10, PRECISION);

        Logic.rshrat(xRat, shiftRat, RADIX_2, PRECISION);

        return Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_2, PRECISION);
    }

    @Test
    public void remrat_works() {
        Ptr<RatPack.RAT> xRat = new Ptr<>(
                Conv.StringToRat(false, "137", false, "0", RADIX_10, PRECISION)
        );

        RatPack.RAT yRat = Conv.StringToRat(false, "27", false, "0", RADIX_10, PRECISION);

        Logic.remrat(xRat, yRat);

        String result = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_10, PRECISION);

        assertEquals("2", result);
    }

    // TODO: Add more tests for remrat & modrat

    @Test
    public void modrat_works() {
        Ptr<RatPack.RAT> xRat = new Ptr<>(
                Conv.StringToRat(false, "137", false, "0", RADIX_10, PRECISION)
        );

        RatPack.RAT yRat = Conv.StringToRat(true, "27", false, "0", RADIX_10, PRECISION);

        Logic.modrat(xRat, yRat);

        String result = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_10, PRECISION);

        assertEquals("-25", result);
    }

    @Test
    public void bitand_works() {
        Ptr<RatPack.RAT> xRat = new Ptr<>(
                Conv.StringToRat(false, "1111000011110000", false, "0", RADIX_2, PRECISION)
        );

        RatPack.RAT yRat = Conv.StringToRat(false, "1111111100000000", false, "0", RADIX_2, PRECISION);

        Logic.andrat(xRat, yRat, RADIX_2, PRECISION);

        String result = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_2, PRECISION);

        assertEquals("1111000000000000", result);
    }

    @Test
    public void bitor_works() {
        Ptr<RatPack.RAT> xRat = new Ptr<>(
                Conv.StringToRat(false, "1111000011110000", false, "0", RADIX_2, PRECISION)
        );

        RatPack.RAT yRat = Conv.StringToRat(false, "1111111100000000", false, "0", RADIX_2, PRECISION);

        Logic.orrat(xRat, yRat, RADIX_2, PRECISION);

        String result = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_2, PRECISION);

        assertEquals("1111111111110000", result);
    }

    @Test
    public void bitxor_works() {
        Ptr<RatPack.RAT> xRat = new Ptr<>(
                Conv.StringToRat(false, "1111000011110000", false, "0", RADIX_2, PRECISION)
        );

        RatPack.RAT yRat = Conv.StringToRat(false, "1111111100000000", false, "0", RADIX_2, PRECISION);

        Logic.xorrat(xRat, yRat, RADIX_2, PRECISION);

        String result = Conv.RatToString(xRat, RatPack.NumberFormat.Float, RADIX_2, PRECISION);

        assertEquals("111111110000", result);
    }
}
