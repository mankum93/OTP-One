package com.otpone.otpone.util.data_mgt_framework;

import com.otpone.otpone.util.data_mgt_framework.collection.CollatableRepository;

/**
 * Created by DJ on 5/29/2017.
 */

public interface CacheableRepository extends CollatableRepository {

    <T> T getCached(String tag);
    <T> T putCached(String tag, T object);
}
