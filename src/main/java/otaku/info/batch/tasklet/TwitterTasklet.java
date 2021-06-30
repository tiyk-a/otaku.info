package otaku.info.batch.tasklet;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.SampleController;
import otaku.info.entity.Team;
import otaku.info.searvice.db.TeamService;

import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
//@AllArgsConstructor
public class TwitterTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    TeamService teamService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- TRANSACTION START ---");
        List<Team> teamList = teamService.findAllTeam();
        List<String> artistList = new ArrayList<>();
        teamList.forEach(t -> artistList.add(t.getName()));
        for (String artist : artistList) {
            System.out.println("***** START: " + artist + "*****");
            sampleController.sample2(artist);
            System.out.println("***** END: " + artist + "*****");
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("--- TRANSACTION END ---");
        return RepeatStatus.FINISHED;
    }
}
