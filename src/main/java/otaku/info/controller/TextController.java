package otaku.info.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import otaku.info.dto.TwiDto;
import otaku.info.entity.Item;
import otaku.info.entity.Program;
import otaku.info.searvice.StationService;
import otaku.info.searvice.TeamService;
import otaku.info.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 投稿用のテキストを色々と生成します。
 *
 */
@Controller
public class TextController {

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private TeamService teamService;

    @Autowired
    private StationService stationService;

    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("MM'/'dd");
    private SimpleDateFormat sdf3 = new SimpleDateFormat("hh:mm");

    /**
     * Twitterポスト用のメッセージを作成します。
     *
     * @param twiDto
     * @return
     */
    public String twitter(TwiDto twiDto) {
        return "【PR】新商品の情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl();
    }

    public String futureItemReminder(TwiDto twiDto) {
        int diff = dateUtils.dateDiff(new Date(), twiDto.getPublication_date());
        return "【PR 発売まで" + diff + "日】%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl();
    }

    public String countdown(Item item) {
        int diff = dateUtils.dateDiff(new Date(), item.getPublication_date());
        String str1 = "【PR】発売まであと" + diff + "日！%0A%0A" + item.getTitle() + "%0A%0A";
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
        String str1 = "【PR】本日発売！%0A%0A" + item.getTitle() + "%0A%0A";
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
        String result = "【PR】" + memberName + "君の新商品情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A" + twiDto.getUrl();
        if (result.length() + memberName.length() < 135) {
            result = "【PR】" + memberName + "君の新商品情報です！%0A%0A" + twiDto.getTitle() + "%0A発売日：" + sdf1.format(twiDto.getPublication_date()) + "%0A#" + memberName + "%0A#" + twiDto.getUrl();
        }
        return result;
    }

    /**
     * TV出演情報がないグループのTwitter投稿文
     *
     * @param teamId Teamテーブルのteam_id
     * @param forToday 今日の情報(TRUE)、明日の情報(FALSE)
     * @param date 情報の日付
     * @return
     */
    public String tvPostNoAlert(Long teamId, boolean forToday, Date date) {
        String dateStr = forToday ? "今日(" + sdf2.format(date) + ")" : "明日(" + sdf2.format(date) + ")";
        return dateStr + "の" + teamService.getTeamName(teamId) + "のTV出演情報はありません。";
    }

    /**
     * TV出演情報があるグループのTwitter投稿文
     *
     * @param ele Mapから抜き取ったEntry(key=TeamId,value=List<Program>)
     * @param forToday 今日の情報(TRUE)、明日の情報(FALSE)
     * @param date 情報の日付
     * @return
     */
    public String tvPost(Map.Entry<Long, List<Program>> ele, boolean forToday, Date date) {
        String dateStr = forToday ? "今日(" + sdf2.format(date) + ")" : "明日(" + sdf2.format(date) + ")";
        String result = dateStr + "の" + teamService.getTeamName(ele.getKey()) + "のTV出演情報です。%0A%0A";

        String info = null;
        for (Program p : ele.getValue()) {
            info = info + sdf3.format(p.getOn_air_date()) + " " + p.getTitle() + " (" + stationService.getStationName(p.getStation_id()) + ")%0A";
        }
        return result + info;
    }
}
