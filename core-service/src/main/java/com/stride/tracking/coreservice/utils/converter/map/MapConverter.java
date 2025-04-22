package com.stride.tracking.coreservice.utils.converter.map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.coreservice.constant.Message;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Map;

public class MapConverter {
	private MapConverter() {}

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static <K, V> String toJson(Map<K, V> map) {
		try {
			return objectMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new StrideException(HttpStatus.BAD_REQUEST, Message.JSON_ERR);
		}
	}

	public static <K, V> Map<K, V> fromJson(String json, Class<K> keyClass, Class<V> valueClass) {
		try {
			return objectMapper.readValue(json,
					objectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
		} catch (IOException e) {
			throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.COMMON_ERR);
		}
	}

	public static <K, V> Map<K, V> fromJson(String json, TypeReference<Map<K, V>> typeRef) {
		try {
			return objectMapper.readValue(json, typeRef);
		} catch (IOException e) {
			throw new StrideException(HttpStatus.INTERNAL_SERVER_ERROR, Message.COMMON_ERR);
		}
	}
}
