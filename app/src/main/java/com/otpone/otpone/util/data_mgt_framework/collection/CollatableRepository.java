package com.otpone.otpone.util.data_mgt_framework.collection;

import com.otpone.otpone.util.data_mgt_framework.Criteria;
import com.otpone.otpone.util.data_mgt_framework.IllegalMappingException;

import org.javatuples.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by DJ on 5/28/2017.
 */

public interface CollatableRepository {

    <T> boolean add(String typeTag, T object);
    <T> boolean addAll(String typeTag, Collection<T> object);

    <T> Collection<T> get(String typeTag, Collection<T> customCollectionImpl);
    <T> Collection<T> get(String typeTag, Criteria<T> criteria, Collection<T> customCollectionImpl);

    <T> boolean remove(String typeTag, T object);
    <T> Collection<T> remove(String typeTag);
    <T> Collection<T> remove(String typeTag, Criteria<T> removalCriteria, Collection<T> customCollectionImpl);
    <T> boolean removeAll(String typeTag, Collection<T> object);

    void clear(String typeTag);
    void clearAll();

    int size();

    Collection<String> getTags();

    <U, V> Map<U, V> map(String typeTag1, String typeTag2, Map<U, V> customMapImpl) throws IllegalMappingException;
    <U, V> Map<U, V> map(String typeTag1, Criteria<U> type1Criteria, String typeTag2, Criteria<V> type2Criteria, Map<U, V> customMapImpl) throws IllegalMappingException;
}
