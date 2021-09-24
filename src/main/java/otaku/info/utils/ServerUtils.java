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
    StringUtilsMine stringUtilsMine;

    @Autowired
    Setting setting;

    public void sleep() {
        try{
            Thread.sleep(10000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * 使用できるパスを見つけ、返却します
     * 識別子も対応
     *
     * @param imagePath Absolute pathで渡すこと
     *                  "ex. Rakuten) https://thumbnail.image.rakuten.co.jp/@0_mall/book/cabinet/1058/4582515771058_1_2.jpg?_ex=128x128"
     *                  "ex. Generated) "
     * @return サーバー上に引数のimageを保存するパス
     */
    public String availablePath(String imagePath) {

        String newPath = imagePath;

        Path path = Paths.get(newPath);
        Integer count = 1;

        while (Files.exists(path)) {
            newPath = imagePath.replaceAll("\\?.*$", "") + "_" + count.toString() + stringUtilsMine.extractSubstring(newPath, "\\?.*$");
            path = Paths.get(newPath);
            ++count;
        }
        return newPath;
    }
}
