package otaku.info.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.setting.Setting;

@Component
public class ServerUtils {

    @Autowired
    StringUtilsMine stringUtilsMine;

    @Autowired
    Setting setting;

    public void sleep() {
        try{
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
