package com.otpone.otpone.util.data_mgt_framework.list;

import com.otpone.otpone.util.data_mgt_framework.Criteria;
import com.otpone.otpone.util.data_mgt_framework.IllegalMappingException;
import com.otpone.otpone.util.data_mgt_framework.collection.CollatableRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by DJ on 6/1/2017.
 */

public interface SequentialRepository extends CollatableRepository {

    <T> boolean add(String typeTag, Collection<T> customCollectionImpl);

    @Override
    List<String> getTags();

    Map<String, List<?>> getAll();

    Map<String, List<?>> removeAll();
}
