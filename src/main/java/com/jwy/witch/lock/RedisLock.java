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

import java.io.Closeable;

/**
 * <p>
 *     定义Redis lock的接口
 * </p>
 *
 * @author Jiang Wanyu
 * @version 1.0
 * @date 2023/11/10
 */
public interface RedisLock extends Closeable {

    /**
     * 添加上分布式锁，并指定锁的时间
     *
     * ！如果没活上锁成功，会不断尝试，直至获取到锁为止
     *
     * @param lockKey
     * @param ttl 指定锁的时间, millisecond
     * @return true上锁成功&反之失败
     * @throws RedisLockException
     */
    boolean acquire(String lockKey, long ttl) throws RedisLockException;

    /**
     * 添加上分布式锁，并指定锁的时间，同时设置等待超时
     *
     * @param lockKey
     * @param ttl 指定锁的时间, millisecond
     * @param timeout 等待时间the time to wait for the lock
     * @return
     * @throws RedisLockException
     */
    boolean acquireTimeout(String lockKey, long ttl, long timeout) throws RedisLockException, InterruptedException;

    /**
     * 尝试上分布式锁，并指定锁的时间
     *
     * 不同于{@link #acquire(String, long)} 不会阻塞线程
     *
     * @param ttl 指定锁的时间
     * @return true上锁成功&反之失败
     * @throws RedisLockException
     */
    boolean tryAcquire(String lockKey, long ttl) throws RedisLockException;

}
