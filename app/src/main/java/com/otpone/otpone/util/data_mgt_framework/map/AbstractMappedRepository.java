package com.otpone.otpone.util.data_mgt_framework.map;

import com.otpone.otpone.util.data_mgt_framework.Criteria;
import com.otpone.otpone.util.data_mgt_framework.IllegalMappingException;
import com.otpone.otpone.util.data_mgt_framework.KeyMaker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by DJ on 5/30/2017.
 */

public abstract class AbstractMappedRepository extends AbstractMappableRepository implements MappableRepository {

    protected Map<String, KeyMaker<?, ?>> typeTagsAndKeyMakers;
    protected Map<String, Map<?, ?>> typeTagsAndValues;

    public AbstractMappedRepository(Map<String, KeyMaker<?, ?>> typeTagsAndKeyMakers, Map<String, Map<?, ?>> typeTagsAndValues) {
        this.typeTagsAndKeyMakers = typeTagsAndKeyMakers == null ? new HashMap<String, KeyMaker<?, ?>>() : typeTagsAndKeyMakers;
        this.typeTagsAndValues = typeTagsAndValues == null ? new HashMap<String, Map<?, ?>>() : typeTagsAndValues;
    }

    @Override
    protected boolean isTypeRegistered(String typeTag) {
        if(typeTag == null){
            return false;
        }
        if(typeTagsAndValues.containsKey(typeTag)){
            return true;
        }
        return false;
    }

    @Override
    public Collection<String> getTags() {
        return typeTagsAndKeyMakers.keySet();
    }

    @Override
    protected int getRepositorySize() {
        return typeTagsAndValues.size();
    }

    @Override
    protected <T, K> KeyMaker<T, K> getKeyMaker(String typeTag) {
        return (KeyMaker<T, K>) typeTagsAndKeyMakers.get(typeTag);
    }

    @Override
    protected <T, K> KeyMaker<T, K> setKeyMaker(String typeTag, KeyMaker<T, K> newKeyMaker) {
        return setKeyMaker(typeTag, newKeyMaker, null);
    }

    protected <T, K> KeyMaker<T, K> setKeyMaker(String typeTag, KeyMaker<T, K> newKeyMaker, Map<K, T> customMapImpl) {
        if(newKeyMaker != null){
            // Existing type?
            if(!typeTagsAndKeyMakers.containsKey(typeTag)){
                if(customMapImpl == null){
                    customMapImpl = new HashMap<>();
                }
                typeTagsAndValues.put(typeTag, customMapImpl);
            }
            return (KeyMaker<T, K>) typeTagsAndKeyMakers.put(typeTag, newKeyMaker);
        }

        return null;
    }

    @Override
    public <K, V> void addMappable(String typeTag, KeyMaker<V, K> keyMaker) {

        if(!isTypeRegistered(typeTag)){
            setKeyMaker(typeTag, keyMaker);
        }
    }

    @Override
    public <K, V> boolean addMappable(String typeTag, KeyMaker<V, K> keyMaker, int matchPolicy, V value) {

        // First and foremost, if the type is not registered then insert everything afresh.

        // Do we have the type registered ?
        if(!isTypeRegistered(typeTag)){
            // Register it with the KeyMaker as well as the generated key and value.
            if(keyMaker != null){

                // First put the KeyMaker
                typeTagsAndKeyMakers.put(typeTag, keyMaker);

                // In case value is null, just update the key maker
                if(value != null){
                    Map<K, V> values = new HashMap<>();

                    // Prepare the {key, value} to be put in the tags and values map.
                    values.put(keyMaker.getKey(value), value);

                    typeTagsAndValues.put(typeTag, values);
                }
            }
            else{
                throw new IllegalStateException("KeyMaker is invalid and the type is not registered. So, there" +
                        "is no way to infer \"key\" information for the value.");
            }

            return true;
        }

        // Get the old KeyMaker and check if its the same as this one.
        KeyMaker<V, K> alreadyKeyMaker = (KeyMaker<V, K>) typeTagsAndKeyMakers.get(typeTag);

        if(keyMaker == null){
            keyMaker = alreadyKeyMaker;
        }

        // Here, the type has to be already registered.
        switch(matchPolicy){

            case MATCH_KEYS:

                if(alreadyKeyMaker != keyMaker && !alreadyKeyMaker.equals(keyMaker)){
                    // Update with new keymaker.
                    typeTagsAndKeyMakers.put(typeTag, keyMaker);
                }
                if(value != null){
                    // Prepare the {key, value} to be put in the tags and values map.

                    // Retrieve the already present values map.
                    Map<K, V> map = (Map<K, V>) typeTagsAndValues.get(typeTag);
                    // Ignoring the custom map implementation.
                    map.put(keyMaker.getKey(value), value);

                    return true;
                }

                return false;
            //break;

            case MATCH_VALUES:
                if(value != null){

                    // Iterate through all the values to find the matching ones and replace them with this one.
                    Map<K, V> values = ( Map<K, V>) typeTagsAndValues.get(typeTag);
                    Iterator<Map.Entry<K, V>> iter = values.entrySet().iterator();

                    V v = null;
                    while(iter.hasNext()){
                        Map.Entry<K, V> entry = iter.next();
                        if((v = entry.getValue()) == value || value.equals(v)){
                            entry.setValue(value);
                        }
                    }

                    return true;
                }

                return false;

            //break;

            default:
                throw new IllegalArgumentException("Invalid match policy.");
        }
    }

    @Override
    public <V> boolean addMappable(String typeTag, int matchPolicy, V value) {

        // First and foremost, if the type is not registered then insert everything afresh.

        // Do we have the type registered ?
        if(!isTypeRegistered(typeTag)){

            throw new IllegalStateException("The type is not registered. There is no key/keymaker provided as well. So, there" +
                    "is no way to infer \"key\" information for the value.");
        }

        // Get the existing KeyMaker
        KeyMaker<V, ?> alreadyKeyMaker = (KeyMaker<V, ?>) typeTagsAndKeyMakers.get(typeTag);

        V replacedValue = null;

        // Here, the type has to be already registered.
        switch(matchPolicy){

            case MATCH_KEYS:

                if(value != null){
                    // Prepare the {key, value} to be put in the tags and values map.

                    // Retrieve the already present values map.
                    Map<Object, V> map = (Map<Object, V>) typeTagsAndValues.get(typeTag);
                    // Ignoring the custom map implementation.
                    map.put(alreadyKeyMaker.getKey(value), value);

                    return true;
                }

                return false;
            //break;

            case MATCH_VALUES:
                if(value != null){

                    // Iterate through all the values to find the matching ones and replace them with this one.
                    Map<Object, V> values = ( Map<Object, V>) typeTagsAndValues.get(typeTag);
                    Iterator<Map.Entry<Object, V>> iter = values.entrySet().iterator();

                    V v = null;
                    while(iter.hasNext()){
                        Map.Entry<Object, V> entry = iter.next();
                        if((v = entry.getValue()) == value || value.equals(v)){
                            replacedValue = entry.setValue(value);
                        }
                    }

                    return replacedValue != null;
                }

                return false;

            //break;

            default:
                throw new IllegalArgumentException("Invalid match policy.");
        }
    }

    @Override
    public <V> boolean addMappableAll(String typeTag, int matchPolicy, Collection<V> values) {

        // First and foremost, if the type is not registered then insert everything afresh.

        // Do we have the type registered ?
        if(!isTypeRegistered(typeTag)){

            throw new IllegalStateException("The type is not registered. There is no key/keymaker provided as well. So, there" +
                    "is no way to infer \"key\" information for the value.");
        }

        // Get the existing KeyMaker
        KeyMaker<V, ?> alreadyKeyMaker = (KeyMaker<V, ?>) typeTagsAndKeyMakers.get(typeTag);

        V replacedValue = null;

        // Here, the type has to be already registered.
        switch(matchPolicy){

            case MATCH_KEYS:

                if(values != null && !values.isEmpty()){
                    // Prepare the {key, value} to be put in the tags and values map.

                    // Retrieve the already present values map.
                    Map<Object, V> map = (Map<Object, V>) typeTagsAndValues.get(typeTag);

                    // Ignoring the custom map implementation.
                    for(V val : values){
                        replacedValue = map.put(alreadyKeyMaker.getKey(val), val);
                    }

                    return replacedValue != null;
                }

                return false;
            //break;

            case MATCH_VALUES:
                if(values != null && !values.isEmpty()){

                    // Iterate through all the values to find the matching ones and replace them with this one.
                    Map<Object, V> values1 = ( Map<Object, V>) typeTagsAndValues.get(typeTag);
                    Iterator<Map.Entry<Object, V>> iter = values1.entrySet().iterator();

                    V v = null;

                    while(iter.hasNext()){
                        Map.Entry<Object, V> entry = iter.next();
                        v = entry.getValue();
                        for(V val : values){
                            if((v = entry.getValue()) == val || val.equals(v)){
                                replacedValue = entry.setValue(val);
                            }
                        }
                    }

                    return replacedValue != null;
                }

                return false;

            //break;

            default:
                throw new IllegalArgumentException("Invalid match policy.");
        }
    }

    @Override
    public <K, V> Map<K, V> getMappable(String typeTag, Criteria<V> criteria, Map<K, V> customMapImpl) {

        if(isTypeRegistered(typeTag)){

            if(customMapImpl == null){
                customMapImpl = new HashMap<>();
            }
            Map<K, V> values = (Map<K, V>) typeTagsAndValues.get(typeTag);

            if(criteria != null){
                // Iterate through all the values to find the matching ones.
                Iterator<Map.Entry<K, V>> iter =  values.entrySet().iterator();

                // Retrieve the key maker.
                KeyMaker<V, K> keyMaker = (KeyMaker<V, K>) typeTagsAndKeyMakers.get(typeTag);

                Map.Entry<K, V> entry;
                V obj;
                while(iter.hasNext()){
                    entry = iter.next();
                    obj = entry.getValue();
                    if(criteria.match(obj)){
                        customMapImpl.put(keyMaker.getKey(obj), obj);
                    }
                }

            }
            else{
                // No criteria, return all
                customMapImpl.putAll(values);
            }

            return customMapImpl.isEmpty() ? null : customMapImpl;
        }

        return null;
    }

    @Override
    public <K, V> V getMappable(String typeTag, K key) {

        if(isTypeRegistered(typeTag)){
            return (V) typeTagsAndValues.get(typeTag).get(key);
        }
        return null;
    }

    @Override
    public <K, V> Map<K, V> getMappableAll(String typeTag, Collection<K> keys, Map<K, V> customMapImpl) {

        if(isTypeRegistered(typeTag)){

            if (customMapImpl == null) {
                customMapImpl = new HashMap<>();
            }

            if(keys != null && !keys.isEmpty()){

                // Retrieve the already present values map.
                Map<Object, V> map = (Map<Object, V>) typeTagsAndValues.get(typeTag);

                for(K key : keys){
                    customMapImpl.put(key, map.get(key));
                }

                return customMapImpl.isEmpty() ? null : customMapImpl;
            }
        }

        return null;
    }

    @Override
    public <K, V> Map<K, V> getMappableAll(String typeTag, Map<K, V> customMapImpl) {
        return getMappable(typeTag, (Criteria<V>) null, customMapImpl);
    }

    @Override
    public Map<String, Map<?, ?>> getMappableAll() {
        return typeTagsAndValues;
    }

    @Override
    public <K, V> V setMappable(String typeTag, K key, V value) {

        if(isTypeRegistered(typeTag)){
            if(key != null){
                Map<K, V> values = (Map<K, V>) typeTagsAndValues.get(typeTag);

                return values.put(key, value);
            }
        }

        return null;
    }

    @Override
    public <K, V> Map<K, V> removeMappable(String typeTag) {

        // Remove all the values as well as the key maker corresponding to this tag.
        if(isTypeRegistered(typeTag)){

            typeTagsAndKeyMakers.remove(typeTag);

            return (Map<K, V>) typeTagsAndValues.remove(typeTag);
        }

        return null;
    }

    @Override
    public <K, V> V removeMappable(String typeTag, K key) {
        if(isTypeRegistered(typeTag)){
            if(key != null){
                Map<K, V> values = (Map<K, V>) typeTagsAndValues.get(typeTag);

                return values.remove(key);
            }
        }

        return null;
    }

    @Override
    public <K, V> Map<K, V> removeMappableAll(String typeTag, Collection<K> keys, Map<K, V> customMapImpl) {

        if(isTypeRegistered(typeTag)){

            if (customMapImpl == null) {
                customMapImpl = new HashMap<>();
            }

            if(keys != null && !keys.isEmpty()){

                // Retrieve the already present values map.
                Map<Object, V> map = (Map<Object, V>) typeTagsAndValues.get(typeTag);

                for(K key : keys){
                    customMapImpl.put(key, map.remove(key));
                }

                return customMapImpl.isEmpty() ? null : customMapImpl;
            }
        }

        return null;
    }

    @Override
    public <K, V> Map<K, V> removeMappableAll(String typeTag, int matchPolicy, Map<K, V> customMapImpl, Collection<V> values) {

        if(isTypeRegistered(typeTag)){

            // Here, the type has to be already registered.
            if(values != null && !values.isEmpty()){

                if (customMapImpl == null) {
                    customMapImpl = new HashMap<>();
                }

                switch(matchPolicy){

                    case MATCH_KEYS:

                        // Get the existing KeyMaker
                        KeyMaker<V, K> alreadyKeyMaker = (KeyMaker<V, K>) typeTagsAndKeyMakers.get(typeTag);

                        // Retrieve the already present values map.
                        Map<K, V> map = (Map<K, V>) typeTagsAndValues.get(typeTag);

                        K k;
                        V v1;

                        for(V val : values){
                            k = alreadyKeyMaker.getKey(val);
                            v1 = map.remove(k);
                            customMapImpl.put(k, v1);
                        }

                    break;

                    case MATCH_VALUES:

                        // Iterate through all the values to find the matching ones and replace them with this one.
                        Map<K, V> values1 = ( Map<K, V>) typeTagsAndValues.get(typeTag);
                        Iterator<Map.Entry<K, V>> iter = values1.entrySet().iterator();

                        V v = null;

                        while(iter.hasNext()){
                            Map.Entry<K, V> entry = iter.next();

                            for(V val : values){
                                if((v = entry.getValue()) == val || val.equals(v)){
                                    iter.remove();
                                    customMapImpl.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }

                    break;

                    default:
                        throw new IllegalArgumentException("Invalid match policy.");
                }

                return customMapImpl.isEmpty() ? null : customMapImpl;
            }

        }

        return null;
    }

    @Override
    public <K, V> Map<K, V> removeMappableAll(String typeTag, Criteria<V> removalCriteria, Map<K, V> customMapImpl) {

        if(isTypeRegistered(typeTag)){

            if(customMapImpl == null){
                customMapImpl = new HashMap<>();
            }
            Map<K, V> values = (Map<K, V>) typeTagsAndValues.get(typeTag);

            if(removalCriteria != null){

                // Iterate through all the values to find the matching ones.
                Iterator<Map.Entry<K, V>> iter =  values.entrySet().iterator();

                // Retrieve the key maker.
                KeyMaker<V, K> keyMaker = (KeyMaker<V, K>) typeTagsAndKeyMakers.get(typeTag);

                Map.Entry<K, V> entry;
                V obj;
                while(iter.hasNext()){
                    entry = iter.next();
                    obj = entry.getValue();
                    if(removalCriteria.match(obj)){
                        customMapImpl.put(keyMaker.getKey(obj), obj);
                        iter.remove();
                    }
                }

            }
            else{
                // No criteria, remove all
                customMapImpl.putAll(values);
                typeTagsAndValues.remove(typeTag);
            }

            return customMapImpl.isEmpty() ? null : customMapImpl;
        }

        return null;
    }

    @Override
    public Map<String, Map<?, ?>> removeMappableAll() {

        Map<String, Map<?, ?>> newMap = typeTagsAndValues;
        typeTagsAndValues = new HashMap<>();

        return newMap;
    }

    @Override
    public void clear(String typeTag) {
        if(isTypeRegistered(typeTag)){
            typeTagsAndValues.get(typeTag).clear();
        }
    }

    @Override
    public void clearAll() {
        // Remove all the objects collections we have.
        for(String typeTag : typeTagsAndKeyMakers.keySet()){

            typeTagsAndValues.get(typeTag).clear();
        }
    }

    @Override
    public <U, V> Map<U, V> map(String typeTag1, String typeTag2, Map<U, V> customMapImpl) throws IllegalMappingException {

        if(!isTypeRegistered(typeTag1) || !isTypeRegistered(typeTag2)){
            return null;
        }

        Map<U, V> mapping = (customMapImpl == null) ? new HashMap<U, V>() : customMapImpl;
        Collection<?> valuesSet1, valuesSet2;
        // Gather values for both tags.
        Iterator<U> iter1 = (Iterator<U>) (valuesSet1 = typeTagsAndValues.get(typeTag1).values()).iterator();
        Iterator<V> iter2 = (Iterator<V>)(valuesSet2 = typeTagsAndValues.get(typeTag2).values()).iterator();

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
        Iterator<U> iter1 = (Iterator<U>) (typeTagsAndValues.get(typeTag1).values()).iterator();
        Iterator<V> iter2 = (Iterator<V>) (typeTagsAndValues.get(typeTag2).values()).iterator();

        resultSet1 = new LinkedList<>();
        resultSet2 = new LinkedList<>();

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
