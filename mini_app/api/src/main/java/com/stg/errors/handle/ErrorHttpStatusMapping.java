package com.stg.errors.handle;

import com.stg.errors.ApplicationException;
import com.stg.errors.CredentialNotFoundException;
import com.stg.errors.UserAlreadyExistsException;
import com.stg.errors.UserNotFoundException;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ErrorHttpStatusMapping extends ExceptionConfigurationMapper {

    @Override
    protected void addMappings() {

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(ApplicationException.class));
        
        Set<BeanDefinition> components = provider.findCandidateComponents("com.stg.errors");
        for (BeanDefinition component : components) {
            try {
                Class<?> cls = Class.forName(component.getBeanClassName());

                if (ApplicationException.class.isAssignableFrom(cls)) {
                    if (cls == ApplicationException.class) {
                        continue;
                    }

                    addMapping((Class<? extends ApplicationException>) cls, ExceptionHandlingConfiguration.Builder.create()
                            .withHttpStatus(HttpStatus.BAD_REQUEST)
                            .withLogLevel(LogLevel.ERROR)
                            .withLogWithStacktrace(false)
                            .build());
                } else {
                    log.warn("ErrorHttpStatusMapping, load miss: {}", cls.getName());
                }
            } catch (ClassNotFoundException e) {
                log.error("ErrorHttpStatusMapping, detail={}", e.getMessage());
            }
        }
        
        addMapping(UserNotFoundException.class, ExceptionHandlingConfiguration.Builder.create()
                .withHttpStatus(HttpStatus.NOT_ACCEPTABLE)
                .withLogLevel(LogLevel.INFO)
                .withLogWithStacktrace(false)
                .build()
        );

        addMapping(CredentialNotFoundException.class, ExceptionHandlingConfiguration.Builder.create()
                .withHttpStatus(HttpStatus.NOT_FOUND)
                .withLogLevel(LogLevel.INFO)
                .withLogWithStacktrace(false)
                .build()
        );

        addMapping(UserAlreadyExistsException.class, ExceptionHandlingConfiguration.Builder.create()
                .withHttpStatus(HttpStatus.CONFLICT)
                .withLogLevel(LogLevel.INFO)
                .withLogWithStacktrace(false)
                .build()
        );

    }

}