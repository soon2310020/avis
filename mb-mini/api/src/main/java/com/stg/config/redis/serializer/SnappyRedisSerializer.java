package com.stg.config.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.xerial.snappy.Snappy;

/***/
public class SnappyRedisSerializer<T> implements RedisSerializer<T> {
    private final RedisSerializer<T> innerSerializer;

    public SnappyRedisSerializer(RedisSerializer<T> innerSerializer) {
        this.innerSerializer = innerSerializer;
    }

    public SnappyRedisSerializer() {
        this.innerSerializer = new DefaultRedisSerializer<>();
    }

    /**
     * Create a byte array by serialising and Compressing a java graph (object)
     */
    @Override
    public byte[] serialize(T object) throws SerializationException {
        try {
            byte[] bytes = innerSerializer.serialize(object);
            return Snappy.compress(bytes);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        try {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            byte[] bos = Snappy.uncompress(bytes);
            return innerSerializer.deserialize(bos);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }
}

