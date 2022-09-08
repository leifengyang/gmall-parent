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
    public static final String USERTEMPID_HEADER = "usertempid";
    public static final String CART_KEY = "cart:user:"; //用户id或临时id
    //购物车中商品条目总数限制
    public static final long CART_ITEMS_LIMIT = 200;

    //单个商品数量限制
    public static final Integer CART_ITEM_NUM_LIMIT = 200;
}
