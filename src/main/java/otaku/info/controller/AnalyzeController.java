package otaku.info.controller;

import lombok.AllArgsConstructor;

import java.text.ParseException;
import java.util.*;

import org.springframework.stereotype.Controller;
import otaku.info.entity.Item;
import otaku.info.utils.StringUtilsMine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 必要なデータを探し出すの。
 *
 */
@Controller
@AllArgsConstructor
public class AnalyzeController {

    /**
     * 商品を引数に取り、そこから発売日っぽい部分を引き抜き、Dateを返却します。
     *
     * @param item
     * @return
     * @throws ParseException
     */
    public Date generatePublicationDate(Item item) throws ParseException {
        Date result = null;

        // 年月日のデータを集める
        Map<String, List<Date>> resultMap = extractPublishDate(item.getItem_caption());
        if (resultMap.get("publishDateList").size() == 0) {
            Map<String, List<Date>> resultMap2 = extractPublishDate(item.getTitle());
            if (resultMap.get("reserveDueList").size() == 0 && resultMap2.get("reserveDueList").size() > 0) {
                resultMap.put("reserveDueList", resultMap2.get("reserveDueList"));
            }
            if (resultMap.get("publishDateList").size() == 0 && resultMap2.get("publishDateList").size() > 0) {
                resultMap.put("publishDateList", resultMap2.get("publishDateList"));
            }
            if (resultMap.get("dateList").size() == 0 && resultMap2.get("dateList").size() > 0) {
                resultMap.put("dateList", resultMap2.get("dateList"));
            }
        }

        if (resultMap.get("publishDateList").size() > 0 || resultMap.get("dateList").size() > 0) {
            result = resultMap.get("publishDateList").get(0);
            if (item.getPublication_date() == null) {
                result = resultMap.get("dateList").get(0);
            }
        }
        return result;
    }

    /**
     * 予約締切日(reserveDue)/発売日(publishDate)/実発売日(realPublishDate)を見つけます
     *
     * @param text
     * @return
     */
    private Map<String, List<Date>> extractPublishDate(String text) throws ParseException {

        Map<String, List<Date>> resultMap = new HashMap<>();

        // 予約締切日(reserveDue)を探す
        String reserveDueRegexYMD = "予約締切日?.*20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9])";
        List<Date> reserveDueList = StringUtilsMine.convertStringToDateList(regexExtract(reserveDueRegexYMD, text));

        if (reserveDueList.size() == 0) {
            // 年月だけ探す
            String reserveDueRegexYM = "予約締切日?.*20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])";
            reserveDueList = StringUtilsMine.convertStringToDateList(regexExtract(reserveDueRegexYM, text));
        }

        // 発売日(publishDate)を探す
        String publishDateRegexYMD = "(([20]?)[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9]).*[発売](日)?)|(発売日?.*20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9]))";
        List<Date> publishDateList = StringUtilsMine.convertStringToDateList(regexExtract(publishDateRegexYMD, text));

        if (publishDateList.size() == 0) {
            // 年月だけ探す
            String publishDateRegexYM = "発売日?.*20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])";
            publishDateList = StringUtilsMine.convertStringToDateList(regexExtract(publishDateRegexYM, text));
        }

        // 発売日が見つからない場合、日付だけを探す(年月日)
        if (publishDateList.size() == 0) {
            String dateRegexYMD = "20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9])";
            publishDateList = StringUtilsMine.convertStringToDateList(regexExtract(dateRegexYMD, text));
        }
        // 発売日が見つからない場合、日付だけを探す(年月)
        if (publishDateList.size() == 0) {
            // 年月だけ探す
            String dateRegexYM = "20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])";
            publishDateList = StringUtilsMine.convertStringToDateList(regexExtract(dateRegexYM, text));
        }
        // 発売日が見つからない場合、日付だけを探す(年)
        List<Date> dateList = new ArrayList<>();
        if (publishDateList.size() == 0) {
            // 年月だけ探す
            String dateRegexYM = "20[0-2]{1}[0-9]{1}";
            dateList = StringUtilsMine.convertStringToDateList(regexExtract(dateRegexYM, text));
        }

        resultMap.put("reserveDueList", reserveDueList);
        resultMap.put("publishDateList", publishDateList);
        resultMap.put("dateList", dateList);

        return resultMap;
    }

    /**
     * 正規表現で
     * @param regex
     * @param text
     * @return
     */
    public List<String> regexExtract(String regex, String text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<String> publishDateList = new ArrayList<>();
        while (matcher.find()) {
            publishDateList.add(matcher.group());
        }
        return publishDateList;
    }

    /**
     * 文字列から年月を見つけ、返します。
     * 雑誌の発売号などが引っかかる想定。
     *
     * @param text
     * @return
     */
    public List<String> extractYMList(String text) {
        String regex = "20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<String> publishDateList = new ArrayList<>();
        while (matcher.find()) {
            publishDateList.add(matcher.group());
        }
        return publishDateList;
    }
}
