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

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>
 *     自定义只从"主库"进行数据操作的template
 * </p>
 *
 * @see MyMainLettuceConnectionFactory
 * @author Jiang Wanyu
 * @version 1.0
 * @date 2023/11/15
 */
public class MyMainStringRedisTemplate extends StringRedisTemplate {

    public MyMainStringRedisTemplate() {
    }

    public MyMainStringRedisTemplate(RedisConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

}
