package com.emoldino.framework.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public abstract class ListAttributeConverter<T> implements AttributeConverter<List<T>, String> {
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(List<T> attribute) {
		if (attribute == null) {
			return "[]";
		}
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			return "[]";
		}
	}

	@Override
	public List<T> convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return new ArrayList<>();
		}
		try {
			return objectMapper.registerModule(new JavaTimeModule()).readValue(dbData, new TypeReference<List<T>>() {
			});
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
}
