package com.otpone.otpone.model.util;

/**
 * Created by DJ on 5/16/2017.
 */

public interface Generator<Outcome, Input> {

    Outcome next(Input input);
}
