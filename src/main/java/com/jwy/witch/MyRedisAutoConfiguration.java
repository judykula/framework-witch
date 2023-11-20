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

import com.jwy.witch.lock.RedisLock;
import com.jwy.witch.lock.RedisLockImpl;
import com.jwy.witch.template.MyMainLettuceConnectionFactory;
import com.jwy.witch.template.MyMainStringRedisTemplate;
import io.lettuce.core.ReadFrom;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * <p>
 *     redis模块自定义内容在这里
 * </p>
 * <p>
 *     使用分布式锁:
 *     <pre>
 *          @Autowired
 *          private RedisLock redisLock;
 *          ...
 *     </pre>
 * </p>
 * <p>
 *     使用redis：
 *     <pre>
 *          @Autowired
 *          private RedisTemplate redisTemplate;
 *     </pre>
 *
 *     ！RedisTemplate 默认支持的是json序列化
 *     如果你仅仅是存储String，使用{@link org.springframework.data.redis.core.StringRedisTemplate}
 *     如果你想用自己的序列化进行存储，需要"自定义"template支持
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

    /**
     *  支持redis分布式锁
     *
     * @return {@link RedisLock}
     */
    @Bean
    public RedisLock redisLock(){
        return new RedisLockImpl();
    }

    /**
     * 自定义Factory，支持"主从分离"
     *
     * 与{@link RedisTemplate}配合使用，默认选择
     *
     * @return
     */
    @Bean
    @Primary
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

    /**
     * 自定义factory，仅从主库读取数据，用于分布式锁等场景
     *
     * 与{@link  MyMainStringRedisTemplate}配合使用
     *
     * @return
     */
    @Bean("myMainLettuceConnectionFactory")
    public MyMainLettuceConnectionFactory myMainLettuceConnectionFactory(){
        if (null == redisProperties.getSentinel()) {
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
            RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
            serverConfig.setPassword(redisProperties.getPassword());
            return new MyMainLettuceConnectionFactory(serverConfig, clientConfig);
        }

        RedisSentinelConfiguration serverConfig = new RedisSentinelConfiguration();

        serverConfig.master(redisProperties.getSentinel().getMaster());
        serverConfig.setPassword(redisProperties.getPassword());
        for (String node : redisProperties.getSentinel().getNodes()) {
            String[] split = node.split(":");
            serverConfig.sentinel(split[0], NumberUtils.toInt(split[1]));
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.MASTER)
                .build();
        return new MyMainLettuceConnectionFactory(serverConfig, clientConfig);
    }

    /**
     * 自定义{@link RedisTemplate} 使用json序列化
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        this.withJacksonSerializer(template);
        return template;
    }

    /**
     * MyMainStringRedisTemplate
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public MyMainStringRedisTemplate myMainStringRedisTemplate(@Qualifier("myMainLettuceConnectionFactory")MyMainLettuceConnectionFactory redisConnectionFactory){
        return new MyMainStringRedisTemplate(redisConnectionFactory);
    }

    /**
     * 设置jackson对redis的对象进行序列化
     * @param template
     */
    private void withJacksonSerializer(RedisTemplate<Object, Object> template){
        template.setKeySerializer(RedisSerializer.json());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.string());
    }

    /**
     * 使用fastjson对redis的value进行序列化
     *
     * @param template
     * @deprecated 不建议使用fastjson
     */
    private void withFastJsonSerializer(RedisTemplate<Object, Object> template){
        ////使用FastJson序列化
        //FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);
        //// value值的序列化采用fastJsonRedisSerializer
        //template.setValueSerializer(fastJsonRedisSerializer);
        //template.setHashValueSerializer(fastJsonRedisSerializer);
        //// key的序列化采用StringRedisSerializer
        //template.setKeySerializer(new StringRedisSerializer());
        //template.setHashKeySerializer(new StringRedisSerializer());
    }

}
