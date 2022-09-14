package com.atguigu.gmall.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.util.StringUtils;

import java.util.Map;

public class Jsons {
    private static ObjectMapper mapper = new ObjectMapper();
    /**
     * 把对象转为json字符串
     * @param object
     * @return
     */
    public static String toStr(Object object) {
        //jackson
        try {
            String s = mapper.writeValueAsString(object);
            return s;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 带复杂泛型的json逆转。这个可以直接兼容
     * toObj(String jsonStr, Class<T> clz)
     * @param jsonStr
     * @param tr
     * @param <T>
     * @return
     */
    public static<T> T toObj(String jsonStr, TypeReference<T> tr){
        if(StringUtils.isEmpty(jsonStr)){
            return null;
        }
        T t = null;
        try {

            t = mapper.readValue(jsonStr, tr);
            return t;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 把json转为普通对象
     * @param jsonStr
     * @param clz
     * @param <T>
     * @return
     */
    public static<T>  T toObj(String jsonStr, Class<T> clz) {
        if(StringUtils.isEmpty(jsonStr)){
            return null;
        }

        T t = null;
        try {

            t = mapper.readValue(jsonStr, clz);
            return t;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 把MQ消息内容转成指定对象
     * @param message
     * @param clz
     * @param <T>
     * @return
     */
    public static<T> T  toObj(Message message,
                              Class<T> clz) {

        String json = new String(message.getBody());
        return toObj(json,clz);
    }
}
