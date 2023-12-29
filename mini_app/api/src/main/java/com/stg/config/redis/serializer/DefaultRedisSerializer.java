package com.stg.config.redis.serializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stg.common.Jackson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/***/
@Slf4j
public class DefaultRedisSerializer<T> implements RedisSerializer<T> {
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static final ThreadLocal<Jackson> JACKSON_LOCAL = ThreadLocal.withInitial(Jackson::get);

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return EMPTY_BYTE_ARRAY;
        }

        try {
            Jackson jackson = JACKSON_LOCAL.get();
            return jackson.toBytes(t);
        } finally {
            if (JACKSON_LOCAL.get() != null) JACKSON_LOCAL.remove();
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            Jackson jackson = JACKSON_LOCAL.get();
            return jackson.fromBytes(bytes, new TypeReference<T>() {
            });
        } finally {
            if (JACKSON_LOCAL.get() != null) JACKSON_LOCAL.remove();
        }
    }
}

