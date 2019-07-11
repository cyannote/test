package com.cyannote.test;

import com.cyannote.utils.JedisUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import javax.sound.midi.Soundbank;
import java.util.List;
import java.util.Map;
import java.util.Set;

/****
 *  author:tan
 *  data:2019-07-11 19:57
 *  description:
 **/
public class TestJedis {
    @Test
    public void demo1(){
        Jedis jedis = new Jedis("192.168.72.142",6379);
        String pong = jedis.ping();
        System.out.println(pong);
    }

    //String
    @Test
    public void demo2() throws InterruptedException {
        Jedis jedis = JedisUtils.getJedis();
        //赋值
        jedis.set("name","tom");
        //取值
        String name = jedis.get("name");
//        System.out.println(name);
        jedis.set("age","18");
        //自加一
        Long incr = jedis.incr("age");
        System.out.println(incr);
        //自减一
        Long decr = jedis.decr("age");
        System.out.println(decr);
        //增加固定值
        Long incrBy = jedis.incrBy("age", 5);
        System.out.println(incrBy);
        //减少固定值
        Long decrBy = jedis.decrBy("age", 10);
        System.out.println(decrBy);

        //拼接
        jedis.append("name", "is a boy");
        String append = jedis.get("name");
        System.out.println(append);

        //设置有效时间
        jedis.setex("time",5,"5");
       /* while (jedis.exists("time")){
            System.out.println(jedis.ttl("time"));
            Thread.sleep(1000);
        }*/

        //为已存在的key设置有效时长
        jedis.expire("name",5);
        while (jedis.exists("name")){
            Thread.sleep(1000);
            System.out.println(jedis.ttl("name"));
        }
        Set<String> keys = jedis.keys("*");
        System.out.println(keys);
        //关闭资源qa
        jedis.close();
    }

    //hash
    @Test
    public void demo3(){
        Jedis jedis = JedisUtils.getJedis();

        jedis.hset("map","name1","tom");
        jedis.hset("map","name2","jack");

        /*String name = jedis.hget("map", "name");*/
        /*List<String> names = jedis.hmget("map", "name1", "name2");*/
       /* Map<String, String> map = jedis.hgetAll("map");*/
        Set<String> map = jedis.hkeys("map");
        List<String> hvals = jedis.hvals("map");
        System.out.println(map);
        System.out.println(hvals);
        Boolean hexists = jedis.hexists("map", "age");
        System.out.println(hexists);
        jedis.close();
    }


    //list
    @Test
    public void demo4(){
        Jedis jedis = JedisUtils.getJedis();
        jedis.lpush("list","a","b","c","d");
        String list = jedis.rpop("list");
//        System.out.println(list);

        List<String> list1 = jedis.lrange("list", 0, -1);
        System.out.println(list1);
        Long account = jedis.llen("list");
        System.out.println(account);

        Long lrem = jedis.lrem("list", 4, "d");
        System.out.println(lrem);
        List<String> list2 = jedis.lrange("list", 0, -1);
        System.out.println(list2);

        Long linsert = jedis.linsert("list", BinaryClient.LIST_POSITION.AFTER, "c", "and");
        System.out.println(linsert);
        List<String> list3 = jedis.lrange("list", 0, -1);
        System.out.println(list3);

        //将链表中的尾部元素弹出并添加到头部
        String rpoplpush = jedis.rpoplpush("list", "list");
        System.out.println(rpoplpush);
        List<String> list4 = jedis.lrange("list", 0, -1);
        System.out.println(list4);
    }

    //set
    @Test
    public void demo5(){
        Jedis jedis = JedisUtils.getJedis();
        jedis.sadd("list2", "a", "b", "c", "d");

        Set<String> list = jedis.smembers("list2");
        System.out.println(list);
        //判断元素是否存在
        Boolean flag = jedis.sismember("list2", "d");
        System.out.println(flag);

        //获取set中的成员的数量
        Long account = jedis.scard("list2");
        System.out.println(account);

        //随机返回一个元素
        String s = jedis.srandmember("list2");
        System.out.println(s);

        jedis.sadd("list3","q","a","z","b");

        //将key1、key2相差的成员存储在destination上
        Long sdiffstore = jedis.sdiffstore("list2", "list3");
        System.out.println(sdiffstore);
               //将返回的交集存储在destination上:
        Long sinterstore = jedis.sinterstore("list2", "list3");
        System.out.println(sinterstore);

//        将返回的并集存储在destination上:
        Long sunionstore = jedis.sunionstore("list2", "list3");
        System.out.println(sunionstore);
    }

    //SortedSet
    @Test
    public void demo6(){
        Jedis jedis = JedisUtils.getJedis();

        jedis.zadd("book",80,"三国");
        jedis.zadd("book",75,"水浒");
        jedis.zadd("book",78,"西游记");
        jedis.zadd("book",83,"红楼梦");

        Long zrank = jedis.zrank("book", "三国");
        System.out.println(zrank);

        //从小到大
        Set<String> book = jedis.zrange("book", 0, -1);
        System.out.println(book);
        //从大到小
        Set<String> list = jedis.zrevrange("book", 0, -1);
        System.out.println(list);

        //从小到大包含成绩
        Set<Tuple> book1 = jedis.zrangeWithScores("book", 0, -1);
        for (Tuple tuple : book1) {
            String element = tuple.getElement();
            double score = tuple.getScore();
            System.out.println(element + "      "+score);
        }

        //返回分数在[min,max]的成员并按照分数从低到高排序。
        Set<String> book2 = jedis.zrangeByScore("book", 80, 90);
        System.out.println(book2);

        //获取分数在[min,max]之间的成员
        Long book3 = jedis.zcount("book", 79, 82);
        System.out.println(book3);
    }
}
