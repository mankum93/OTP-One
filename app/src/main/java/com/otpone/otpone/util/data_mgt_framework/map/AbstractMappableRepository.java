package com.otpone.otpone.util.data_mgt_framework.map;

import com.otpone.otpone.util.data_mgt_framework.KeyMaker;

import java.util.Collection;

/**
 * Created by DJ on 6/7/2017.
 */

public abstract class AbstractMappableRepository implements MappableRepository {

    protected abstract <T, K> KeyMaker<T, K> getKeyMaker(String typeTag);

    protected abstract <T, K> KeyMaker<T, K> setKeyMaker(String typeTag, KeyMaker<T, K> newKeyMaker);

    protected abstract <K> Collection<K> getKeys(String typeTag);

    protected abstract boolean isTypeRegistered(String typeTag);

    protected abstract int getRepositorySize();
}
