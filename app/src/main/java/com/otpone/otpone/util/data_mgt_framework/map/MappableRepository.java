package com.otpone.otpone.util.data_mgt_framework.map;

import com.otpone.otpone.util.data_mgt_framework.Criteria;
import com.otpone.otpone.util.data_mgt_framework.IllegalMappingException;
import com.otpone.otpone.util.data_mgt_framework.KeyMaker;
import com.otpone.otpone.util.data_mgt_framework.collection.CollatableRepository;

import java.util.Collection;
import java.util.Map;

/**
 * Created by DJ on 5/29/2017.
 */

public interface MappableRepository {

    public static final int MATCH_KEYS = 0x01;
    public static final int MATCH_VALUES = 0x02;
    public static final int MATCH_NONE = 0x03;

    <K, V> boolean addMappable(String typeTag, KeyMaker<V, K> keyMaker, int matchPolicy, V value);
    <K, V> void addMappable(String typeTag, KeyMaker<V, K> keyMaker);
    <V> boolean addMappable(String typeTag, int matchPolicy, V value);
    <V> boolean addMappableAll(String typeTag, int matchPolicy, Collection<V> value);

    <K, V> Map<K, V> getMappable(String typeTag, Criteria<V> criteria, Map<K, V> customMapImpl);
    <K, V> V getMappable(String typeTag, K key);
    <K, V> Map<K, V> getMappableAll(String typeTag, Map<K, V> customMapImpl);
    <K, V> Map<K, V> getMappableAll(String typeTag, Collection<K> keys, Map<K, V> customMapImpl);
    Map<String, Map<?, ?>> getMappableAll();

    <K, V> V setMappable(String typeTag, K key, V value);

    <K, V> Map<K, V> removeMappable(String typeTag);
    <K, V> V removeMappable(String typeTag, K key);
    <K, V> Map<K, V> removeMappableAll(String typeTag, Collection<K> keys, Map<K, V> customMapImpl);
    <K, V> Map<K, V> removeMappableAll(String typeTag, int matchPolicy, Map<K, V> customMapImpl, Collection<V> value);
    <K, V> Map<K, V> removeMappableAll(String typeTag, Criteria<V> removalCriteria, Map<K, V> customMapImpl);
    Map<String, Map<?, ?>> removeMappableAll();

    Collection<String> getTags();

    void clear(String typeTag);
    void clearAll();

    <U, V> Map<U, V> map(String typeTag1, String typeTag2, Map<U, V> customMapImpl) throws IllegalMappingException;
    <U, V> Map<U, V> map(String typeTag1, Criteria<U> type1Criteria, String typeTag2, Criteria<V> type2Criteria, Map<U, V> customMapImpl) throws IllegalMappingException;
}
