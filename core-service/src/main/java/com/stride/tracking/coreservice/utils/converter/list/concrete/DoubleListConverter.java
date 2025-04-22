package com.stride.tracking.coreservice.utils.converter.list.concrete;

import com.stride.tracking.coreservice.utils.converter.list.ListConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class DoubleListConverter implements AttributeConverter<List<Double>, String> {

    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        return ListConverter.toJson(attribute);
    }

    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        return ListConverter.fromJson(dbData, Double.class);
    }
}
