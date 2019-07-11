package com.cyannote.utils;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/****
 *  author:tan
 *  data:2019-07-11 19:45
 *  description:
 **/
public class JedisUtils {
    private static JedisPool jedisPool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);//最大连接数
        config.setMaxIdle(50);  //最大闲时的数量
        config.setMinIdle(25);  //最小闲时的数量
        // 注意:  千万别在等号左侧  写上 "JedisPool jedisPool " 而应该写成 "jedisPool"
        jedisPool = new JedisPool(config,"192.168.72.142",6379);
    }

    // 获取连接的方法
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }

    /*@Test
    public void demo(){
        Jedis jedis = jedisPool.getResource();
        String pong = jedis.ping();
        System.out.println(pong);
    }*/
}
