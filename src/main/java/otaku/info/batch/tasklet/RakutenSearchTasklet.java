package otaku.info.batch.tasklet;

import java.util.Map;

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

import java.util.HashMap;
import java.util.List;

@Component
@StepScope
public class RakutenSearchTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    TeamService teamService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- 楽天新商品検索 START ---");
        List<Team> teamList = teamService.findAllTeam();
        Map<Long, String> artistMap = new HashMap<Long, String>();
        teamList.forEach(t -> artistMap.put(t.getTeam_id(), t.getTeam_name()));
        for (Map.Entry<Long, String> artist : artistMap.entrySet()) {
            System.out.println("***** START: " + artist.getValue() + "*****");
            sampleController.searchItem(artist.getKey(), artist.getValue(), 0L);
            System.out.println("***** END: " + artist + "*****");
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("--- 楽天新商品検索 END ---");
        return RepeatStatus.FINISHED;
    }
}
