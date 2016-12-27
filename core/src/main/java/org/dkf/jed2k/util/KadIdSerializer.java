package org.dkf.jed2k.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.dkf.jed2k.protocol.kad.KadId;

import java.lang.reflect.Type;

/**
 * Created by apavlov on 08.12.16.
 */
public class KadIdSerializer implements JsonSerializer<KadId> {
    public JsonElement serialize(KadId src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
