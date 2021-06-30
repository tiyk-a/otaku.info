package otaku.info.controller;

import org.springframework.stereotype.Controller;
import otaku.info.dto.TwiDto;

/**
 * 
 */
@Controller
public class TextController {

    public String twitter(TwiDto twiDto) {
        String result = "新商品の情報です！%0A%0A" + twiDto.title + "%0A" + twiDto.url;
        return result;
    }
}
