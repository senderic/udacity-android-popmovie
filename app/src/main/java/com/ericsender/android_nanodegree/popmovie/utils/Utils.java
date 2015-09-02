package com.ericsender.android_nanodegree.popmovie.utils;

import android.content.Context;
import android.content.res.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Created by g56147 on 8/11/2015.
 */
public class Utils {
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Object.class, new NaturalDeserializer()).create();

    /*
        * http://stackoverflow.com/a/18387977/1582712
        */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static final Gson getGson() {
        return gson;
    }

    // Deserialize -- Based on:
    // http://grepcode.com/file_/repo1.maven.org/maven2/commons-lang/commons-lang/2.4/org/apache/commons/lang/SerializationUtils.java/?v=source
    //-----------------------------------------------------------------------

    /**
     * <p>Deserializes an <code>Object</code> from the specified stream.</p>
     * <p/>
     * <p>The stream will be closed once the object is written. This
     * avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.</p>
     * <p/>
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param inputStream the serialized object input stream, must not be null
     * @return the deserialized object
     * @throws SerializationException   (runtime) if the serialization fails
     */
    public static Object deserialize(InputStream inputStream) {
        try {
            return (new ObjectInputStream(inputStream)).readObject();
        } catch (ClassNotFoundException | IOException ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * <p>Deserializes a single <code>Object</code> from an array of bytes.</p>
     *
     * @param objectData the serialized object, must not be null
     * @return the deserialized object
     * @throws SerializationException   (runtime) if the serialization fails
     */
    public static Object deserialize(byte[] objectData) {
        return deserialize(new ByteArrayInputStream(objectData));
    }

    public static class SerializationException extends RuntimeException {
        public SerializationException(Exception ex) {
            super(ex);
        }
    }
}
