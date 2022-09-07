package com.atguigu.gmall.common.constant;

public class SysRedisConst {

    public static final String NULL_VAL = "x";
    public static final String LOCK_SKU_DETAIL = "lock:sku:detail:";
    public static final Long NULL_VAL_TTL = 60*30L;
    public static final Long SKUDETAIL_TTL = 60*60*24*7L;

    public static final String SKU_INFO_PREFIX = "sku:info:";

    public static final String BLOOM_SKUID = "bloom:skuid";
    public static final String CACHE_CATEGORYS = "categorys";
    public static final int SEARCH_PAGE_SIZE = 8;

    public static final String SKU_HOTSCORE_PREFIX = "sku:hotscore:"; //49
    public static final String LOGIN_USER = "user:login:"; //拼接token
    public static final String USERID_HEADER = "userid";
}
