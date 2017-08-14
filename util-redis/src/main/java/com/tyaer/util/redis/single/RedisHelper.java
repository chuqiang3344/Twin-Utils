package com.tyaer.util.redis.single;

/**
 * Created by Twin on 2017/1/4.
 */

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * redis操作类
 *
 * @author mg
 */
public abstract class RedisHelper {
    protected static final Logger LOGGER = Logger.getLogger(RedisHelper.class.getName());
    //jedis操作工具
    private JedisPool jedisPool;
    //Redis服务器IP
    private String redis_ip;
    //Redis服务器端口
    private int redis_port;
    //Redis服务器连接密码
    private String redis_psw;

    protected RedisHelper(String redis_ip, int redis_port, String redis_psw) {
        this.redis_ip = redis_ip;
        this.redis_port = redis_port;
        this.redis_psw = redis_psw;
        init();
    }

    public void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        //设置最大连接数, 默认8个
        config.setMaxTotal(300);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(2000);
        //设置最大空闲连接数, 默认8个
        config.setMaxIdle(20);
        //初始化jedisPool,设置IP和端口号
        jedisPool = new JedisPool(new JedisPoolConfig(), redis_ip, redis_port);
    }

    public String getKeyType(String key) {
        String type = getJedis().type(key);
//        switch (type){
//
//        }
        return type;
    }

    protected Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        jedis.auth(redis_psw);
        return jedis;
    }

    public void closeJedisPool() {
        jedisPool.close();
    }

    public Long del(String key){
        System.err.println("删除队列："+key);
        Long del = getJedis().del(key);
        return del;
    }

//    public Long saveData(String key,String data){
//
//    }
//
//    public  int saveData(String key,List<String> data){
//
//    }

//    public abstract Set<String> scanDatas(String key);

}
