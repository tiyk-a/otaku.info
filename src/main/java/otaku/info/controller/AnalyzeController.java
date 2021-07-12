package otaku.info.controller;

import lombok.AllArgsConstructor;

import java.text.ParseException;
import java.util.*;

import org.springframework.stereotype.Controller;
import otaku.info.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@AllArgsConstructor
public class AnalyzeController {

    /**
     * 予約締切日(reserveDue)/発売日(publishDate)/実発売日(realPublishDate)を見つけます
     *
     * @param text
     * @return
     */
    public Map<String, List<Date>> extractPublishDate(String text) throws ParseException {

        Map<String, List<Date>> resultMap = new HashMap<>();

        // 予約締切日(reserveDue)を探す
        String reserveDueRegexYMD = "予約締切日?.*20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9])";
        List<Date> reserveDueList = StringUtils.convertStringToDateList(regexExtract(reserveDueRegexYMD, text));

        if (reserveDueList.size() == 0) {
            // 年月だけ探す
            String reserveDueRegexYM = "予約締切日?.*20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])";
            reserveDueList = StringUtils.convertStringToDateList(regexExtract(reserveDueRegexYM, text));
        }

        // 発売日(publishDate)を探す
        String publishDateRegexYMD = "発売日?.*20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9])";
        List<Date> publishDateList = StringUtils.convertStringToDateList(regexExtract(publishDateRegexYMD, text));

        if (publishDateList.size() == 0) {
            // 年月だけ探す
            String publishDateRegexYM = "発売日?.*20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])";
            publishDateList = StringUtils.convertStringToDateList(regexExtract(publishDateRegexYM, text));
        }

        // 発売日が見つからない場合、日付だけを探す(年月日)
        if (publishDateList.size() == 0) {
            String dateRegexYMD = "20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9])";
            publishDateList = StringUtils.convertStringToDateList(regexExtract(dateRegexYMD, text));
        }
        // 発売日が見つからない場合、日付だけを探す(年月)
        if (publishDateList.size() == 0) {
            // 年月だけ探す
            String dateRegexYM = "20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])";
            publishDateList = StringUtils.convertStringToDateList(regexExtract(dateRegexYM, text));
        }
        // 発売日が見つからない場合、日付だけを探す(年)
        List<Date> dateList = new ArrayList<>();
        if (publishDateList.size() == 0) {
            // 年月だけ探す
            String dateRegexYM = "20[0-2]{1}[0-9]{1}";
            dateList = StringUtils.convertStringToDateList(regexExtract(dateRegexYM, text));
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
     * 文字列から年月日をみつけ、返します。
     * 発売日、予約締切日などが引っかかる想定。
     *
     * @param text
     * @return
     */
    public List<String> extractYMDList(String text) {
        String regex = "20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9])";
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
