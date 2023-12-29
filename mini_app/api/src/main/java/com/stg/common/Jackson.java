package com.stg.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.stg.errors.JacksonParserException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Parser override
 */
@Slf4j
public class Jackson {
    private final ObjectMapper objectMapper;

    Jackson() {
        this.objectMapper = newObjectMapperInstance();
    }

    Jackson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /***/
    private static final Jackson INSTANCE = new Jackson();
    private static final Jackson SNACK_CASE_INSTANCE = newSnack();

    /**
     * Singleton!
     */
    public static Jackson get() {
        return INSTANCE;
    }

    public static Jackson snack() {
        return SNACK_CASE_INSTANCE;
    }

    public static Jackson newInstance() {
        return new Jackson();
    }

    public static Jackson newSnack() {
        return Jackson.newInstance(Jackson.newObjectMapperInstance().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE));
    }

    public static Jackson newInstance(ObjectMapper objectMapper) {
        return new Jackson(objectMapper);
    }

    /***/
    public static ObjectMapper newObjectMapperInstance() {
        return new ObjectMapper()
                /*.registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)*/
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                //.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .findAndRegisterModules();
    }

    /***/
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException ex) {
            log.warn("Cannot parse JSON", ex);
            throw new JacksonParserException(ex.getMessage());
        }
    }
    
    public <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        try {
            return objectMapper.readValue(json, valueTypeRef);
        } catch (JsonProcessingException ex) {
            log.error("Cannot parse JSON", ex);
            throw new JacksonParserException(ex.getMessage());
        }
    }

    /***/
    public <T> List<T> fromJsonList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException ex) {
            log.warn("Cannot parse JSON", ex);
            throw new JacksonParserException(ex.getMessage());
        }
    }


    /***/
    public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.warn("Cannot convert to JSON", ex);
            throw new JacksonParserException(ex.getMessage());
        }
    }

    public String toJsonNonTrace(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    /***/
    public String toString(Object obj) {
        if (obj == null) return "null";

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.warn("Cannot convert to JSON", ex);
            return obj.toString();
        }
    }


    /***/
    public <T> T fromBytes(byte[] bytes, TypeReference<T> valueTypeRef) {
        try {
            return objectMapper.readValue(bytes, valueTypeRef);
        } catch (IOException ex) {
            log.warn("Can't parse BINARY", ex);
            throw new JacksonParserException(ex.getMessage());
        }
    }

    public <T> T fromBytes(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException ex) {
            log.warn("Cannot parse BINARY", ex);
            throw new JacksonParserException(ex.getMessage());
        }
    }

    public <T> List<T> fromBytesList(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException ex) {
            log.warn("Cannot parse BINARY", ex);
            throw new JacksonParserException(ex.getMessage());
        }
    }

    public byte[] toBytes(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException ex) {
            log.warn("Cannot convert to BINARY", ex);
            throw new JacksonParserException(ex.getMessage());
        }
    }
}
