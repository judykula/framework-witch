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
package com.jwy.witch.template;

import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * <p>
 *     自定义只从主库进行操作的factory
 * </p>
 *
 * @see MyMainStringRedisTemplate
 * @author Jiang Wanyu
 * @version 1.0
 * @date 2023/11/15
 */
public class MyMainLettuceConnectionFactory extends LettuceConnectionFactory {

    public MyMainLettuceConnectionFactory() {
    }

    public MyMainLettuceConnectionFactory(RedisStandaloneConfiguration standaloneConfig, LettuceClientConfiguration clientConfig) {
        super(standaloneConfig, clientConfig);
    }

    public MyMainLettuceConnectionFactory(RedisSentinelConfiguration sentinelConfiguration, LettuceClientConfiguration clientConfig) {
        super(sentinelConfiguration, clientConfig);
    }
}
