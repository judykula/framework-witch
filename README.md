
# 概要

整套架构的项目名称以DOTA2中的英雄名称命名

## witch

framework cache模块

## 1.0.0

提供如下支持:
```
- 提供自定义template：
    - 读写分离模式 RedisTemplate
    - 仅主节点模式 MyMainStringRedisTemplate
- 自定义使用jackson序列化
- 提供分布式锁RedisLock，支持ttl与wait

```
