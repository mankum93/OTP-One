package com.otpone.otpone.util.data_mgt_framework.impl.collection.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by DJ on 5/30/2017.
 */

public class ArrayListRepository extends AbstractListRepository {

    public ArrayListRepository(Map<String, List<?>> typeTagsAndTypeValues) {
        super(typeTagsAndTypeValues);
    }

    @Override
    protected <P, T> List<T> getNewContainer(String typeTag, int size, P...params) {
        return new ArrayList<>(size >= 10 ? size : 10);
    }
}
