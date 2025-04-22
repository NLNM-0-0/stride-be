package com.stride.tracking.coreservice.utils.converter.list.concrete;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stride.tracking.coreservice.utils.converter.list.ListConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class ListDoubleListConverter implements AttributeConverter<List<List<Double>>, String> {

    @Override
    public String convertToDatabaseColumn(List<List<Double>> attribute) {
        return ListConverter.toJson(attribute);
    }

    @Override
    public List<List<Double>> convertToEntityAttribute(String dbData) {
        return ListConverter.fromJson(dbData, new TypeReference<>() {
        });
    }
}
