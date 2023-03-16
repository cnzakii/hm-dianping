package com.hmdp;

import cn.hutool.core.util.RandomUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisIdWork;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl service;

    @Resource
    private RedisIdWork redisIdWork;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    private final ExecutorService executorService = Executors.newFixedThreadPool(500);

    @Test
    void testRedisIdWork() {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWork.nextId("order");
                System.out.println("id = " + id);
                latch.countDown();
            }
        };
        long begin = System.currentTimeMillis();

        for (int i = 0; i < 300; i++) {
            executorService.submit(task);
        }
        long end = System.currentTimeMillis();
    }


    @Test
    void test() throws InterruptedException {
        Result result = service.queryById(1L);
        System.out.println(result);
    }

    @Test
    void loadShopData() {
        List<Shop> list = service.list();

        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));

        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY + typeId;
            List<Shop> value = entry.getValue();

            for (Shop shop : value) {
                stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());
            }

        }
    }

    @Test
    void testHyperLogLog() {

        String[] values = new String[1000];

        for (int i = 0; i < values.length; i++) {
            values[i] = "user_" + RandomUtil.randomNumbers(6);
        }

        stringRedisTemplate.opsForHyperLogLog().add("hl3", values);
    }
}
