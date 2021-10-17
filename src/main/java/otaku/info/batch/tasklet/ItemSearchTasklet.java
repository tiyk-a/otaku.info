package otaku.info.batch.tasklet;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.SampleController;
import otaku.info.entity.Team;
import otaku.info.searvice.TeamService;
import otaku.info.setting.Log4jUtils;

import java.util.HashMap;
import java.util.List;

@Component
@StepScope
public class ItemSearchTasklet implements Tasklet {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger();

    @Autowired
    SampleController sampleController;

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
            logger.debug("***** START: " + artist.getValue() + "*****");
            sampleController.searchItem(artist.getKey(), artist.getValue(), 0L, 1L);
            logger.debug("***** END: " + artist + "*****");
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        return RepeatStatus.FINISHED;
    }
}
