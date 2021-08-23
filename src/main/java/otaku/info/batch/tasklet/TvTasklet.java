package otaku.info.batch.tasklet;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import otaku.info.controller.TvController;
import otaku.info.searvice.TeamService;

@Component
@StepScope
public class TvTasklet implements Tasklet {

    static final String TV_URL = "https://www.tvkingdom.jp/schedulesBySearch.action";

    @Autowired
    TvController tvController;

    @Autowired
    TeamService teamService;

    Logger logger = org.slf4j.LoggerFactory.getLogger(TvTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("--- TV検索 START ---");

        List<String> teamNameList = teamService.findAllTeamName();

        for (String artist : teamNameList) {
            boolean nextFlg = true;
            String urlWithParam = TV_URL;
            String param = "?stationPlatformId=0&condition.keyword=" + artist + "&submit=%E6%A4%9C%E7%B4%A2";

            urlWithParam += param;

            logger.info(artist + "の番組を検索します");
            while (nextFlg) {
                // URLアクセスして要素を取得、次ページアクセスのためのパラメタを返す。
                param = jsopConnect(urlWithParam, artist);

                if (param.equals("")) {
                    nextFlg = false;
                }
                urlWithParam = TV_URL + param;
            }
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        logger.info("--- TV検索 END ---");
        return RepeatStatus.FINISHED;
    }

    /**
     * Jsoupでhtml要素を取ってきて、必要データを取り出して次ページへのパラメタを返却。
     * 必要データは登録まで行う。
     *
     * @param url
     * @return
     * @throws IOException
     */
    private String jsopConnect(String url, String teamName) throws IOException {
        // URLにアクセスして要素を取ってくる
        Document document = Jsoup.connect(url).get();

        // 必要な要素を取り出す
        Elements elements = document.select("div.utileList");
        Map<String, String[]> tvMap = new HashMap<>();
        for (Element e : elements) {
            // 異なるチャンネルで同じ番組の放送があるため、「詳細」がkey、「タイトル」はvalue[0](被る可能性がある)。value[1]には詳細画面へのURL
            String[] valueArr = {e.getElementsByTag("h2").text(), e.getElementsByTag("a").first().attr("abs:href")};
            tvMap.put(e.getElementsByClass("utileListProperty").text(), valueArr);
        }
        tvController.tvKingdomSave(tvMap, teamName);

        // 次のページがあるか確認する
        Element nextBtn = document.select("div.listIndexNum").first();
        // 次ページのパラメタを返却
        return nextBtn.select("a.linkArrowE").attr("href");
    }
}
