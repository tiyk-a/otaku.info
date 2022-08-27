package otaku.info.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.setting.Setting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ServerUtils {

    @Autowired
    Setting setting;

    /**
     * 10秒スリープ
     *
     */
    public void sleep() {
        try{
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("ServerUtilsのエラー");
            e.printStackTrace();
        }
    }

    /**
     * 使用できるパスを見つけ、返却します
     * 楽天の画像で使用することを想定
     *
     * @param imagePath
     * @return
     */
    public String availablePath(String imagePath) {
        String newPath = setting.getImageItem() + imagePath + ".png";
        Path path = Paths.get(newPath);
        Integer count = 1;

        while (Files.exists(path)) {
            newPath = setting.getImageItem() + imagePath + "_" + count.toString() + ".png";
            path = Paths.get(newPath);
            ++count;
        }
        return newPath;
    }
}
