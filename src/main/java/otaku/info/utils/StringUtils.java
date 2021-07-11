package otaku.info.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文字列操作
 *
 */
public class StringUtils {

    /**
     * 第二引数で指定した長さで、第一引数の文字列を丸める。
     * https://hacknote.jp/archives/3537/
     *
     * @param src 元の文字列
     * @param length 丸めの長さ
     * @return ●処理後の文字列
     */
    public static String compressString(String src, int length) {
        if (src == null || src.length() == 0 || length <= 0) {
            return src;
        }

        String subject;
        if (src.length() > length) {
            subject = src.substring(0, length);
            subject += "・・・";
        } else {
            subject = src;
        }
        return subject;
    }

    public static List<Date> convertStringToDateList(List<String> list) throws ParseException {
        List<Date> dateList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        for (String str : list) {
            String str1 = str.replaceAll("[^\\d]", "");
            if (str1.length() == 7) {
                str1 = str1.substring(0,3) + "0" + str1.substring(4);
            } else if (str1.length() == 6) {
                str1 = str1 + "01";
            } else if (str1.length() == 5) {
                str1 = str1.substring(0,3) + "0" + str1.substring(4) + "01";
            } else if (str1.length() == 4) {
                str1 = str1.substring(0,3) + "0101";
            }
            if (str1.length() == 8) {
                Date date = new Date(sdf.parse(str1).getTime());
                dateList.add(date);
            }
        }
        return dateList;
    }
}
