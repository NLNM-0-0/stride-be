package com.stride.tracking.coreservice.utils.converter.list.concrete;

import com.stride.tracking.coreservice.model.HeartRateZoneValue;
import com.stride.tracking.coreservice.utils.converter.list.ListConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class HeartRateZoneValueListConverter implements AttributeConverter<List<HeartRateZoneValue>, String> {

    @Override
    public String convertToDatabaseColumn(List<HeartRateZoneValue> attribute) {
        return ListConverter.toJson(attribute);
    }

    @Override
    public List<HeartRateZoneValue> convertToEntityAttribute(String dbData) {
        return ListConverter.fromJson(dbData, HeartRateZoneValue.class);
    }
}
