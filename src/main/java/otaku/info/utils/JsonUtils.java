package otaku.info.utils;

import org.json.JSONArray;

public class JsonUtils {

    public static boolean isJsonArray(String target) {
        return target.startsWith("[");
    }

    public static boolean isJsonArray(Object target) {
        return target instanceof JSONArray;
    }
}
