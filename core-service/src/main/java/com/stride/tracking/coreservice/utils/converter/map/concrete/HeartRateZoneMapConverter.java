package com.stride.tracking.coreservice.utils.converter.map.concrete;

import com.stride.tracking.coreservice.utils.converter.map.MapConverter;
import com.stride.tracking.profile.dto.profile.HeartRateZone;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Converter
@Component
public class HeartRateZoneMapConverter implements AttributeConverter<Map<HeartRateZone, Integer>, String> {

    @Override
    public String convertToDatabaseColumn(Map<HeartRateZone, Integer> attribute) {
        return MapConverter.toJson(attribute);
    }

    @Override
    public Map<HeartRateZone, Integer> convertToEntityAttribute(String dbData) {
        return MapConverter.fromJson(dbData, HeartRateZone.class, Integer.class);
    }
}
