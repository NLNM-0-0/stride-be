package com.stride.tracking.coreservice.utils.converter.list.concrete;

import com.stride.tracking.coreservice.model.Rule;
import com.stride.tracking.coreservice.utils.converter.list.ListConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Converter
@Component
public class RuleListConverter implements AttributeConverter<List<Rule>, String> {

    @Override
    public String convertToDatabaseColumn(List<Rule> attribute) {
        return ListConverter.toJson(attribute);
    }

    @Override
    public List<Rule> convertToEntityAttribute(String dbData) {
        return ListConverter.fromJson(dbData, Rule.class);
    }
}
