package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWork {

    /**
     * 开始时间戳
     */
    private static final long BEGIN_TIME = 1640995200;
    private static final int COUNT_BITS = 32;

    private static final String PREFIX = "icr:";


    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public long nextId(String keyPrefix) {
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIME;

        // 2.生成序列号
        // 2.1 获取当前日期，精确到天
        String data = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));

        Long count = stringRedisTemplate.opsForValue().increment(PREFIX + keyPrefix + ":" + data);

        // 3.拼接并返回
        return timeStamp << COUNT_BITS | count;
    }

}
