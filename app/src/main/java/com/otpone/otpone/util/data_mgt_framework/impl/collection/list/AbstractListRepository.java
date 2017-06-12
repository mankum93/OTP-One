package com.otpone.otpone.util.data_mgt_framework.impl.collection.list;

import com.otpone.otpone.util.data_mgt_framework.Criteria;
import com.otpone.otpone.util.data_mgt_framework.IllegalMappingException;
import com.otpone.otpone.util.data_mgt_framework.list.SequentialRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by DJ on 6/3/2017.
 */

public abstract class AbstractListRepository implements SequentialRepository {

    protected Map<String, List<?>> typeTagsAndTypeValues;

    public AbstractListRepository(Map<String, List<?>> typeTagsAndTypeValues) {
        this.typeTagsAndTypeValues = (typeTagsAndTypeValues == null) ? new HashMap<String, List<?>>() : typeTagsAndTypeValues;
    }

    protected abstract <P, T> List<T> getNewContainer(String typeTag, int size, P...params);

    protected boolean isTypeRegistered(String typeTag){
        if(typeTag == null){
            return false;
        }
        if(typeTagsAndTypeValues.containsKey(typeTag)){
            return true;
        }
        return false;
    }

    @Override
    public <T> boolean add(String typeTag, T object) {

        // Check if the type is already registered.
        if(!isTypeRegistered(typeTag)){
            List<T> typeValues;

            typeValues = getNewContainer(typeTag, 1);
            // Add if the value is valid.
            if(object != null){
                typeValues.add(object);
            }
            typeTagsAndTypeValues.put(typeTag, typeValues);
            return true;
        }
        else{
            List<T> values = (List<T>) typeTagsAndTypeValues.get(typeTag);

            return values.add(object);
        }
    }

    @Override
    public <T> boolean addAll(String typeTag, Collection<T> objects) {
        // Check if the type is already registered.
        if(!isTypeRegistered(typeTag)){
            List<T> typeValues;
            // Add if the value is valid.
            if(objects != null){
                typeValues = getNewContainer(typeTag, 1);
                typeValues.addAll(objects);
                typeTagsAndTypeValues.put(typeTag, typeValues);

                return true;
            }
            return false;
        }
        else{
            List<T> values = (List<T>) typeTagsAndTypeValues.get(typeTag);

            return values.addAll(objects);
        }
    }

    @Override
    public <T> boolean add(String typeTag, Collection<T> customCollectionImpl) {

        if(!isTypeRegistered(typeTag)){
            List<T> typeValues;

            if(!(customCollectionImpl instanceof List)){
                typeValues = getNewContainer(typeTag, 1);
            }
            else{
                typeValues = (List<T>)customCollectionImpl;
            }
            typeTagsAndTypeValues.put(typeTag, typeValues);

            return true;
        }

        return false;
    }

    @Override
    public <T> Collection<T> get(String typeTag, Collection<T> customCollectionImpl) {

        if(isTypeRegistered(typeTag)){
            Collection<T> resultHolder;

            if(customCollectionImpl == null){
                resultHolder = getNewContainer(typeTag, 1);
            }
            else{
                resultHolder = customCollectionImpl;
            }
            // Retrieve the values for the tag.
            List<T> values = (List<T>) typeTagsAndTypeValues.get(typeTag);

            resultHolder.addAll(values);

            return resultHolder;
        }
        return null;
    }

    @Override
    public List<String> getTags() {

        Set<String> tagSet = typeTagsAndTypeValues.keySet();
        if(!tagSet.isEmpty()){

            List<String> result = getNewContainer(null, 1);
            result.addAll(tagSet);

            return result;
        }

        return null;
    }

    @Override
    public Map<String, List<?>> getAll() {
        return !typeTagsAndTypeValues.isEmpty() ? typeTagsAndTypeValues : typeTagsAndTypeValues;
    }

    @Override
    public <T> Collection<T> get(String typeTag, Criteria<T> criteria, Collection<T> customCollectionImpl) {

        if(isTypeRegistered(typeTag)){

            if(customCollectionImpl == null){
                customCollectionImpl = getNewContainer(typeTag, 1);
            }

            ListIterator<T> iter = (ListIterator<T>)typeTagsAndTypeValues.get(typeTag).listIterator();
            T ele;
            while (iter.hasNext()){
                ele = iter.next();
                if(criteria.match(ele)){
                    customCollectionImpl.add(ele);
                }
            }

            return customCollectionImpl.isEmpty() ? null : customCollectionImpl;
        }

        return null;
    }

    @Override
    public Map<String, List<?>> removeAll() {

        Map<String, List<?>> result = typeTagsAndTypeValues;
        typeTagsAndTypeValues = new HashMap<>();

        return result;
    }

    @Override
    public <T> boolean remove(String typeTag, T object) {

        if(isTypeRegistered(typeTag)){

            // Retrieve the values for the tag.
            List<T> values = (List<T>) typeTagsAndTypeValues.get(typeTag);

            return values.remove(object);
        }
        return false;
    }

    @Override
    public <T> List<T> remove(String typeTag) {

        if(isTypeRegistered(typeTag)){

            return (List<T>) typeTagsAndTypeValues.remove(typeTag);
        }
        return null;
    }

    @Override
    public <T> Collection<T> remove(String typeTag, Criteria<T> removalCriteria, Collection<T> customCollectionImpl) {
        if(isTypeRegistered(typeTag)){

            if(customCollectionImpl == null){
                customCollectionImpl = getNewContainer(typeTag, 1);
            }

            ListIterator<T> iter = (ListIterator<T>)typeTagsAndTypeValues.get(typeTag).listIterator();
            T ele;
            while (iter.hasNext()){
                ele = iter.next();
                if(removalCriteria.match(ele)){
                    customCollectionImpl.add(ele);
                    iter.remove();
                }
            }

            return customCollectionImpl.isEmpty() ? null : customCollectionImpl;
        }

        return null;
    }

    @Override
    public <T> boolean removeAll(String typeTag, Collection<T> object) {

        if(isTypeRegistered(typeTag)){

            List<T> values = (List<T>) typeTagsAndTypeValues.get(typeTag);

            return values.removeAll(object);
        }
        return false;
    }

    @Override
    public void clear(String typeTag) {

        if(isTypeRegistered(typeTag)){
            List<?> values = typeTagsAndTypeValues.get(typeTag);
            values.clear();
        }
    }

    @Override
    public void clearAll() {

        for(String typeTag : typeTagsAndTypeValues.keySet()){
            clear(typeTag);
        }
    }

    @Override
    public int size() {
        return typeTagsAndTypeValues != null ? typeTagsAndTypeValues.size() : -1;
    }

    @Override
    public <U, V> Map<U, V> map(String typeTag1, String typeTag2, Map<U, V> customMapImpl) throws IllegalMappingException {
        if(!isTypeRegistered(typeTag1) || !isTypeRegistered(typeTag2)){
            return null;
        }

        Map<U, V> mapping = (customMapImpl == null) ? new HashMap<U, V>() : customMapImpl;
        Collection<?> valuesSet1, valuesSet2;
        // Gather values for both tags.
        Iterator<U> iter1 = (Iterator<U>)(valuesSet1 = typeTagsAndTypeValues.get(typeTag1)).iterator();
        Iterator<V> iter2 = (Iterator<V>)(valuesSet2 = typeTagsAndTypeValues.get(typeTag2)).iterator();

        if(valuesSet1.size() != valuesSet2.size()){
            throw new IllegalMappingException("Collections to be mapped have different lengths.");
        }

        while(iter1.hasNext() && iter2.hasNext()){
            mapping.put(iter1.next(), iter2.next());
        }
        return mapping;
    }

    @Override
    public <U, V> Map<U, V> map(String typeTag1, Criteria<U> type1Criteria, String typeTag2, Criteria<V> type2Criteria, Map<U, V> customMapImpl) throws IllegalMappingException {
        if(!isTypeRegistered(typeTag1) || !isTypeRegistered(typeTag2)){
            return null;
        }

        Map<U, V> mapping = (customMapImpl == null) ? new HashMap<U, V>() : customMapImpl;
        //Collection<?> valuesSet1, valuesSet2;
        Collection<U> resultSet1;
        Collection<V> resultSet2;
        // Gather values for both tags.
        Iterator<U> iter1 = (Iterator<U>) (typeTagsAndTypeValues.get(typeTag1)).iterator();
        Iterator<V> iter2 = (Iterator<V>) (typeTagsAndTypeValues.get(typeTag2)).iterator();

        resultSet1 = getNewContainer(typeTag1, 10);
        resultSet2 = getNewContainer(typeTag2, 10);

        // Runt through both of the collections identifying elements that match the criteria.
        U ele1;
        V ele2;
        while(iter1.hasNext()){
            ele1 = iter1.next();
            if(type1Criteria.match(ele1)){
                resultSet1.add(ele1);
            }
        }

        while(iter2.hasNext()){
            ele2 = iter2.next();
            if(type2Criteria.match(ele2)){
                resultSet2.add(ele2);
            }
        }

        if(resultSet1.size() != resultSet2.size()){
            throw new IllegalMappingException("Collections to be mapped have different lengths.");
        }

        Iterator<U> i1 = resultSet1.iterator();
        Iterator<V> i2 = resultSet2.iterator();

        while(i1.hasNext() && i2.hasNext()){
            mapping.put(i1.next(), i2.next());
        }

        return mapping;
    }
}
