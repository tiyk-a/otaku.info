package otaku.info.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.dto.TwiDto;
import otaku.info.entity.Item;
import otaku.info.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 色々、投稿用のテキストを生成します。
 *
 */
@Controller
public class TextController {

    @Autowired
    private DateUtils dateUtils;

    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");

    /**
     * Twitterポスト用のメッセージを作成します。
     *
     * @param twiDto
     * @return
     */
    public String twitter(TwiDto twiDto) {
        return "新商品の情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl();
    }

    public String futureItemReminder(TwiDto twiDto) {
        int diff = dateUtils.dateDiff(new Date(), twiDto.getPublication_date());
        return "予約はお済みですか？%0A%0A【発売まで" + diff + "日】%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl();
    }

    public String countdown(Item item) {
        int diff = dateUtils.dateDiff(new Date(), item.getPublication_date());
        String str1 = "発売まであと" + diff + "日！%0A%0A" + item.getTitle() + "%0A%0A";
        String str2 = item.getUrl();
        String result;
        int length = str1.length() + str2.length() + "%0A%0A".length();
        if (length < 140) {
            result = str1 + item.getItem_caption().substring(0, 140 - length) + "%0A%0A" + str2;
        } else {
            result = str1 + str2;
        }
        return result;
    }

    public String releasedItemAnnounce(Item item) {
        String str1 = "本日発売！%0A%0A" + item.getTitle() + "%0A%0A";
        String str2 = item.getUrl();
        String result;
        int length = str1.length() + str2.length() + "%0A%0A".length();
        if (length < 140) {
            result = str1 + item.getItem_caption().substring(0, 140 - length) + "%0A%0A" + str2;
        } else {
            result = str1 + str2;
        }
        return result;
    }

    public String twitterPerson(TwiDto twiDto, String memberName) {
        String result = memberName + "君の新商品情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl();
        if (result.length() + memberName.length() < 139) {
            result = memberName + "君の新商品情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A#" + memberName + "%0A#" + twiDto.getUrl();
        }
        return result;
    }
}
