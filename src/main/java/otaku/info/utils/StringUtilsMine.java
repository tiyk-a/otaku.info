package otaku.info.utils;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Optional<String> getAmazonLinkFromCard(String imAmazonImage) {
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
}
