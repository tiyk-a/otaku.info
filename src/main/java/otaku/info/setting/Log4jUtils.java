package otaku.info.setting;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.TTCCLayout;

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
     *
     * @return
     */
    public static Logger newConsoleCsvAllLogger() {
        return newConsolePatternAllLogger("name", "%d{yyyy/MM/dd HH:mm},%F:%L,%x,%r,%t,%-5p,%m%n");
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
}
