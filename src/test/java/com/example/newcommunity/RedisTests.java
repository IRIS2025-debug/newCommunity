package com.example.newcommunity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;


@SpringBootTest
@ContextConfiguration(classes = NewCommunityApplication.class)
public class RedisTests {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
        String redisKey="test:count";
        //调用template存储
        redisTemplate.opsForValue().set(redisKey,100);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    //访问哈希
    @Test
    public void testHashes(){
        String redisKey="test:hash";
        redisTemplate.opsForHash().put(redisKey,"id","1");
        redisTemplate.opsForHash().put(redisKey,"username","张三");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));

    }

    //访问列表
    @Test
    public void testLists(){
        String redisKey="test:ids";

        redisTemplate.opsForList().leftPush(redisKey,"101");
        redisTemplate.opsForList().leftPush(redisKey,"102");
        redisTemplate.opsForList().leftPush(redisKey,"103");

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));

    }

    @Test
    public void testSets(){
        String redisKey="test:teachers";

        redisTemplate.opsForSet().add(redisKey,"刘备","关羽","张飞","赵云","马超","黄忠");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    @Test
    public void testSortedSets(){
        String redisKey="test:students";

        redisTemplate.opsForZSet().add(redisKey,"zhangsan",80);
        redisTemplate.opsForZSet().add(redisKey,"guanyu",90);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"guanyu"));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"zhangsan"));
        //默认从小到大排列，反转之后变成从大到小排列
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"guanyu"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,1));


    }

    @Test
    public void testKey(){
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);

    }


    //多次访问同一个Key
    @Test
    public void testBoundOperations(){
        String redisKey="test:count";
        //把key提前绑定，不用每一次都写一遍key
        BoundValueOperations operations=redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }


    @Test
    public void testTransactional(){
        Object obj=redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey="test:tx";
                operations.multi();
                operations.opsForSet().add(redisKey, "jennie");
                operations.opsForSet().add(redisKey, "lisa");
                operations.opsForSet().add(redisKey, "roosie");

                System.out.println(operations.opsForSet().members(redisKey));

                return operations.exec();
            }
        });
        System.out.println(obj);
    }
}






