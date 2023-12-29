package com.stg.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.gson.Gson;
import com.stg.common.Jackson;
import com.stg.service.dto.RawData;
import com.stg.utils.DateUtil;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JacksonConfiguration {
    @Bean("jackson")
    public Jackson jackson() {
        return Jackson.newInstance();
    }

    /*GSON*/
    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Jackson2ObjectMapperBuilderCustomizer customJackson() {
        return builder -> {
            builder.serializerByType(Page.class, new JsonSerializer<Page<?>>() {
                @Override
                public void serialize(Page<?> page, JsonGenerator jsonGenerator, SerializerProvider serializers)
                        throws IOException {
                    Map<String, Object> content = new HashMap<>();
                    content.put("content", page.getContent());
                    content.put("total_elements", page.getTotalElements());
                    content.put("total_pages", page.getTotalPages());
                    content.put("size", page.getSize());
                    content.put("page", page.getNumber());
                    jsonGenerator.writeObject(content);
                }
            });

            builder.serializerByType(GrantedAuthority.class, new JsonSerializer<GrantedAuthority>() {
                @Override
                public void serialize(GrantedAuthority authority, JsonGenerator jsonGenerator, SerializerProvider serializers)
                        throws IOException {
                    jsonGenerator.writeString(authority.getAuthority());
                }
            });
            
            builder.serializerByType(RawData.class, new JsonSerializer<RawData>() {
                @Override
                public void serialize(RawData raw, JsonGenerator jsonGenerator, SerializerProvider serializers)
                        throws IOException {
                    if (StringUtils.hasLength(raw.getValue())) {
                        jsonGenerator.writeRawValue(raw.getValue());
                    } else {
                        jsonGenerator.writeNull();
                    }
                }
            });
            
            builder.serializerByType(LocalDate.class, new JsonSerializer<LocalDate>() {
                @Override
                public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializers)
                        throws IOException {
                    jsonGenerator.writeString(DateUtil.localDateTimeToString(localDate));
                }
            });

            builder.deserializerByType(LocalDate.class, new JsonDeserializer<LocalDate>() {

                @Override
                public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
                        throws IOException, JacksonException {
                    return DateUtil.stringToLocalDate(p.getText());
                }
                
            });
            
            builder.deserializerByType(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {

                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
                        throws IOException, JacksonException {
                    return DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_ZONE, p.getText());
                }
                
            });

            builder.serializationInclusion(Include.NON_NULL);
            builder.failOnUnknownProperties(false);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        };
    }
}
