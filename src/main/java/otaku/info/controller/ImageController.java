package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.setting.Setting;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 画像を生成します
 *
 */
@Controller
@AllArgsConstructor
public class ImageController {

    @Autowired
    private Setting setting;

    /**
     * 画像を生成します。2行のテキスト、中央配置
     *
     * @param fileName
     * @param text1
     * @param text2
     * @return
     */
    public String createImage(String fileName, String text1, String text2) {
        BufferedImage bufferedImage = new BufferedImage(1200, 630, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();

        // TODO: 色カラフルとか指定とか何かしたい
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // TODO: フォント可愛くしたい
        Font font = null;
        try {
            font = new Font("YuppyTC-Regular", Font.BOLD, 100);
        } catch (Exception e) {
            font = new Font("Arial-Black", Font.BOLD, 100);
        }

        graphics2D.setFont(font);
        // TODO: 文字色赤は渋い
        graphics2D.setColor(Color.RED);

        FontMetrics fm1 = graphics2D.getFontMetrics();
        Rectangle rectText1 = fm1.getStringBounds(text1, graphics2D).getBounds();
        int x1 = 1200/2 - rectText1.width/2;
        int y1 = 630/2 - rectText1.height/2 - fm1.getMaxAscent()/30;

        FontMetrics fm2 = graphics2D.getFontMetrics();
        Rectangle rectText2 = fm2.getStringBounds(text2, graphics2D).getBounds();
        int x2 = 1200/2 - rectText2.width/2;
        int y2 = 630/2 - rectText2.height/2 + fm2.getMaxAscent()*2;

        graphics2D.drawString(text1, x1, y1);
        graphics2D.drawString(text2, x2, y2);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // output
        try {
            String path = setting.getGeneratedImage() + fileName;
            ImageIO.write(bufferedImage, "png", new File(path));
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}