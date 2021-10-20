package otaku.info.setting;

import org.apache.log4j.*;

/**
 * https://kght6123.page/posts/Java/Log4j/%E8%A8%AD%E5%AE%9A%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%E4%B8%8D%E8%A6%81%E3%81%A7%E6%A8%99%E6%BA%96%E5%87%BA%E5%8A%9B%E3%81%ABLog4j%E3%82%92%E4%BD%BF%E3%81%84%E3%81%9F%E3%81%84/
 * https://takadayuichi.hatenablog.com/entry/20121202/1354449787
 */
public class Log4jUtils {
    // 一番基底のLoggerを作るだけのメソッド
    public static Logger newLogger(final String name, final Appender appender, final Level level) {
        final Logger logger = Logger.getLogger(name);
        logger.addAppender(appender);
        logger.setLevel(level);
        return logger;
    }

    // SimpleLayoutでログを標準出力するLoggerを作るメソッド
    public static Logger newConsoleSimpleAllLogger(final String name) {
        return newConsoleAllLogger(name, new SimpleLayout());
    }

    // TTCCLayoutでログを標準出力するLoggerを作るメソッド
    public static Logger newConsoleTTCCAllLogger(final String name) {
        return newConsoleAllLogger(name, new TTCCLayout());
    }

    /**
     * 今これ使ってます
     * PatternLayout（CSV）でログを標準出力するLoggerを作るメソッド
     * TODO: %F:%Lは負荷が高いらしいので落ち着いたら削除しましょう。対処法としては、引数にstringを入れて各ファイルでクラスメイを渡してあげて、出力にその引数の値を入れてあげれば良いのでは
     * %c	カテゴリー名。 %c{1} と記述した場合、一番「下の」 レベルのみ出力できる。
     * 「sample.pg.LoggerSample」の場合、「LoggerSample」となる。
     * %C	ログを生成したクラス名。カテゴリ名では無くクラス名。
     * %C{1} と記述した場合、一番「下の」 レベルのみ出力できる。
     * 「sample.pg.LoggerSample」の場合、「LoggerSample」となる。
     * %d	日付。 %d{yyyy-MMM-dd HH:mm:ss,SSS} の様に詳細に指定できる。
     * %l	%F、%L、%Mを纏めた情報。※性能に問題あり。
     * %F	ログを生成したソースファイル名。※性能に問題あり
     * %L	ログを生成した箇所のソースの行番号。※性能に問題あり
     * %M	ログを生成したメソッドの名前。※性能に問題あり
     * %m	ログメッセージ
     * %x	NDC でpushした値
     * %X{key}	MDC に保存された key の値
     * %n	改行コードを生成する。
     * %p	ログレベル（FATALやINFOなど）
     * %r	アプリケーションが開始してからの通算時間（ミリ秒）
     * %t	ログを生成したスレッド名
     *
     * @return
     */
    public static Logger newConsoleCsvAllLogger(String name) {
        return newConsolePatternAllLogger(name, "%d{yyyy/MM/dd HH:mm:ss.SSS},%l,%r,%t,%-5p,%m%n");
    }

    /**
     * PatternLayout（パターンは指定する）でログを標準出力するLoggerを作るメソッド
     * @param name
     * @param pattern
     * @return
     */
    public static Logger newConsolePatternAllLogger(final String name, final String pattern) {
        return newConsoleAllLogger(name, new PatternLayout(pattern));
    }

    /**
     * コンソールにログを出力するLoggerを作るだけのメソッド
     */
    public static Logger newConsoleAllLogger(final String name, final Layout layout) {
        return newLogger(name, new ConsoleAppender(layout), Level.ALL);
    }

    public static Logger newFileLogger(final String name, String fileName) {
        Layout layout = new PatternLayout("%d{yyyy/MM/dd HH:mm:ss.SSS},%l,%r,%t,%-5p,%m%n");
        Logger logger = newLogger(name, new ConsoleAppender(layout), Level.ALL);
        try {
            logger = newLogger(name, new DailyRollingFileAppender(layout, fileName, "'.'yyyy-MM-dd"), Level.ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logger;
    }
}
