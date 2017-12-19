package com.songxm.commons.serializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.songxm.commons.BaseJsonUtils;
import com.songxm.commons.BaseStringUtils;
import com.songxm.commons.annotation.JsonMosaic;
import java.io.IOException;

/**
 * @author  songxm
 */
@SuppressWarnings("unchecked")
public class CustomSerializer extends StdSerializer {
    private JsonMosaic jsonMosaic;

    public CustomSerializer(JsonMosaic jsonMosaic) {
        super((Class)null);
        this.jsonMosaic = jsonMosaic;
    }

    @Override
    public void serialize(Object obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(obj != null) {
            String text = BaseJsonUtils.writeValue(obj);
            text = BaseStringUtils.mosaic(text, this.jsonMosaic, '*');
            jsonGenerator.writeString(text);
        }
    }
}