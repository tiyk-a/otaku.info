package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.LoggerController;
import otaku.info.controller.PythonController;
import otaku.info.controller.TwTextController;
import otaku.info.entity.*;
import otaku.info.enums.TeamEnum;
import otaku.info.service.PMService;
import otaku.info.utils.StringUtilsMine;

import java.time.LocalDateTime;
import java.util.*;

@Component
@StepScope
public class TvAlertTasklet implements Tasklet {

    @Autowired
    PythonController pythonController;

    @Autowired
    TwTextController twTextController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    PMService pmService;

    /**
     * 1つの番組を同時間に複数放送局で放送される場合、
     * 1つのツイートにまとめる
     * 複数チームにまたがる場合、別アカの場合、それぞれてツイート
     * 総合アカの場合、まとめて1回ツイート
     *
     * 文言は1つで同じ
     *
     * @param contribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // これから1時間以内に放送開始し、チームIDも埋まっているレコードを取得する。
        List<PM> pmList = pmService.findByOnAirDateNotDeleted(LocalDateTime.now(),1);
        loggerController.printTvAlertTasklet("*** pmList: " + pmList.size());
        if (pmList.size() > 0) {
            // Postする番組の投稿文を作る Map<TeamId, text>
            Map<Long, String> postMap = new HashMap<>();
            for (PM pm : pmList) {
                // teamIdに依存しない部分の文章だけ作る
                String text = twTextController.tvAlert(pm);

                List<Long> teamIdList = StringUtilsMine.stringToLongList(pm.getTeamArr());
                // generalブログのteamIdを詰めていくリスト
                List<Long> generalBlogTeamIdList = new ArrayList<>();
                for (Long teamId : teamIdList) {
                    if (TeamEnum.get(teamId).getTw_id().equals("")) {
                        generalBlogTeamIdList.add(teamId);
                    } else {
                        // そのチームのおすすめを追加する
                        text = text + twTextController.createRecomItemText(teamId);
                        postMap.put(teamId, text);
                    }
                }

                if (generalBlogTeamIdList.size() > 0) {
                    Long id;
                    if (generalBlogTeamIdList.size() == 1) {
                        id = generalBlogTeamIdList.get(0);
                    } else {
                        id = generalBlogTeamIdList.get(new Random().nextInt(generalBlogTeamIdList.size()));
                    }
                    text = text + twTextController.createRecomItemText(id);
                    postMap.put(generalBlogTeamIdList.get(0), text);
                }
            }

            // Post(総合Twitterは1投稿だけする)
            if (postMap.size() > 0) {
                for (Map.Entry<Long, String> post : postMap.entrySet()) {
                    pythonController.post(post.getKey(), post.getValue());
                }
            }
        }
        return RepeatStatus.FINISHED;
    }
}