package com.otpone.otpone.util.data_mgt_framework.map;

import com.otpone.otpone.util.data_mgt_framework.IllegalMappingException;
import com.otpone.otpone.util.data_mgt_framework.KeyMaker;
import com.otpone.otpone.util.data_mgt_framework.map.MappableRepository;

import java.util.Collection;
import java.util.Map;

/**
 * Created by DJ on 5/29/2017.
 */

public interface RichMappableRepository extends MappableRepository {


    void addMappable(Map<String, KeyMaker<?, ?>> typeTagsAndKeyMakers);
    <K, V> Collection<V> addMappable(String typeTag, KeyMaker<V, K> keyMaker, int matchPolicy, V... value);
    <K, V> Collection<V> addMappable(String typeTag, KeyMaker<V, K> keyMaker, int matchPolicy, Collection<V> value);
    <K, V> Collection<V> addMappable(String typeTag, KeyMaker<V, K> keyMaker, int matchPolicy, Collection<V> customCollectionImpl, V... value);
    <K, V> Collection<V> addMappable(String typeTag, KeyMaker<V, K> keyMaker, int matchPolicy, Collection<V> customCollectionImpl, Collection<V> value);

    // Practical usefulness of this method is reduced by the fact that we are not asking for a custom Map
    // implementation for every type. These multiple implementations make this method suitable for
    // specialized cases and can always be introduced separately should one require.

    //Map<String, Collection<?>> addMappable(Map<Pair<String, KeyMaker<?, ?>>, Collection<?>> typeTagsKeysAndValues, int matchPolicy);

    <T, K> Collection<T> removeMappable(String typeTag, K... keys);
    <T, K> Collection<T> removeMappable(String typeTag, Collection<K> keys);
    <T, K> Collection<T> removeMappable(String typeTag, Collection<T> customCollectionImpl, K... keys);
    <T, K> Collection<T> removeMappable(String typeTag, Collection<T> customCollectionImpl, Collection<K> keys);

    <K, V> Map<K, V> getMappable(String typeTag, Map<K, V> customMapImpl, K... keys);
    <K, V> Map<K, V> getMappable(String typeTag, Map<K, V> customMapImpl, Collection<K> keys);

    // Practical usefulness of this method is reduced by the fact that we are not asking for a custom Map
    // implementation for every type. These multiple implementations make this method suitable for
    // specialized cases and can always be introduced separately should one require.

    // As an idea, impl of these methods may take a map provider that would map the type tag to the
    // the required map implementation. There of course, may be other techniques of getting around this
    // problem.

    //Map<String, Map<?, ?>> getMappable(Map<String, Criteria<?>> tagsAndCriteria);
    //Map<String, Map<?, ?>> getMappable(Collection<String> tags);

    <U, V, K> Map<U, V> map(String typeTag1, String typeTag2, Map<U, V> customMapImpl, K... keys) throws IllegalMappingException;
}
