package otaku.info.controller;

import org.springframework.stereotype.Controller;
import otaku.info.dto.TwiDto;

import java.text.SimpleDateFormat;

/**
 * 色々、投稿用のテキストを生成します。
 *
 */
@Controller
public class TextController {

    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");

    /**
     * Twitterポスト用のメッセージを作成します。
     *
     * @param twiDto
     * @return
     */
    public String twitter(TwiDto twiDto) {
        return "新商品の情報です！%0A%0A" + twiDto.title + "%0A発売日：" + sdf1.format(twiDto.publication_date) + "%0A" + twiDto.url;
    }

    public String futureItemReminder(TwiDto twiDto) {
        return "今後発売予定商品の情報です！%0A%0A" + twiDto.title + "%0A発売日：" + sdf1.format(twiDto.publication_date) + "%0A" + twiDto.url;
    }
}
