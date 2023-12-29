package vn.com.twendie.avis.mobile.api.model.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobMessage implements Serializable {

    private Class<?> clazz;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] args;

}
