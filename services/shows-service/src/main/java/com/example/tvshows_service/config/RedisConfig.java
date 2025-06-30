package com.example.tvshows_service.config;

import com.example.tvshows_service.dto.TvShowDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, TvShowDto> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, TvShowDto> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<TvShowDto> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(TvShowDto.class);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(jackson2JsonRedisSerializer);

        return template;
    }
}
