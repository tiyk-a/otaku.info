package otaku.info.batch.tasklet;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import otaku.info.controller.LoggerController;
import otaku.info.controller.TvController;
import otaku.info.enums.MemberElimEnum;
import otaku.info.enums.MemberEnum;
import otaku.info.enums.TeamEnum;
import otaku.info.setting.Setting;
import otaku.info.utils.StringUtilsMine;

@Component
@StepScope
public class TvTasklet implements Tasklet {

    @Autowired
    TvController tvController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    StringUtilsMine stringUtilsMine;

    @Autowired
    Setting setting;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        loggerController.printTvTasklet("グループごと検索 START");
        List<String> teamNameList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getName).collect(Collectors.toList());
        mainTransaction(false);
        loggerController.printTvTasklet("グループごと検索 END");

        loggerController.printTvTasklet("個人検索 START");
        mainTransaction(true);
        loggerController.printTvTasklet("個人検索 END");
        System.out.println("end");
        return RepeatStatus.FINISHED;
    }

    /**
     * グループ・個人どちらも検索できるこのクラスのメイン処理
     *
     * @param memFlg
     * @param memFlg
     */
    private void mainTransaction(boolean memFlg) throws IOException {

        // <Name, Team/Member Id>
        Map<String, Long> wordMap = null;
        if (!memFlg) {
            // 嵐以外のチーム名を入れる
            // 嵐はヒットが多すぎるので検索停止中
            wordMap = Arrays.stream(TeamEnum.values()).filter(e -> !e.getId().equals(TeamEnum.ARASHI.getId())).collect(Collectors.toMap(TeamEnum::getName, TeamEnum::getId));
        } else {
            wordMap = Arrays.stream(MemberEnum.values()).collect(Collectors.toMap(MemberEnum::getName, MemberEnum::getId));
        }

        for (Map.Entry<String, Long> artist : wordMap.entrySet()) {
            boolean nextFlg = true;
            String urlWithParam = setting.getTvKingdom();

            String param = "?stationPlatformId=0&condition.keyword=" + artist.getKey() + "&submit=%E6%A4%9C%E7%B4%A2";

            urlWithParam += param;

            loggerController.printTvTasklet(artist.getKey() + "の番組を検索します");
            while (nextFlg) {
                // URLアクセスして要素を取得、次ページアクセスのためのパラメタを返す。
                if (!memFlg) {
                    param = jsopConnect(urlWithParam, artist.getValue(), null);
                } else {
                    param = jsopConnect(urlWithParam, null, artist.getValue());
                }

                if (param.equals("")) {
                    nextFlg = false;
                }
                urlWithParam = setting.getTvKingdom() + param;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                loggerController.printTvTasklet("TV検索エラー");
                e.printStackTrace();
            }
        }
    }

    /**
     * Jsoupでhtml要素を取ってきて、必要データを取り出し保存して次ページへのパラメタを返却。
     *
     * @param url
     * @return
     * @throws IOException
     */
    private String jsopConnect(String url, Long teamId, Long memberId) {

        Element nextBtn = null;

        try {
            // URLにアクセスして要素を取ってくる
            Document document = Jsoup.connect(url).get();
            // 必要な要素を取り出す
            Elements elements = document.select("div.utileList");
            Map<String, String[]> tvMap = new HashMap<>();
            for (Element e : elements) {
                // 異なるチャンネルで同じ番組の放送があるため、「詳細」がkey、「タイトル」はvalue[0](被る可能性がある)。value[1]には詳細画面へのURL
                if (e != null && e.getElementsByTag("h2") != null && e.getElementsByTag("a") != null
                        && e.getElementsByTag("a").first() != null && e.getElementsByTag("a").first().attr("abs:href") != null
                        && e.getElementsByClass("utileListProperty") != null) {

                    // キンプリ、NEWS、嵐など関係ないTV情報もヒットしやすいチームの場合、詳細ページも確認し不要なものは排除する。
                    if (teamId != null && TeamEnum.get(teamId).getChkTvDetailByMemName() != null ) {
                        String detailPageUrl = e.getElementsByTag("a").first().attr("abs:href");
                        Boolean res = isValidInfo(detailPageUrl, teamId);

                        // 取得すべきデータじゃなかったらリストに詰めず次に進む
                        if (!res) {
                            continue;
                        }
                    }

                    // メンバー名が類似別人と一致してる場合、削除
                    if (memberId != null) {
                        if (MemberElimEnum.hasElimData(memberId)) {
                            String detailPageUrl = e.getElementsByTag("a").first().attr("abs:href");
                            List<String> keywordList = MemberElimEnum.getElimNameList(memberId);

                            Boolean isValidFlg = true;
                            for (String kw : keywordList) {
                                Boolean res = isValidInfo(detailPageUrl, kw);

                                // 取得すべきデータじゃなかったらリストに詰めず次に進む
                                if (!res) {
                                    isValidFlg = false;
                                    break;
                                }
                            }

                            if (!isValidFlg) {
                                continue;
                            }
                        }
                    }
                    String[] valueArr = {e.getElementsByTag("h2").text(), e.getElementsByTag("a").first().attr("abs:href")};
                    tvMap.put(e.getElementsByClass("utileListProperty").text(), valueArr);
                }
            }
            tvController.tvKingdomSave(tvMap, teamId, memberId);

            // 次のページがあるか確認する
            nextBtn = document.select("div.listIndexNum").first();
        } catch (Exception e) {
            loggerController.printTvTasklet("TV検索エラー");
            e.printStackTrace();
        }

        if (nextBtn != null) {
            // 次ページのパラメタを返却
            return nextBtn.select("a.linkArrowE").attr("href");
        } else {
            return "";
        }
    }

    /**
     * 正しい情報であることを確認する
     *
     * @return
     */
    public Boolean isValidInfo(String detailUrl, Long teamId) {

        try {
            // URLにアクセスして要素を取ってくる
            Document document = Jsoup.connect(detailUrl).get();

            // 必要な要素を取り出す
            Elements elements = document.select("p.basicTxt");

            Boolean isValid = false;
            for (Element e : elements) {
                System.out.println(e.text());

                // team名からチェックしていいならこの中に入る
                if (TeamEnum.get(teamId).getChkTvDetailByMemName().equals(false)) {
                    // 直接チーム名が出てきたらOK
                    String teamName = TeamEnum.get(teamId).getName();
                    if (e.text().contains(teamName) || e.text().contains(teamName.replace(" ", ""))) {
                        isValid = true;
                        break;
                    }

                    // 全角でもチェック
                    String zenkakuTeamName = stringUtilsMine.alphabetTo2BytesAlphabet(teamName);
                    if (e.text().contains(zenkakuTeamName) || e.text().contains(zenkakuTeamName.replace(" ", ""))) {
                        isValid = true;
                        break;
                    }
                }

                // メンバー名が出てくるか見る
                for (MemberEnum me :Arrays.stream(MemberEnum.values()).filter(m -> m.getTeamId().equals(teamId)).collect(Collectors.toList())) {
                    String memberName = me.getName();
                    if (e.text().contains(memberName) || e.text().contains(memberName.replace(" ", ""))) {
                        isValid = true;
                        break;
                    }

                    // 全角でもチェック
                    String zenkakuMemName = stringUtilsMine.alphabetTo2BytesAlphabet(memberName);
                    if (e.text().contains(zenkakuMemName) || e.text().contains(zenkakuMemName.replace(" ", ""))) {
                        isValid = true;
                        break;
                    }
                }
            }
            return isValid;
        } catch (Exception e) {
            loggerController.printTvTasklet("TV検索エラー");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 正しい情報であることを確認する。
     * 引数2のstringが文章に含まれている場合、false。
     *
     * @return
     */
    public Boolean isValidInfo(String detailUrl, String keyWord) {

        try {
            // URLにアクセスして要素を取ってくる
            Document document = Jsoup.connect(detailUrl).get();

            // 必要な要素を取り出す
            Elements elements = document.select("p.basicTxt");

            Boolean isValid = true;
            for (Element e : elements) {
                System.out.println(e.text());

                if (e.text().contains(keyWord) || e.text().contains(keyWord.replace(" ", ""))) {
                    isValid = false;
                    break;
                }
            }
            return isValid;
        } catch (Exception e) {
            loggerController.printTvTasklet("TV検索エラー");
            e.printStackTrace();
        }
        return true;
    }
}
