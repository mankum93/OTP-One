package com.otpone.otpone.util;

import android.support.v4.util.Pair;

import com.otpone.otpone.model.Contact;
import com.otpone.otpone.model.OTPMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DJ on 6/14/2017.
 */

public class ListUtil {


    public static <T, U> List<Pair<T, U>> toListOfPairs(List<T> tList, U u){

        List<Pair<T, U>> result = new ArrayList<>(tList.size());

        for(int i = 0; i < tList.size(); i++){
            result.add(new Pair<>(tList.get(i), u));
        }

        return result;
    }
}
