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

import com.jwy.witch.template.MyMainStringRedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

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

    @Autowired
    private MyMainStringRedisTemplate redisTemplate;

    @Override
    public boolean acquire(String lockKey, long ttl) throws RedisLockException {
        return acquireTimeout(lockKey, ttl, -1);
    }

    @Override
    public boolean acquireTimeout(String lockKey, long ttl, long timeout) throws RedisLockException {

        int timeSum = 0;
        while(!tryAcquire(lockKey, ttl)){
            long rd = RandomUtils.nextLong(50, 80);
            try {
                Thread.sleep(rd);
            } catch (InterruptedException e) {
                throw new RedisLockException(e);
            }

            if(timeout < 0) continue;
            timeSum += rd;
            if(timeSum >= timeout){
                log.debug("【RLI064】latency time with: {}", timeout);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean tryAcquire(String lockKey, long ttl) throws RedisLockException {

        Assert.isTrue(ttl > 0, "ttl must be a positive number");
        log.debug("【RLI069】try acquire lock for key: {}", lockKey);

        long expireValue4Set = System.currentTimeMillis() + ttl + 1;//设置锁的存在时间
        String expireValue4SetStr = String.valueOf(expireValue4Set);
        long expireTime = Math.min(expireValue4Set*2, TimeUnit.MINUTES.toMillis(5));//设置锁的过期时间，默认x2 ，最大5min
        if(expireValue4Set > TimeUnit.MINUTES.toMillis(3)) {
            log.warn("【RLI076】Locked for key: {} too long: {}", lockKey, expireValue4Set);
        }

        /*尝试第一次直接设置锁*/
        boolean done = this.redisTemplate.opsForValue().setIfAbsent(lockKey, expireValue4SetStr, Duration.ofMillis(expireTime));
        if(done) return true;

        /*尝试第二次，判断value-锁的持续时间设置锁*/
        String valueOfGet = this.redisTemplate.opsForValue().get(lockKey);
        if(StringUtils.isEmpty(valueOfGet)){
            boolean done2 = this.redisTemplate.opsForValue().setIfAbsent(lockKey, expireValue4SetStr, Duration.ofMillis(expireTime));
            return done2;
        }
        long expireTimeSeted = NumberUtils.toLong(valueOfGet);
        if(expireTimeSeted >= System.currentTimeMillis()){
            //锁未被释放，加锁失败
            return false;
        }else{
            //锁已经过期
            String oldValue = this.redisTemplate.opsForValue().getAndSet(lockKey, expireValue4SetStr);
            if(null != oldValue && !valueOfGet.equals(oldValue)) {
                //表示锁被其他人抢到了
                return false;
            }
            this.redisTemplate.expire(lockKey, Duration.ofMillis(expireTime));
            return true;
        }
    }
}
