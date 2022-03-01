package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.LoggerController;
import otaku.info.controller.SampleController;
import otaku.info.dto.MemberSearchDto;
import otaku.info.entity.Team;
import otaku.info.service.MemberService;
import otaku.info.service.TeamService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@StepScope
public class YahooItemSearchTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    TeamService teamService;

    @Autowired
    MemberService memberService;

    /**
     * Yahoo新商品を検索します。
     *
     * @param contribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            List<Team> teamList = teamService.findAllTeam();
            Map<Long, String> artistMap = new HashMap<Long, String>();
            teamList.forEach(t -> artistMap.put(t.getTeam_id(), t.getTeam_name()));
            for (Map.Entry<Long, String> artist : artistMap.entrySet()) {
                loggerController.printYahooItemSearchTasklet("***** START: " + artist.getValue() + "*****");
                sampleController.searchItem(artist.getKey(), artist.getValue(), 0L, 2L);
                loggerController.printYahooItemSearchTasklet("***** END: " + artist + "*****");
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        loggerController.printYahooItemSearchTasklet("--- 新商品検索（個人） START ---");
        try {
            List<MemberSearchDto> dtoList = new ArrayList<>();
            memberService.findAllMember().forEach(e -> dtoList.add(e.convertToDto()));
            for (MemberSearchDto dto : dtoList) {
                loggerController.printYahooItemSearchTasklet("***** SEARCH: " + dto.getMember_name() + "*****");
                sampleController.searchItem(dto.getTeam_id(), dto.getMember_name(), dto.getMember_id(), 1L);
                loggerController.printYahooItemSearchTasklet("***** END: " + dto.getMember_name() + "*****");
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loggerController.printYahooItemSearchTasklet("--- 新商品検索（個人） END ---");
        return RepeatStatus.FINISHED;
    }
}
