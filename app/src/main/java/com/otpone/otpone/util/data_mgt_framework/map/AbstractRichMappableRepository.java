package com.otpone.otpone.util.data_mgt_framework.map;

import com.otpone.otpone.util.data_mgt_framework.KeyMaker;

import java.util.Collection;
import java.util.Map;

/**
 * Created by DJ on 6/1/2017.
 */

public abstract class AbstractRichMappableRepository extends AbstractMappedRepository implements RichMappableRepository {

    public AbstractRichMappableRepository(Map<String, KeyMaker<?, ?>> typeTagsAndKeyMakers, Map<String, Map<?, ?>> typeTagsAndValues) {
        super(typeTagsAndKeyMakers, typeTagsAndValues);
    }

    @Override
    public <K, V> Collection<V> addMappable(String typeTag, KeyMaker<V, K> keyMaker, int matchPolicy, V... value) {
        return addMappable(typeTag, keyMaker, matchPolicy, (Collection<V>) null, value);
    }

    @Override
    public <K, V> Collection<V> addMappable(String typeTag, KeyMaker<V, K> keyMaker, int matchPolicy, Collection<V> value) {
        return addMappable(typeTag, keyMaker, matchPolicy, (Collection<V>) null, value);
    }

    @Override
    public <T, K> Collection<T> removeMappable(String typeTag, K... keys) {
        return removeMappable(typeTag, (Collection<T>) null, keys);
    }

    @Override
    public <T, K> Collection<T> removeMappable(String typeTag, Collection<K> keys) {
        return removeMappable(typeTag, (Collection<T>) null, keys);
    }
}
