package com.otpone.otpone.util.data_mgt_framework.collection;

import com.otpone.otpone.util.data_mgt_framework.Criteria;

import java.util.Collection;
import java.util.Map;

/**
 * Created by DJ on 5/29/2017.
 */

public interface RichCollatableRepository extends CollatableRepository {

    <T> Collection<T> add(String typeTag, Collection<T> customCollectionImpl, T... objects);
    <T> Collection<T> add(String typeTag, Collection<T> customCollectionImpl, Collection<T> objects);
    <T> Collection<T> add(String typeTag, Collection<T> customCollectionImpl);
    <T> Collection<T> add(String typeTag, Criteria<T> criteria, Collection<T> customCollectionImpl);

    <T> Collection<T> remove(String typeTag, Collection<T> customCollectionImpl, Collection<T> objects);
    <T> Collection<T> remove(String typeTag, Collection<T> customCollectionImpl, T... objects);
    <T> Collection<T> remove(String typeTag, T object, Collection<T> customCollectionImpl);
    <T> Collection<T> remove(String typeTag, Collection<T> customCollectionImpl);
    <T> Collection<T> remove(String typeTag, Criteria<T> removalCriteria, Collection<T> customCollectionImpl);

    void clear(String... typeTags);
    void clear(Collection<String> typeTags);
}
