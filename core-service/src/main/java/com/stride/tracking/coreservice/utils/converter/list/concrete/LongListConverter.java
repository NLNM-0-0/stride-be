package com.stride.tracking.coreservice.utils.converter.list.concrete;

import com.stride.tracking.coreservice.utils.converter.list.ListConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class LongListConverter implements AttributeConverter<List<Long>, String> {

    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        return ListConverter.toJson(attribute);
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        return ListConverter.fromJson(dbData, Long.class);
    }
}
