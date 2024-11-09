package mscalc.ratpack;

import mscalc.ratpack.RatPack.NUMBER;

import java.util.concurrent.atomic.AtomicBoolean;

import static mscalc.ratpack.Conv.i32tonum;
import static mscalc.ratpack.RatPack.BASEX;

public interface Support {
    AtomicBoolean g_ftrueinfinite = new AtomicBoolean(false); // Set to true if you don't want
    // chopping internally
    // precision used internally

    NUMBER num_two = i32tonum(2, BASEX);
}
