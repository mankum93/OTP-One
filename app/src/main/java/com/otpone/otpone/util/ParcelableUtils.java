package com.otpone.otpone.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by DJ on 5/26/2017.
 */

/**
 * Utility methods to read/write an Object from/to {@link Parcel}
 */
public class ParcelableUtils {

    // String--------------------------------------------------------------------------------------------------------------

    /**
     * @param dest : Parcel to write the String to.
     * @param string : The String to write.
     */
    public static void write(Parcel dest, String string) {
        dest.writeByte((byte) (string == null ? 0 : 1));
        if (string != null) {
            dest.writeString(string);
        }
    }

    /**
     * @param source : The Parcel consisting of our desired String.
     * @return The desired String if it wasn't null or null.
     */
    public static String readString(Parcel source) {
        if (source.readByte() == 1) {
            return source.readString();
        }
        return null;
    }

    // Parcelable---------------------------------------------------------------------------------------------------------

    /**
     * @param dest : Parcel to write the Parcelable to.
     * @param parcelable : The Parcelable to write.
     * @param flags : Flags to specify for Parcelable as per Android specs.
     */
    public static void write(Parcel dest, Parcelable parcelable, int flags) {
        dest.writeByte((byte) (parcelable == null ? 0 : 1));
        if (parcelable != null) {
            dest.writeParcelable(parcelable, flags);
        }
    }

    /**
     * @param source : The Parcel consisting of our desired Parcelable.
     * @return The desired Parcelable if it wasn't null or null.
     */
    public static <T extends Parcelable> T readParcelable(Parcel source, Parcelable.Creator<T> creator) {
        if (source.readByte() == 1) {
            return creator.createFromParcel(source);
        }
        return null;
    }



    // Map<String>
    /*public static void write(Parcel dest, Map<String, String> strings) {
        if (strings == null) {
            dest.writeInt(-1);
        }
        {
            dest.writeInt(strings.keySet().size());
            for (String key : strings.keySet()) {
                dest.writeString(key);
                dest.writeString(strings.get(key));
            }
        }
    }

    public static Map<String, String> readStringMap(Parcel source) {
        int numKeys = source.readInt();
        if (numKeys == -1) {
            return null;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < numKeys; i++) {
            String key = source.readString();
            String value = source.readString();
            map.put(key, value);
        }
        return map;
    }*/


    // Parcelable Map------------------------------------------------------------------------------------------------------

    /**
     * @param dest : Parcel to write the Parcelable to.
     * @param map : The Parcelable to write.
     * @param flags : Flags to specify for Parcelable as per Android specs.
     */
    public static <K extends Parcelable, V extends Parcelable> void write(Parcel dest,
                                                    Map<K, V> map, int flags) {
        if (map == null) {
            dest.writeInt(-1);
        } else {
            Set<Map.Entry<K, V>> entrySet = map.entrySet();
            dest.writeInt(entrySet.size());
            for (Map.Entry<K, V> entry : entrySet) {
                dest.writeParcelable(entry.getKey(), flags);
                dest.writeParcelable(entry.getValue(), flags);
            }
        }
    }

    /**
     * Method to deserialize a Parcelable Map from Parcel.
     * <br>
     * Note: The default Map implementation used for deserializing is a {@link HashMap}.
     * If you want ordering of entries or some other property preserved, pass in your own
     * implementation in the parameter stashInThisMap. If passed null, default shall be used.
     *
     * @param source : The Parcel consisting of our desired Parcelable Map.
     * @param stashInThisMap : The Map in which the deserialized Map entries should be stashed.
     * @param keyCreator : The {@link android.os.Parcelable.Creator} to be employed for creating the key.
     * @param valueCreator : The {@link android.os.Parcelable.Creator} to be employed for creating the value.
     *
     * @param <K> : KeyMaker type parameter.
     * @param <V> : Value type parameter.
     *
     * @return : Deserialized Map or null if it had originally been null.
     */
    public static <K extends Parcelable, V extends Parcelable> Map<K, V> readParcelableMap(
            Parcel source, Map<K, V> stashInThisMap, Parcelable.Creator<K> keyCreator, Parcelable.Creator<V> valueCreator) {
        int numKeys = source.readInt();
        if (numKeys == -1) {
            return null;
        }

        for (int i = 0; i < numKeys; i++) {
            K key = keyCreator.createFromParcel(source);
            V value = valueCreator.createFromParcel(source);
            stashInThisMap.put(key, value);
        }
        return stashInThisMap;
    }


    // java.net.URI----------------------------------------------------------------------------------------------------------

    /**
     * @param dest : Parcel to write the URI to.
     * @param uri : The URI to write.
     */
    public static void write(Parcel dest, URI uri) {
        dest.writeString(uri.toString());
    }

    /**
     * @param source : The Parcel consisting of our desired URI.
     * @return The desired URI if it wasn't null or null.
     */
    public static URI readURI(Parcel source) {
        try {
            return new URI(source.readString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    // java.net.URL----------------------------------------------------------------------------------------------------------

    /**
     * @param dest : Parcel to write the URL to.
     * @param url : The URL to write.
     */
    public static void write(Parcel dest, URL url) {
        dest.writeString(url.toExternalForm());
    }

    /**
     * @param source : The Parcel consisting of our desired URL.
     * @return The desired URL if it wasn't null or null.
     */
    public static URL readURL(Parcel source) {
        try {
            return new URL(source.readString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // java.util.Date-------------------------------------------------------------------------------------------------------

    /**
     * @param dest : Parcel to write the Date to.
     * @param date : The Date to write.
     */
    public static void write(Parcel dest, Date date) {
        dest.writeByte((byte) (date == null ? 0 : 1));
        if (date != null) {
            dest.writeLong(date.getTime());
        }
    }

    /**
     * @param source : The Parcel consisting of our desired Date.
     * @return The desired Date if it wasn't null or null.
     */
    public static Date readDate(Parcel source) {
        if (source.readByte() == 1) {
            return new Date(source.readLong());
        }
        return null;
    }

    // Enum<T>-------------------------------------------------------------------------------------------------------------

    /**
     * @param dest : Parcel to write the Enum to.
     * @param anEnum : The Enum to write.
     */
    public static <T extends Enum<T>> void write(Parcel dest, Enum<T> anEnum) {
        if (anEnum == null) {
            dest.writeString("");
        } else {
            dest.writeString(anEnum.name());
        }
    }

    /**
     * @param source : The Parcel consisting of our desired Enum.
     * @return The desired Enum if it wasn't null or null.
     */
    public static <T extends Enum<T>> T readEnum(Parcel source, Class<T> clazz) {
        String name = source.readString();
        if ("".equals(name)) {
            return null;
        }
        return Enum.valueOf(clazz, name);
    }


    // boolean--------------------------------------------------------------------------------------------------------------

    /**
     * @param dest : Parcel to write the boolean to.
     * @param bool : The boolean to write.
     */
    public static void write(Parcel dest, boolean bool) {
        dest.writeByte((byte) (bool ? 1 : 0));
    }

    /**
     * @param source : The Parcel consisting of our desired boolean.
     * @return The desired boolean.
     */
    public static boolean readBoolean(Parcel source) {
        return source.readByte() == 1;
    }


    // Parcelable List------------------------------------------------------------------------------------------------------

    /**
     *
     * @param dest : Parcel to write the List to.
     * @param fields : The List to write.
     * @param flags : Flags to specify for Parcelable as per Android specs.
     *
     * @param <T> : The type of element, the List holds.
     */
    public static <T extends Parcelable> void write(Parcel dest,
                                                    List<T> fields, int flags) {
        if (fields == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(fields.size());
            for (T field : fields) {
                dest.writeParcelable(field, flags);
            }
        }
    }

    /**
     * Method to deserialize a Parcelable List from Parcel.
     * <br>
     * Note: The default List implementation used for deserializing is an {@link ArrayList}.
     * If you want some List property preserved, pass in your own
     * implementation in the parameter stashInThisList. If passed null, default shall be used.
     *
     * @param source : The Parcel consisting of our desired List.
     * @param stashInThisList : The desired List.
     * @param creator : Creator used for the element to be put in the list.
     *
     * @param <T> : The type of the element of the List.
     *
     * @return : Deserialized List or null if it had originally been null.
     */
    public static <T extends Parcelable> List<T> readParcelableList(
            Parcel source, List<T> stashInThisList, Parcelable.Creator<T> creator) {
        int size = source.readInt();
        if (size == -1) {
            return null;
        }
        if(stashInThisList == null){
            stashInThisList = new ArrayList<T>(size);
        }
        for (int i = 0; i < size; i++) {
            stashInThisList.add(creator.createFromParcel(source));
        }
        return stashInThisList;
    }
}