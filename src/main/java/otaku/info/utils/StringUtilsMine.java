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
public class StringUtilsMine {

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
            String year;
            String month;
            String date;
            String fullDate = null;
            String[] array = str.split("[^0-9]");
            if(array.length == 3) {
                year = array[0].length() == 4 ? array[0] : "20" + array[0];
                month = array[1].length() == 2 ? array[1] : "0" + array[1];
                date = array[2].length() == 2 ? array[2] : "0" + array[2];

                if (year.length() == 4 && month.length() == 2 && date.length() == 2) {
                    fullDate = year + month + date;
                }
            } else if (array.length == 2) {
                if (array[0].length() == 4 && Integer.parseInt(array[0]) > 2020 && Integer.parseInt(array[0]) < 2026
                    && array[1].length() == 2 && Integer.parseInt(array[1]) > 0 && Integer.parseInt(array[1]) < 32) {
                    fullDate = array[0] + array[1] + "01";
                } else if (array[0].length() == 2 && Integer.parseInt(array[0]) > 20 && Integer.parseInt(array[0]) < 26
                    && array[1].length() == 2 && Integer.parseInt(array[1]) > 0 && Integer.parseInt(array[1]) < 32) {
                    fullDate = "20" + array[0] + array[1] + "01";
                }
            } else if (array.length == 1 && array[0].length() == 8) {
                fullDate = array[0];
            }
            if (fullDate != null && fullDate.length() == 8) {
                Date date1 = new Date(sdf.parse(fullDate).getTime());
                dateList.add(date1);
            }
        }
        return dateList;
    }
}
