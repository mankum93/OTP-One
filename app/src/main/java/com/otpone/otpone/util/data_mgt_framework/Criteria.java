package com.otpone.otpone.util.data_mgt_framework;

/**
 * Created by DJ on 5/28/2017.
 */

public interface Criteria<T> {

    boolean match(T type);
}
