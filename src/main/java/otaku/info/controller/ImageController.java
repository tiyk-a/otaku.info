package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.setting.Setting;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // 背景色はランダムに1つ選ぶ
        Color color = randomColor4BackGround();
        graphics2D.setColor(color);
        graphics2D.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        Font font = null;
        try {
            font = new Font("TakaoGothic", Font.BOLD, 100);
//            font = new Font("YuppyTC-Regular", Font.BOLD, 100);
        } catch (Exception e) {
            font = new Font("Arial-Black", Font.BOLD, 100);
        }

        graphics2D.setFont(font);
        // 文字色はDark Brown
        graphics2D.setColor(new Color(51, 0, 0));

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

    /**
     * 画像を生成します。3行のテキスト、中央配置
     * https://qiita.com/macoshita/items/e92f8c74cb2469a3494e
     * https://www.web-dev-qa-db-ja.com/ja/java/graphics2ddrawstring%E3%81%AE%E6%94%B9%E8%A1%8C%E3%81%AB%E9%96%A2%E3%81%99%E3%82%8B%E5%95%8F%E9%A1%8C/970571360/
     *
     * @param fileName
     * @param text1
     * @param text2
     * @return
     */
    public String createImage(String fileName, String text1, String text2, String text3) {
        BufferedImage bufferedImage = new BufferedImage(1200, 630, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();

        // 背景色はランダムに1つ選ぶ
        Color color = randomColor4BackGround();
        graphics2D.setColor(color);
        graphics2D.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        Font font = null;
        int h = 100;
        int margin = 20;
        try {
            font = new Font("TakaoGothic", Font.BOLD, h);
        } catch (Exception e) {
            font = new Font("Arial-Black", Font.BOLD, h);
        }
        graphics2D.setFont(font);

        // 文字色はDark Brown
        graphics2D.setColor(new Color(51, 0, 0));

        // フォントサイズ調整
        int checkY = totalHeight(graphics2D, h, text2, text3, margin);

        // フォント小さくする必要あるならちょうどいいサイズを探しに行く
        while (checkY >= 630) {
            h -= 10;
            checkY = totalHeight(graphics2D, h, text2, text3, margin);
        }

        // フォント小さくする必要あるなら再設定
        if (h != 100) {
            try {
                font = new Font("TakaoGothic", Font.BOLD, h);
            } catch (Exception e) {
                font = new Font("Arial-Black", Font.BOLD, h);
            }
            graphics2D.setFont(font);
        }
        // フォントサイズ調整終わり

        FontMetrics fm = graphics2D.getFontMetrics();
        Rectangle rectText1 = fm.getStringBounds(text1, graphics2D).getBounds();
        int x1 = 1200/2 - rectText1.width/2;
        int y = 630/2 - checkY/2 + h + margin;
        graphics2D.drawString(text1, x1, y);
        y += (h + margin);

        if(fm.stringWidth(text2) < 1200) {
            Rectangle rectText2 = fm.getStringBounds(text2, graphics2D).getBounds();
            int x2 = 1200/2 - rectText2.width/2;
            graphics2D.drawString(text2, x2, y);
        } else {
            String[] words = text2.split(" ");
            String currentLine = words[0];
            int addHeight = 0;

            for(int i = 1; i < words.length; i++) {
                if(fm.stringWidth(currentLine+words[i]) < 1200) {
                    currentLine += " "+words[i];
                } else {
                    Rectangle rectText2 = fm.getStringBounds(currentLine, graphics2D).getBounds();
                    int x2 = 1200/2 - rectText2.width/2;
                    y += addHeight;
                    graphics2D.drawString(currentLine, x2, y);
                    currentLine = words[i];
                    if (addHeight == 0) {
                        addHeight += h;
                    }
                }
            }
            if(currentLine.trim().length() > 0) {
                Rectangle rectText2 = fm.getStringBounds(currentLine, graphics2D).getBounds();
                int x2 = 1200/2 - rectText2.width/2;
                y += addHeight;
                graphics2D.drawString(currentLine, x2, y);
            }
        }

        y += (h + margin);

        if(fm.stringWidth(text2) < 1200) {
            Rectangle rectText3 = fm.getStringBounds(text3, graphics2D).getBounds();
            int x3 = 1200/2 - rectText3.width/2;
            graphics2D.drawString(text3, x3, y);
        } else {
            String[] words = text3.split(" ");
            String currentLine = words[0];
            int addHeight = 0;

            for(int i = 1; i < words.length; i++) {
                if(fm.stringWidth(currentLine+words[i]) < 1200) {
                    currentLine += " "+words[i];
                } else {
                    Rectangle rectText3 = fm.getStringBounds(currentLine, graphics2D).getBounds();
                    int x3 = 1200/2 - rectText3.width/2;
                    y += addHeight;
                    graphics2D.drawString(currentLine, x3, y);
                    currentLine = words[i];
                    if (addHeight == 0) {
                        addHeight += h;
                    }
                }
            }
            if(currentLine.trim().length() > 0) {
                Rectangle rectText3 = fm.getStringBounds(currentLine, graphics2D).getBounds();
                int x3 = 1200/2 - rectText3.width/2;
                y += addHeight;
                graphics2D.drawString(currentLine, x3, y);
            }
        }

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

    /**
     * ランダムに背景となる色を返却します。
     *
     * @return
     */
    private Color randomColor4BackGround() {
        Color[] colorArray = {
                // Lavendar
                new Color(230,230,250),

                // lightcyan
                new Color(224,255,255),

                // honeydew
                new Color(240,255,240),

                // beige
                new Color(245,245,220),

                // lemonchiffon
                new Color(255,250,205),

                // oldlace
                new Color(253,245,230),

                // blanchedalmond
                new Color(255,235,205),

                // lavenderblush
                new Color(255,240,245),

                // mistyrose
                new Color(255,228,225),

                // pink
                new Color(255,192,203),

                // wheat
                new Color(245,222,179),

                // peachpuff
                new Color(255,218,185),

                // navajowhite
                new Color(255,222,173),

                // thistle
                new Color(216,191,216)
        };

        int num =  (int) (Math.random() * (colorArray.length));
        return colorArray[num];
    }

    /**
     * 3行建ての画像がオーバーせずに作れるか高さをチェックする
     *
     * @param graphics2D 画像幅はここから取得
     * @param h フォントサイズ
     * @param text2 2行目の文字列
     * @param text3 3行目の文字列
     * @param margin 2行目と3行目のgap。1と2行目の間にも。
     * @return
     */
    private int totalHeight(Graphics2D graphics2D, Integer h, String text2, String text3, Integer margin) {
        FontMetrics fm = graphics2D.getFontMetrics();
        int y = h + margin;
        if(fm.stringWidth(text2) >= 1200) {
            String[] words = text2.split(" ");
            String currentLine = words[0];
            int addHeight = h;

            for(int i = 1; i < words.length; i++) {
                if(fm.stringWidth(currentLine+words[i]) < 1200) {
                    currentLine += " "+words[i];
                } else {
                    y += addHeight;
                    currentLine = words[i];
                }
            }
            if(currentLine.trim().length() > 0) {
                y += addHeight;
            }
        } else {
            y += h;
        }

        y += margin;

        if(fm.stringWidth(text3) >= 1200) {
            String[] words = text3.split(" ");
            String currentLine = words[0];
            int addHeight = h;

            for(int i = 1; i < words.length; i++) {
                if(fm.stringWidth(currentLine+words[i]) < 1200) {
                    currentLine += " "+words[i];
                } else {
                    y += addHeight;
                    currentLine = words[i];
                }
            }
            if(currentLine.trim().length() > 0) {
                y += addHeight;
            }
        } else {
            y += h;
        }
        return y;
    }
}