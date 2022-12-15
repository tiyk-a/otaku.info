package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.RoomController;

@Component
@StepScope
public class RoomLikeCountTasklet implements Tasklet {

    @Autowired
    RoomController roomController;

    /**
     * 楽天ROOMの私のItemへのいいね数を1商品ずつカウントする
     * 前日のカウントと変動があれば差分を記録する
     *
     * @param contribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        roomController.execRoomLikeCount();
        return RepeatStatus.FINISHED;
    }
}
