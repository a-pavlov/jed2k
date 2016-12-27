package org.dkf.jed2k.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.dkf.jed2k.protocol.Endpoint;

import java.lang.reflect.Type;

/**
 * Created by inkpot on 08.12.2016.
 */
public class EndpointSerializer implements JsonSerializer<Endpoint> {
    public JsonElement serialize(Endpoint src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
