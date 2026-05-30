package com.example.schedulemeetingbe.config;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    // cấu hình rest template để sử dụng cho opsforvalue, ...
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        //lưu key dưới dạng string
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // dùng builder của phiên bản mới để tự động cấu hình ObjectMapper chuẩn
        template.setValueSerializer(genericJacksonJsonRedisSerializer);
        template.setHashValueSerializer(genericJacksonJsonRedisSerializer);

        // kiểm tra cấu hình
        template.afterPropertiesSet();
        return template;
    }

    //cấu hình các thông sô cho cache manager như ttl, cách ghi key value...
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericJacksonJsonRedisSerializer));
    }

    /* cấu hình lại thì khi sử dụng các annotation như @Cacheable, ... ko lưu bằng concurrent map cache manager nữa
        mà lưu sang redis server, mặc định ko cấu hình thì sẽ dùng concurrent
     */
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration redisCacheConfiguration
    ) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }

    @Bean
    public GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer() {
        return GenericJacksonJsonRedisSerializer.builder().build();
    }

    // nếu lỗi thì bỏ qua, vẫn sẽ xuống db lấy data như bình thường
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
//                log.error("Redis Cache GET thất bại với key [{}]. Lỗi: {}", key, exception.getMessage());
                // Không ném ra ngoại lệ (exception), ứng dụng sẽ tự xuống DB tìm dữ liệu
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
//                log.error("Redis Cache PUT thất bại với key [{}]. Lỗi: {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
//                log.error("Redis Cache EVICT (Xóa) thất bại với key [{}]. Lỗi: {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
//                log.error("Redis Cache CLEAR thất bại. Lỗi: {}", exception.getMessage());
            }
        };
    }

}
