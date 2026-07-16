package com.example.newcommunity.util;

public class RedisKeyUtil {

    private static final String SPLIT=":";
    //统称帖子和评论为实体
    private static final String PREFIX_ENTITY_LIKE="like:entity";

    //某个实体的赞,可以看见谁给我点赞了
    //like：entity：entityType：entityId->set(userId)
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

}
