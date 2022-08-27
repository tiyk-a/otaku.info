package otaku.info.batch.tasklet;

import java.util.Arrays;
import java.util.List;
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
import otaku.info.enums.TeamEnum;

import java.util.HashMap;

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
        List<TeamEnum> teamList = Arrays.asList(TeamEnum.values());
        Map<Long, String> artistMap = new HashMap<Long, String>();
        teamList.forEach(t -> artistMap.put(t.getId(), t.getName()));
        for (Map.Entry<Long, String> artist : artistMap.entrySet()) {
            loggerController.printItemSearchTasklet("***** START: " + artist.getValue() + "*****");
            sampleController.searchItem(artist.getKey(), artist.getValue(), 0L, 1L);
            loggerController.printItemSearchTasklet("***** END: " + artist + "*****");
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                loggerController.printItemSearchTasklet("チーム検索エラー");
                e.printStackTrace();
            }
        }
        return RepeatStatus.FINISHED;
    }
}
