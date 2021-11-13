package otaku.info.utils;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.entity.ErrorJson;
import otaku.info.service.ErrorJsonService;
import otaku.info.setting.Log4jUtils;

@Component
public class JsonUtils extends JSONObject {

    @Autowired
    ErrorJsonService errorJsonService;

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("JsonUtils");

    public JSONObject createJsonObject(String source) {
        String str = formatJsonString(source);

        JSONObject jo = null;
        try {
            jo = new JSONObject(str);
        } catch (Exception e) {
            // エラーになったら、文字列修正なしの元データでjsonにできるか試してみる
            try {
                jo = new JSONObject(source);
            } catch (Exception e2) {
                // json元データでも作れなかった場合、DB確認してエラーjsonデータなかったら作ってあげる
                if (!errorJsonService.isExists(source)) {
                    ErrorJson errj = new ErrorJson();
                    errj.setJson(source);
                    errj.set_solved(false);
                    errorJsonService.save(errj);
                    logger.debug(e.getMessage());
                } else {
                    logger.debug("前にもあったjson parseエラーです");
                }
                logger.debug(str);
            }
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
