package otaku.info.batch.tasklet;

import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.PythonController;
import otaku.info.controller.TextController;
import otaku.info.controller.TvController;
import otaku.info.entity.Program;

import java.util.*;

@Component
@StepScope
public class TvPostTasklet implements Tasklet {

    @Autowired
    TvController tvController;

    @Autowired
    TextController textController;

    @Autowired
    PythonController pythonController;

    Logger logger = org.slf4j.LoggerFactory.getLogger(TvPostTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("--- TV番組投稿処理 START ---");
        Calendar calToday = Calendar.getInstance();
        Calendar calTmrw = Calendar.getInstance();
        calTmrw.add(Calendar.DATE, 1);
        List<Program> programList = new ArrayList<>();
        int postCount = 0;

        // 本日の情報を取得するのか、明日の情報を取得するのかフラグ
        boolean forToday = calToday.get(Calendar.AM_PM) == Calendar.AM;

        if (forToday) {
            // バッチ実行時間が午前の場合（朝一番を想定）、その日のTV情報を取得する。
            programList = tvController.getTvList(new Date());
        } else {
            // バッチ実行時間が午後の場合（夜を想定）、次の日のTV情報を取得する。
            programList = tvController.getTvList(calTmrw.getTime());
        }

        // 取得結果が0件だったらTwitterのあるグループのみ「出演情報はない」ポストをする。
        if (programList.size() == 0) {
            if (forToday) {
                tvController.allNoTvPost(forToday, calToday.getTime());
            } else {
                tvController.allNoTvPost(forToday, calTmrw.getTime());
            }
            logger.info("--- TV番組投稿処理: 0件 ---");
            logger.info("--- TV番組投稿処理 END ---");
            return RepeatStatus.FINISHED;
        }

        // 全てのグループIDが入ったMap<TeamId, List<Program>>
        Map<Long, List<Program>> tvListMapByGroup = tvController.mapByGroup(programList);

        // Mapの要素１つずつ（=1グループずつ）投稿へ進める
        for (Map.Entry<Long, List<Program>> ele : tvListMapByGroup.entrySet()) {
            String text = null;

            if (ele.getValue().size() == 0) {
                // 出演情報のないグループ

                // 文章を作成
                if (forToday) {
                    text = textController.tvPostNoAlert(ele.getKey(), forToday, calToday.getTime());
                } else {
                    text = textController.tvPostNoAlert(ele.getKey(), forToday, calTmrw.getTime());
                }
            } else {
                // 出演情報のあるグループ
                postCount ++;

                // 文章を作成
                if (forToday) {
                    text = textController.tvPost(ele, forToday, calToday.getTime());
                } else {
                    text = textController.tvPost(ele, forToday, calTmrw.getTime());
                }
            }
            // Twitter post指示
            pythonController.post(ele.getKey().intValue(), text);
        }

        logger.info("--- TV番組投稿グループ数: " + postCount + " ---");
        logger.info("--- TV番組投稿処理 END ---");
        return RepeatStatus.FINISHED;
    }

}