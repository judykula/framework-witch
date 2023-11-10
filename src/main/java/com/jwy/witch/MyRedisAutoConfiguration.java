/*
 * easy come, easy go.
 *
 * contact : syiae.jwy@gmail.com
 *
 * · · · · ||   ..     __       ___      ____  ®
 * · · · · ||  ||  || _ ||   ||    ||   ||      ||
 * · · · · ||  ||  \\_ ||_.||    ||   \\_  ||
 * · · _//                                       ||
 * · · · · · · · · · · · · · · · · · ·· ·    ___//
 */
package com.jwy.witch;

import io.lettuce.core.ReadFrom;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * <p>
 *     redis模块
 * </p>
 *
 * @author Jiang Wanyu
 * @version 1.0
 * @date 2023/11/8
 */
@EnableConfigurationProperties(RedisProperties.class)
public class MyRedisAutoConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        if (null == redisProperties.getSentinel()) {
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                    //.readFrom(ReadFrom.MASTER)
                    .build();

            RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
            serverConfig.setPassword(redisProperties.getPassword());
            return new LettuceConnectionFactory(serverConfig, clientConfig);
        }

        RedisSentinelConfiguration serverConfig = new RedisSentinelConfiguration();

        serverConfig.master(redisProperties.getSentinel().getMaster());
        serverConfig.setPassword(redisProperties.getPassword());
        for (String node : redisProperties.getSentinel().getNodes()) {
            String[] split = node.split(":");
            serverConfig.sentinel(split[0], NumberUtils.toInt(split[1]));
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();
        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    //@Bean
    //@SuppressWarnings("unchecked")
    //@ConditionalOnMissingBean
    //public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    //    RedisTemplate<Object, Object> template = new RedisTemplate<>();
    //
    //    //使用FastJson序列化
    //    FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);
    //    // value值的序列化采用fastJsonRedisSerializer
    //    template.setValueSerializer(fastJsonRedisSerializer);
    //    template.setHashValueSerializer(fastJsonRedisSerializer);
    //    // key的序列化采用StringRedisSerializer
    //    template.setKeySerializer(new StringRedisSerializer());
    //    template.setHashKeySerializer(new StringRedisSerializer());
    //
    //    template.setConnectionFactory(redisConnectionFactory);
    //    return template;
    //}

}
