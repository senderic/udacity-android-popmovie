package com.ericsender.android_nanodegree.popmovie.utils;

import android.content.Context;
import android.content.res.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

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

    public static final String readStreamToString(InputStream is) {
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bis.readLine()) != null) sb.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // Serialize / Deserialize -- Based on:
    // http://grepcode.com/file_/repo1.maven.org/maven2/commons-lang/commons-lang/2.4/org/apache/commons/lang/SerializationUtils.java/?v=source
    //-----------------------------------------------------------------------

    // Clone
    //-----------------------------------------------------------------------
    /**
     * <p>Deep clone an <code>Object</code> using serialization.</p>
     *
     * <p>This is many times slower than writing clone methods by hand
     * on all objects in your object graph. However, for complex object
     * graphs, or for those that don't support deep cloning this can
     * be a simple alternative implementation. Of course all the objects
     * must be <code>Serializable</code>.</p>
     *
     * @param object  the <code>Serializable</code> object to clone
     * @return the cloned object
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static Object clone(Serializable object) {
        return deserialize(serialize(object));
    }

    // Serialize
    //-----------------------------------------------------------------------
    /**
     * <p>Serializes an <code>Object</code> to the specified stream.</p>
     *
     * <p>The stream will be closed once the object is written.
     * This avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.</p>
     *
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param obj  the object to serialize to bytes, may be null
     * @param outputStream  the stream to write to, must not be null
     * @throws IllegalArgumentException if <code>outputStream</code> is <code>null</code>
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static void serialize(Object obj, OutputStream outputStream) {
        if (outputStream == null)
            throw new IllegalArgumentException("The OutputStream must not be null");

        try {
            new ObjectOutputStream(outputStream).writeObject(obj);
        } catch (IOException ex) {
            throw new SerializationException(ex);
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {}
        }
    }

    /**
     * <p>Serializes an <code>Object</code> to a byte array for
     * storage/serialization.</p>
     *
     * @param obj  the object to serialize to bytes
     * @return a byte[] with the converted Serializable
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }
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
        public SerializationException(Throwable ex) {
            super(ex);
        }
    }
}
