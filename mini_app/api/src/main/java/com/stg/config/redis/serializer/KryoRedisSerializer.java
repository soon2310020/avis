package com.stg.config.redis.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayOutputStream;

/***/
@Slf4j
public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static final ThreadLocal<Kryo> KRYO_LOCAL = ThreadLocal.withInitial(Kryo::new);

    private final Class<T> clazz;

    public KryoRedisSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return EMPTY_BYTE_ARRAY;
        }

        Kryo kryo = KRYO_LOCAL.get();
        /*kryo.setReferences(false);
        kryo.register(clazz);*/
        kryo.setRegistrationRequired(false);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            kryo.writeClassAndObject(output, t);
            output.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (KRYO_LOCAL.get() != null) KRYO_LOCAL.remove();
        }

        return EMPTY_BYTE_ARRAY;
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        Kryo kryo = KRYO_LOCAL.get();
        /*kryo.setReferences(false);
        kryo.register(clazz);*/
        kryo.setRegistrationRequired(false);

        try (Input input = new Input(bytes)) {
            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (KRYO_LOCAL.get() != null) KRYO_LOCAL.remove();
        }

        return null;
    }
}

