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
import otaku.info.enums.BlogEnum;
import otaku.info.enums.TeamEnum;
import otaku.info.service.PMService;
import otaku.info.utils.StringUtilsMine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//    @Autowired
//    PmVerService pmVerService;

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
            // pmIdでverをまとめる
            // pmId, List<PMVer>
//            Map<Long, List<PMVer>> pmPmVerMap = new HashMap<>();
//            for (PMVer pmVer : pmVerList) {
//                List<PMVer> verList;
//                if (pmPmVerMap.containsKey(pmVer.getPm_id())) {
//                    verList = pmPmVerMap.get(pmVer.getPm_id());
//                } else {
//                    verList = new ArrayList<>();
//                }
//                verList.add(pmVer);
//                pmPmVerMap.put(pmVer.getPm_id(), verList);
//            }

            // Postする番組の投稿文を作る Map<TeamId, text>
            Map<Long, String> postMap = new HashMap<>();
            for (PM pm : pmList) {
//                PM pm = pmService.findByPmId(pmPmVerElem.getKey());

                // 放送日時,verリスト
//                Map<LocalDateTime, List<PMVer>> onAirDateMap = new HashMap<>();
//                for (PMVer pmVer : pmPmVerElem.getValue()) {
//                    List<PMVer> tmpList;
//                    if (onAirDateMap.containsKey(pmVer.getOn_air_date())) {
//                        tmpList = onAirDateMap.get(pmVer.getOn_air_date());
//                    } else {
//                        tmpList = new ArrayList<>();
//                    }
//                    tmpList.add(pmVer);
//                    onAirDateMap.put(pmVer.getOn_air_date(), tmpList);
//                }

                // 放送日時の数だけそれぞれ文章を作る
//                for (Map.Entry<LocalDateTime, List<PMVer>> elem : onAirDateMap.entrySet()) {
                    String text = twTextController.tvAlert(pm);

                    List<Long> teamIdList = StringUtilsMine.stringToLongList(pm.getTeamArr());
                    // generalブログのteamIdを詰めていくリスト
                    List<Long> generalBlogTeamIdList = new ArrayList<>();
                    for (Long teamId : teamIdList) {
                        if (TeamEnum.get(teamId).getBlogEnumId().equals(BlogEnum.MAIN.getId())) {
                            generalBlogTeamIdList.add(teamId);
                        } else {
                            postMap.put(teamId, text);
                        }
                    }

                    if (generalBlogTeamIdList.size() > 0) {
                        postMap.put(generalBlogTeamIdList.get(0), text);
                    }
//                }
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