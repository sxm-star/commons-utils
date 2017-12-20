package com.songxm.commons.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.songxm.commons.BaseDateUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Date;

public class FromStandardFormat extends JsonDeserializer<Date> {
    public FromStandardFormat() {
    }

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
        String date = jp.getText();
        return StringUtils.isBlank(date)?null: BaseDateUtils.parseDate(date, new String[]{"yyyy-MM-dd\'T\'HH:mm:ss.SSSX"});
    }
}