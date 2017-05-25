package com.otpone.otpone.model.util;

import java.util.Random;

/**
 * Created by DJ on 5/16/2017.
 */

/**
 * A Simple Random Generator built around java.util.Random's
 * random integer generation approach.
 */
public class SimpleSixDigitRandomIntGenerator implements Generator<Integer, Void> {

    private static final int RANGE_MIN = 100000;
    private static final int RANGE_MAX = 999999;

    private final Random random;

    public SimpleSixDigitRandomIntGenerator(long seed){
        random = new Random(seed);
    }

    public SimpleSixDigitRandomIntGenerator(){
        random = new Random();
    }

    @Override
    public Integer next(Void aVoid) {
        return random.nextInt(RANGE_MAX - RANGE_MIN + 1) + RANGE_MIN;
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }
}
