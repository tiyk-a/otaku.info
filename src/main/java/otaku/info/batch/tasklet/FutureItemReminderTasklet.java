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
import otaku.info.dto.TwiDto;
import otaku.info.entity.Item;
import otaku.info.searvice.ItemService;

import java.util.List;

@Component
@StepScope
public class FutureItemReminderTasklet implements Tasklet {

    @Autowired
    PythonController pythonController;

    @Autowired
    TextController textController;

    @Autowired
    ItemService itemService;

    Logger logger2 = org.slf4j.LoggerFactory.getLogger("otaku.info.batch2");

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger2.info("--- 未発売商品リマインダー START ---");
        // 1年以内に発売される商品リストを取得
        List<Item> itemList = itemService.findFutureItemByDate(365);
        for (Item item : itemList) {
            TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), item.getPublication_date(), null, (long) item.getTeam_id());
            pythonController.post(item.getTeam_id(), textController.futureItemReminder(twiDto));
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        logger2.info("--- 未発売商品リマインダー END ---");
        return RepeatStatus.FINISHED;
    }
}
