package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import otaku.info.controller.LoggerController;
import otaku.info.controller.PythonController;
import otaku.info.controller.TvController;
import otaku.info.controller.TwTextController;
import otaku.info.entity.PMVer;

import java.util.*;

@Component
@StepScope
public class TvPostTasklet implements Tasklet {

    @Autowired
    TvController tvController;

    @Autowired
    TwTextController twTextController;

    @Autowired
    PythonController pythonController;

    @Autowired
    LoggerController loggerController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Calendar calToday = Calendar.getInstance();
        Calendar calTmrw = Calendar.getInstance();
        calTmrw.add(Calendar.DATE, 1);
        List<PMVer> pmVerList = new ArrayList<>();
        int postCount = 0;

        // 本日の情報を取得するのか、明日の情報を取得するのかフラグ
        boolean forToday = calToday.get(Calendar.AM_PM) == Calendar.AM;

        if (forToday) {
            // バッチ実行時間が午前の場合（朝一番を想定）、その日のTV情報を取得する。
            pmVerList = tvController.getTvList(new Date());
        } else {
            // バッチ実行時間が午後の場合（夜を想定）、次の日のTV情報を取得する。
            pmVerList = tvController.getTvList(calTmrw.getTime());
        }

        // 全てのグループIDが入ったMap<TeamId, List<Program>>
        if (pmVerList.size() > 0) {
            Map<Long, List<PMVer>> tvListMapByGroup = tvController.mapByGroup(pmVerList);

            // Mapの要素１つずつ（=1グループずつ）投稿へ進める
            for (Map.Entry<Long, List<PMVer>> ele : tvListMapByGroup.entrySet()) {
                String text = "";

                if (ele.getValue().size() > 0) {
                    // 出演情報のあるグループ
                    postCount ++;

                    // 文章を作成
                    if (forToday) {
                        text = twTextController.tvPost(ele, forToday, calToday.getTime(), ele.getKey());
                    } else {
                        text = twTextController.tvPost(ele, forToday, calTmrw.getTime(), ele.getKey());
                    }
                }
                // Twitter post指示
                if (StringUtils.hasText(text)) {
                    pythonController.post((long) ele.getKey().intValue(), text);
                }
            }
        }

        loggerController.printTvPostTasklet("--- TV番組投稿グループ数: " + postCount + " ---");
        return RepeatStatus.FINISHED;
    }

}
