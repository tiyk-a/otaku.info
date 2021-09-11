package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.BlogController;

@Component
@StepScope
public class BlogUpdateTasklet implements Tasklet {

    @Autowired
    BlogController blogController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- Blog Update START ---");
        // もし月末が近かったら来月のWpタグ(yyyyMM)があるか確認し、なかったら追加する。
        blogController.addNextMonthTag();
        // 近日発売新商品情報を更新
        blogController.updateReleaseItems();
        System.out.println("--- Blog Update END ---");
        return RepeatStatus.FINISHED;
    }
}
