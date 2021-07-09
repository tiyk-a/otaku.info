package otaku.info.controller;

import org.springframework.stereotype.Controller;
import otaku.info.dto.TwiDto;

/**
 * 色々、投稿用のテキストを生成します。
 *
 */
@Controller
public class TextController {

    /**
     * Twitterポスト用のメッセージを作成します。
     *
     * @param twiDto
     * @return
     */
    public String twitter(TwiDto twiDto) {
        String result = "新商品の情報です！%0A%0A" + twiDto.title + "%0A" + twiDto.url;
        return result;
    }
}
