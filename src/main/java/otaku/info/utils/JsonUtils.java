package otaku.info.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils extends JSONObject {

    public JSONObject createJsonObject(String source) {
        String str = formatJsonString(source);
        System.out.println(str);

        JSONObject jo = null;
        try {
            jo = new JSONObject(str);
        } catch (Exception e) {
            // TODO: jsonにエラーが見つかったらどうするかの対処を考えたい。ファイルに書き出すか？同じの取りに行かないようにしたいな？
            e.printStackTrace();
        }

        if (jo == null) {
            jo = new JSONObject();
        }
        return jo;
    }

    public static boolean isJsonArray(String target) {
        return target.startsWith("[");
    }

    public static boolean isJsonArray(Object target) {
        return target instanceof JSONArray;
    }

    private String formatJsonString(String target) {
        String res = target.replaceAll("”", "\"");
        String res1 = res.replaceAll("“", "\"");

//        checkDoubleQuoteEscape(res1);
        return res1;
    }

//    private String checkDoubleQuoteEscape(String target) {
//        if (target.contains(""))
//    }
}
