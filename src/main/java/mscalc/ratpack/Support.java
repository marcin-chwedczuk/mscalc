package mscalc.ratpack;

import mscalc.ratpack.RatPack.NUMBER;

import static mscalc.ratpack.Conv.i32tonum;
import static mscalc.ratpack.RatPack.BASEX;

public interface Support {
    NUMBER num_two = i32tonum(2, BASEX);
}
