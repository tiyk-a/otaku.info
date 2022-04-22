package otaku.info.utils;

import org.springframework.stereotype.Component;

@Component
public class ServerUtils {

    /**
     * 10秒スリープ
     *
     */
    public void sleep() {
        try{
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
