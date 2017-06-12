package com.otpone.otpone.util.data_mgt_framework.impl;

import java.util.List;

/**
 * Created by DJ on 6/11/2017.
 */

public interface MultiGenerator<T> extends Generator<T> {

    List<T> next(int count);
}
