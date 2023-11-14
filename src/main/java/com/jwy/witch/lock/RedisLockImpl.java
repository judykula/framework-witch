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
package com.jwy.witch.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     redis分布式锁的默认实现类
 * </p>
 *
 * @author Jiang Wanyu
 * @version 1.0
 * @date 2023/11/10
 */
@Slf4j
public class RedisLockImpl implements RedisLock{

    /**默认锁的过期时间*/
    private final long TTL_DEFAULT =  TimeUnit.SECONDS.toMillis(30);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;//TODO 因为涉及到分布式，这个需要全部在主库上操作，规避主从同步问题

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean acquire(String lockKey) throws RedisLockException {
        return acquireTimeout(lockKey, TTL_DEFAULT, -1);
    }

    @Override
    public boolean acquire(String lockKey, long ttl) throws RedisLockException {
        return acquireTimeout(lockKey, ttl, -1);
    }

    @Override
    public boolean acquireTimeout(String lockKey, long ttl, long timeout) throws RedisLockException {
        return false;
    }

    @Override
    public boolean tryAcquire(String lockKey) throws RedisLockException {
        return this.tryAcquire(lockKey, TTL_DEFAULT);
    }

    @Override
    public boolean tryAcquire(String lockKey, long ttl) throws RedisLockException {

        log.debug("【RLI069】try acquire lock for key: {}", lockKey);

        long expireValue4Set = System.currentTimeMillis() + ttl + 1;//设置锁的存在时间
        long expireTime = Math.min(expireValue4Set*2, TimeUnit.MINUTES.toMillis(5));//设置锁的过期时间，默认x2 ，最大5min
        if(expireValue4Set > TimeUnit.MINUTES.toMillis(3)) {
            log.warn("【RLI076】Locked for key: {} too long: {}", lockKey, expireValue4Set);
        }

        /*尝试第一次直接设置锁*/
        boolean done = this.stringRedisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(expireValue4Set), Duration.ofMillis(expireTime));
        if(done) return true;

        /*尝试第二次，判断value-锁的持续时间设置锁*/
        String valueOfGet = this.stringRedisTemplate.opsForValue().get(lockKey);
        if(StringUtils.isEmpty(valueOfGet)){
            boolean done2 = this.stringRedisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(expireValue4Set), Duration.ofMillis(expireTime));
            return done2;
        }
        long expireTimeSeted = NumberUtils.toLong(valueOfGet);
        if(expireTimeSeted >= System.currentTimeMillis()){
            //锁未被释放，加锁失败
            return false;
        }else{
            //锁已经过期
            String oldValue = this.stringRedisTemplate.opsForValue().getAndSet(lockKey, String.valueOf(expireValue4Set));
            if(null != oldValue && !valueOfGet.equals(oldValue)) {
                //表示锁被其他人抢到了
                return false;
            }
            this.stringRedisTemplate.expire(lockKey, Duration.ofMillis(expireTime));
            return true;
        }
    }
}
