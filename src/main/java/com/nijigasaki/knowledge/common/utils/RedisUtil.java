package com.nijigasaki.knowledge.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nijigasaki.knowledge.common.enums.error.BusinessError;
import com.nijigasaki.knowledge.common.utils.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public void setObject(final String key, final Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            throw new BusinessException(BusinessError.REDIS_SET_OBJECT_FAILED);
        }
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public void setObject(final String key, final Object value, final Long timeout, final TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            throw new BusinessException(BusinessError.REDIS_SET_OBJECT_FAILED);
        }
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        boolean expire = false;
        try {
            expire = expire(key, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new BusinessException(BusinessError.REDIS_EXPIRE_FAILED);
        }
        return expire;
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        boolean expire = false;
        try {
            expire = Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            throw new BusinessException(BusinessError.REDIS_EXPIRE_FAILED);
        }
        return expire;
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public Object getObject(final String key) {
        Object o = null;
        try {
            o = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new BusinessException(BusinessError.REDIS_GET_DATA_FAILED);
        }
        return o;
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getObject(final String key, final Class<T> clazz) {
        Object o;
        try {
            o = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            throw new BusinessException(BusinessError.REDIS_GET_DATA_FAILED);
        }
        String s = JSONObject.toJSONString(o);
        return JSONObject.parseObject(s, clazz);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection<String> collection) {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public long setList(final String key, final Collection<Object> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public List<Object> getList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getList(final String key, final Class<T> clazz) {
        List<Object> objectList = redisTemplate.opsForList().range(key, 0, -1);
        String s = JSONArray.toJSONString(objectList);
        return JSONArray.parseArray(s, clazz);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public void setSet(final String key, final Set dataSet) {
        redisTemplate.opsForSet().add(key, dataSet);
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public Set<Object> getSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public void setMap(final String key, final Map<String, Object> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public Map getMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public void setMap(final String key, final String hKey, final Object value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public Object getMap(final String key, final String hKey) {
        return redisTemplate.opsForHash().get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public List getMap(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }
}
