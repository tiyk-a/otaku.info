package otaku.info.batch.tasklet;

import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.LoggerController;
import otaku.info.controller.SampleController;
import otaku.info.entity.Team;
import otaku.info.service.TeamService;

import java.util.HashMap;
import java.util.List;

/**
 * チーム名で商品検索を行う
 *
 */
@Component
@StepScope
public class ItemSearchTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    TeamService teamService;

    /**
     * 楽天新商品を検索します。
     *
     * @param contribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<Team> teamList = teamService.findAllTeam();
        Map<Long, String> artistMap = new HashMap<Long, String>();
        teamList.forEach(t -> artistMap.put(t.getTeam_id(), t.getTeam_name()));
        for (Map.Entry<Long, String> artist : artistMap.entrySet()) {
            loggerController.printItemSearchTasklet("***** START: " + artist.getValue() + "*****");
            sampleController.searchItem(artist.getKey(), artist.getValue(), 0L, 1L);
            loggerController.printItemSearchTasklet("***** END: " + artist + "*****");
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return RepeatStatus.FINISHED;
    }
}
