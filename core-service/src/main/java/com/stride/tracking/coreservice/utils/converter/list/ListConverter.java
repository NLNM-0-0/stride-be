package com.stride.tracking.coreservice.utils.converter.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.constant.Message;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

public class ListConverter {
	private ListConverter() {}

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static <T> String toJson(List<T> list) {
		try {
			return objectMapper.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			throw new StrideException(HttpStatus.BAD_REQUEST, Message.JSON_ERR);
		}
	}

	public static <T> List<T> fromJson(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json,
					objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
		} catch (IOException e) {
			throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.COMMON_ERR);
		}
	}

	public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
		try {
			return objectMapper.readValue(json, valueTypeRef);
		} catch (IOException e) {
			throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.COMMON_ERR);
		}
	}
}
