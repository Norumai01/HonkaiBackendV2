package com.norumai.honkaiwebsitebackend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.RedisCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class Jackson2JsonRedisCodec<K, V> implements RedisCodec<K, V> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<K> keyClass;
    private final Class<V> valueClass;
    private static final Logger logger = LoggerFactory.getLogger(Jackson2JsonRedisCodec.class);

    public Jackson2JsonRedisCodec(Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    // Redis stores data as binary. Convert binary data of key back into Java Objects.
    @Override
    public K decodeKey(ByteBuffer bytes) {
        try {
            byte[] byteArray = new byte[bytes.remaining()];
            bytes.get(byteArray);
            return objectMapper.readValue(byteArray, keyClass); // Parse into appropriate Object class.
        }
        catch (Exception e) {
            logger.error("Error while decoding key ", e);
            throw new RuntimeException("Error while decoding key ", e);
        }
    }

    // Convert binary data of key-value back into Java Objects
    @Override
    public V decodeValue(ByteBuffer bytes) {
        try {
            byte[] byteArray = new byte[bytes.remaining()];
            bytes.get(byteArray);
            return objectMapper.readValue(byteArray, valueClass);
        }
        catch (Exception e) {
            logger.error("Error while decoding key-value ", e);
            throw new RuntimeException("Error while decoding key-value ", e);
        }
    }

    // Convert Java Objects into binary for key to be stored in Redis
    @Override
    public ByteBuffer encodeKey(K key) {
        try {
            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(key)); // Convert objects into JSON.
        }
        catch (Exception e) {
            logger.error("Error while encoding key", e);
            throw new RuntimeException("Error while encoding key ", e);
        }
    }

    // Convert Java objects into binary for key-value to be stored in Redis.
    @Override
    public ByteBuffer encodeValue(V value) {
        try {
            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
        }
        catch (Exception e) {
            logger.error("Error while encoding key-value ", e);
            throw new RuntimeException("Error while encoding key-value ", e);
        }
    }
}
