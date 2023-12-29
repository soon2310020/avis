package com.stg.errors;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HttpMessageConverter<String> messageConverter;

    @Autowired
    private final Gson gson;

    public CustomAuthenticationEntryPoint(Gson gson) {
        this.gson = gson;
        this.messageConverter = new StringHttpMessageConverter();
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        ApiError apiError = new ApiError(UNAUTHORIZED);
        apiError.setMessage(e.getMessage());
        apiError.setDebugMessage(e.getMessage());
        try (ServerHttpResponse outputMessage = new ServletServerHttpResponse(httpServletResponse)) {
            outputMessage.setStatusCode(HttpStatus.UNAUTHORIZED);
            messageConverter.write(gson.toJson(apiError), MediaType.APPLICATION_JSON, outputMessage);
        }
    }
}
