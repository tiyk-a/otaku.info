package otaku.info.batch.tasklet;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import otaku.info.controller.SampleController;
import otaku.info.entity.Team;
import otaku.info.searvice.TeamService;

import java.util.HashMap;
import java.util.List;

@Component
@StepScope
public class RakutenSearchTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    TeamService teamService;

    Logger logger1 = org.slf4j.LoggerFactory.getLogger("otaku.info.batch1");

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger1.info("--- 楽天新商品検索 START ---");
        List<Team> teamList = teamService.findAllTeam();
        Map<Long, String> artistMap = new HashMap<Long, String>();
        teamList.forEach(t -> artistMap.put(t.getTeam_id(), t.getTeam_name()));
        for (Map.Entry<Long, String> artist : artistMap.entrySet()) {
            logger1.info("***** START: " + artist.getValue() + "*****");
            sampleController.sample2(artist.getKey(), artist.getValue());
            logger1.info("***** END: " + artist + "*****");
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
                logger1.info(e.getStackTrace().toString());
            }
        }
        logger1.info("--- 楽天新商品検索 END ---");
        return RepeatStatus.FINISHED;
    }

    @ExceptionHandler(Throwable.class)
    public void exceptionHandler(Throwable t) {
        logger1.info(t.toString());
    }
}
