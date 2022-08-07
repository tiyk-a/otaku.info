package otaku.info.utils;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文字列操作
 *
 */
@Component
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

    /**
     * 引数1が引数2の中に含まれているかどうかをチェックする
     *
     * @param arg1
     * @param arg2
     * @return
     */
    public static boolean arg2ContainsArg1(String arg1, String arg2) {
        if (arg2.contains(arg1)) {
            return true;
        }

        // arg1にスペースがあったら切り取って検索もする
        if (arg1.contains(" ")) {
            return arg2.contains(arg1.replaceAll(" ", ""));
        }
        return false;
    }

    /**
     * 正規表現に合致するsubstringを返却します
     *
     * @param target regexに合致する部分を探すstring
     * @param regex regex
     * @return
     */
    public String extractSubstring(String target, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(target);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    /**
     * IMのamazon_imageを引数として渡すと、pureなアフィリリンクを返却します
     * リンク抜き出しに成功した場合、その文字列を
     * 失敗したらemptyを返します
     *
     * @param imAmazonImage
     * @return
     */
    public static Optional<String> getAmazonLinkFromCard(String imAmazonImage) {
        // propertiesに定数は用意したんだけどSettingインポートするとファイル依存ができてしまって困るのでそのまま文字列入れてる
        String url = imAmazonImage.replace("<a target=\"_blank\"  href=\"", "").replace("\"><img border=\"0\" src=\"//ws-fe.amazon-adsystem.com/widgets/q?_encoding=UTF8&MarketPlace=JP&ASIN=B09GYY4M9H&ServiceVersion=20070822&ID=AsinImage&WS=1&Format=_SL250_&tag=tiyk8ank-22\" ></a>", "");
        if (url.startsWith("https://www.amazon.co.jp/") && !url.contains("\"")) {
            return Optional.of(url);
        } else {
            return Optional.empty();
        }
    }

    /**
     * 半角アルファベットを全角アルファベットに変換します
     * http://www7a.biglobe.ne.jp/~java-master/samples/string/HankakuAlphabetToZenkakuAlphabet.html
     *
     * @param s
     * @return
     */
    public String alphabetTo2BytesAlphabet(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'z') {
                sb.setCharAt(i, (char)(c - 'a' + 'ａ'));
            } else if (c >= 'A' && c <= 'Z') {
                sb.setCharAt(i, (char)(c - 'A' + 'Ａ'));
            }
        }
        return sb.toString();
    }

    /**
     *
     *
     * @param strNum
     * @return
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * 引数1の文字列末尾に引数2をカンマ区切りで追加し配列の形にします
     *
     * @param str1
     * @param str2
     * @return
     */
    public static String addToStringArr(String str1, String str2) {
        String res = "";

        if (str1 == null || str1.equals("")) {
            res = str2;
        } else {
            List<String> tmpList = stringToStrList(str1);
            if (!tmpList.contains(str2)) {
                tmpList.add(str2);
            }
            res = tmpList.toString();
        }
        return res;
    }

    /**
     * 引数1の文字列末尾に引数2をカンマ区切りで追加し配列の形にします
     * (引数1の配列に引数2が存在しない場合のみ)
     *
     * @param str1
     * @param num
     * @return
     */
    public static String addToStringArr(String str1, Number num) {
        String res = "";

        if (str1 == null || str1.equals("")) {
            res = num.toString();
        } else {
            List<Long> tmpList = stringToLongList(str1);
            if (!tmpList.contains(num)) {
                tmpList.add((long) num);
            }
            res = tmpList.toString();
        }
        return res;
    }

    /**
     * 引数1と引数2のカンマ区切りのstring配列を比較し、要素が一致しているか判定します
     *
     * @param str1
     * @param str2
     * @return
     */
    public static boolean sameElementArrays(String str1, String str2) {
        String[] arr1 = str1.split(",");
        String[] arr2 = str2.split(",");

        if (arr1.length != arr2.length) {
            return false;
        }

        for (String s1 : arr1) {
            if (Arrays.stream(arr2).noneMatch(e -> e.equals(s1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 引数1と引数2を比較し、引数1に含まれていない引数2の要素をStringで返します
     *
     * @param str1
     * @param str2
     * @return
     */
    public static String elemsToSave(String str1, String str2) {
        String[] arr1 = str1.split(",");
        String[] arr2 = str2.split(",");
        String res = "";

        for (String s2 : arr2) {
            if (Arrays.stream(arr1).noneMatch(a -> a.equals(s2))) {
                res = addToStringArr(res, s2);
            }
        }
        return res;
    }

    /**
     *
     * @param str
     * @return
     */
    public static List<Long> stringToLongList(String str) {
        String[] arr = str.split(",");
        return Arrays.stream(arr).map(e -> Long.parseLong(e)).collect(Collectors.toList());
    }

    /**
     *
     * @param str
     * @return
     */
    public static List<String> stringToStrList(String str) {
        String[] arr = str.split(",");
        return Arrays.stream(arr).collect(Collectors.toList());
    }
}
