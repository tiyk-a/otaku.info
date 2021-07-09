package otaku.info.utils;

public class StringUtils {

    /**
     * 第二引数で指定した長さで、第一引数の文字列を丸める。
     * https://hacknote.jp/archives/3537/
     *
     * @param src
     *          元の文字列
     * @param length
     *          丸めの長さ
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

    /**

     * 文字列の置換を行う

     *

     * @param input 処理の対象の文字列

     * @param pattern 置換前の文字列

     * @oaram replacement 置換後の文字列

     * @return 置換処理後の文字列

     */

    static public String substitute(String input, String pattern, String replacement) {

        // 置換対象文字列が存在する場所を取得

        int index = input.indexOf(pattern);



        // 置換対象文字列が存在しなければ終了

        if(index == -1) {

            return input;

        }



        // 処理を行うための StringBuffer

        StringBuffer buffer = new StringBuffer();



        buffer.append(input.substring(0, index) + replacement);



        if(index + pattern.length() < input.length()) {

            // 残りの文字列を再帰的に置換

            String rest = input.substring(index + pattern.length(), input.length());

            buffer.append(substitute(rest, pattern, replacement));

        }

        return buffer.toString();

    }



    /**

     * SQL文出力用に次の置換を行う

     * ' -> ''

     * \ -> \\

     *

     * @param input 置換対象の文字列

     * @return 置換処理後の文字列

     */

    static public String escapeSQL(String input) {

        input = substitute(input, "'", "''");

        input = substitute(input, "\\", "\\\\");

        return input;

    }


}
