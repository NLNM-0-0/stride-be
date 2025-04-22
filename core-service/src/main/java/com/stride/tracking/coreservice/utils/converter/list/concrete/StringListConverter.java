package com.stride.tracking.coreservice.utils.converter.list.concrete;

import com.stride.tracking.coreservice.utils.converter.list.ListConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return ListConverter.toJson(attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return ListConverter.fromJson(dbData, String.class);
    }
}
