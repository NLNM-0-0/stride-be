package com.stride.tracking.coreservice.utils.converter.list.concrete;

import com.stride.tracking.coreservice.utils.converter.list.ListConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class IntegerListConverter implements AttributeConverter<List<Integer>, String> {

    @Override
    public String convertToDatabaseColumn(List<Integer> attribute) {
        return ListConverter.toJson(attribute);
    }

    @Override
    public List<Integer> convertToEntityAttribute(String dbData) {
        return ListConverter.fromJson(dbData, Integer.class);
    }
}
