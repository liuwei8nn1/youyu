package com.youyu.common.util;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.Filter;

/**
 * 基于 FastJSON2 的 JSON 工具类
 * 
 * 集中管理 JSON 处理逻辑，便于未来如果 FastJSON2 出现严重 bug 时快速替换
 * 
 * @author demo
 */
public abstract class JsonUtil {

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param object 要序列化的对象
     * @return JSON 字符串，如果对象为 null 则返回 null
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        return JSON.toJSONString(object);
    }

    /**
     * 将对象序列化为格式化的 JSON 字符串（带缩进）
     *
     * @param object 要序列化的对象
     * @return 格式化后的 JSON 字符串
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        return JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat);
    }

    /**
     * 将对象序列化为 JSON 字符串（支持自定义过滤器）
     * 过滤器可用于控制哪些字段被序列化，例如隐藏敏感信息
     *
     * @param object 要序列化的对象
     * @param filters 序列化过滤器（如 PropertyFilter、NameFilter、ValueFilter 等）
     * @return JSON 字符串
     * 
     * 使用示例：
     * <pre>{@code
     * // 隐藏密码字段
     * PropertyFilter filter = (obj, name, value) -> !"password".equals(name);
     * String json = JsonUtil.toJsonWithFilter(user, filter);
     * }</pre>
     */
    public static String toJsonWithFilter(Object object, Filter... filters) {
        if (object == null) {
            return null;
        }
        return JSON.toJSONString(object, filters);
    }

    /**
     * 将 JSON 字符串反序列化为指定类型
     *
     * @param json JSON 字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象，如果 json 为 null 或空则返回 null
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    /**
     * 将 JSON 字符串反序列化为指定泛型类型
     * 用于处理复杂的泛型类型，如 List<User>、Map<String, Object> 等
     *
     * @param json JSON 字符串
     * @param typeReference 包含泛型信息的类型引用
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * 
     * 使用示例：
     * <pre>{@code
     * List<User> users = JsonUtil.fromJson(jsonString, new TypeReference<List<User>>(){});
     * Map<String, Object> map = JsonUtil.fromJson(jsonString, new TypeReference<Map<String, Object>>(){});
     * }</pre>
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSON.parseObject(json, typeReference);
    }



    /**
     * 将 JSON 字符串转换为 List
     *
     * @param json JSON 数组字符串
     * @param clazz 元素类型
     * @param <T> 元素泛型类型
     * @return 对象列表，如果 json 为 null 或空则返回空列表
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        return JSON.parseArray(json, clazz);
    }



    /**
     * 将 JSON 字符串转换为 Map
     *
     * @param json JSON 对象字符串
     * @return Map 对象，如果 json 为 null 或空则返回空 Map
     */
    public static Map<String, Object> toMap(String json) {
        if (json == null || json.isEmpty()) {
            return Map.of();
        }
        return JSON.parseObject(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 将 JSON 字符串解析为 JSONObject
     *
     * @param json JSON 字符串
     * @return JSONObject 对象，如果 json 为 null 或空则返回 null
     */
    public static JSONObject parseObject(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSON.parseObject(json);
    }

    /**
     * 将 JSON 字符串解析为 JSONArray
     *
     * @param json JSON 数组字符串
     * @return JSONArray 对象，如果 json 为 null 或空则返回 null
     */
    public static JSONArray parseArray(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSON.parseArray(json);
    }

    /**
     * 检查字符串是否为合法的 JSON 格式
     *
     * @param json 待检查的字符串
     * @return true 表示是合法 JSON，false 表示不是
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            JSON.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 将对象转换为 JSONObject
     *
     * @param object 源对象
     * @return JSONObject 对象
     */
    public static JSONObject toJsonObject(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        return JSON.parseObject(toJson(object));
    }

    /**
     * 通过 JSON 序列化/反序列化实现对象的深拷贝
     *
     * @param object 要克隆的对象
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 克隆后的对象
     */
    public static <T> T deepClone(T object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        String json = toJson(object);
        return fromJson(json, clazz);
    }

    /**
     * 获取 JSON 字符串的字节数组（UTF-8 编码）
     *
     * @param object 要序列化的对象
     * @return UTF-8 编码的字节数组
     */
    public static byte[] toJsonBytes(Object object) {
        if (object == null) {
            return null;
        }
        return JSON.toJSONBytes(object);
    }

    /**
     * 将 JSON 字节数组反序列化为对象
     *
     * @param bytes JSON 字节数组
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     */
    public static <T> T fromJsonBytes(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return JSON.parseObject(bytes, clazz);
    }

    /**
     * 合并多个对象为一个 JSON 对象
     * 后面的对象会覆盖前面对象中相同的键
     *
     * @param objects 要合并的对象数组
     * @return 合并后的 JSON 字符串
     */
    public static String merge(Object... objects) {
        if (objects == null || objects.length == 0) {
            return "{}";
        }
        
        JSONObject merged = new JSONObject();
        for (Object obj : objects) {
            if (obj != null) {
                JSONObject json = toJsonObject(obj);
                if (json != null) {
                    merged.putAll(json);
                }
            }
        }
        return merged.toJSONString();
    }
}
