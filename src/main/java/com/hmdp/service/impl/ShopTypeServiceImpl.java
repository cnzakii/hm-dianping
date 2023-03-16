package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryAllType() {


        // 1.从redis中查询所有购物类型
        List<String> shopTypeJsonList  = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, -1);
        System.out.println(shopTypeJsonList);


        // 2.判断是否存在
        if (shopTypeJsonList != null && shopTypeJsonList.size() > 0) {
            // 3.存在，返回
            List<ShopType> shopTypeList = new ArrayList<>();
           for (String StrShop:shopTypeJsonList){
               ShopType shopType = JSONUtil.toBean(StrShop,ShopType.class);
               shopTypeList.add(shopType);
           }
            System.out.println(shopTypeList);

            return Result.ok(shopTypeList);
        }

        // 4.不存在，查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();

        // 5.不存在，返回错误
        if (shopTypeList == null || shopTypeList.size() == 0) {
            return Result.fail("购物类型不存在!");
        }

        // 6.存在，存入redis
        for (ShopType shopType : shopTypeList) {
            String shopTypeJson = JSONUtil.toJsonStr(shopType);
            shopTypeJsonList.add(shopTypeJson);
        }

        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY,shopTypeJsonList);

        return Result.ok(shopTypeList);
    }
}
