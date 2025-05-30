package com.stride.tracking.coreservice.utils.converter.map.concrete;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stride.tracking.coreservice.utils.converter.map.MapConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Converter
@Component
public class MapStringListString implements AttributeConverter<Map<String, List<String>>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, List<String>> attribute) {
        return MapConverter.toJson(attribute);
    }

    @Override
    public Map<String, List<String>> convertToEntityAttribute(String dbData) {
        return MapConverter.fromJson(dbData, new TypeReference<>() {
        });
    }
}
