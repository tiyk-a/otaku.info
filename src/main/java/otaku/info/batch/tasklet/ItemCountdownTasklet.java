package otaku.info.batch.tasklet;

import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.PythonController;
import otaku.info.controller.TextController;
import otaku.info.entity.Item;
import otaku.info.searvice.ItemService;

import java.util.List;

@Component
@StepScope
public class ItemCountdownTasklet implements Tasklet {

    @Autowired
    PythonController pythonController;

    @Autowired
    TextController textController;

    @Autowired
    ItemService itemService;

    Logger logger3 = org.slf4j.LoggerFactory.getLogger("otaku.info.batch3");

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger3.info("--- 新商品カウントダウン START ---");
        // 3日以内に発売する商品リストを取得
        List<Item> itemList = itemService.findFutureItemByDate(3);
        for (Item item : itemList) {
            String text = textController.countdown(item);
            pythonController.post(item.getTeam_id(), text);
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        logger3.info("--- 新商品カウントダウン END ---");
        return RepeatStatus.FINISHED;
    }
}
