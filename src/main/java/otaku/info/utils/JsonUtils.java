package otaku.info.utils;

import org.json.JSONArray;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils {

    public static boolean isJsonArray(String target) {
        return target.startsWith("[");
    }

    public static boolean isJsonArray(Object target) {
        return target instanceof JSONArray;
    }
}
