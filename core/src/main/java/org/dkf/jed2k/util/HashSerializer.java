package org.dkf.jed2k.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.dkf.jed2k.protocol.Hash;

import java.lang.reflect.Type;

/**
 * Created by apavlov on 08.12.16.
 */
public class HashSerializer  implements JsonSerializer<Hash> {
    public JsonElement serialize(Hash src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
