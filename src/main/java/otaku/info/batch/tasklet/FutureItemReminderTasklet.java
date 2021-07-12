package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.SampleController;
import otaku.info.controller.TextController;
import otaku.info.dto.TwiDto;
import otaku.info.entity.Item;
import otaku.info.searvice.ItemService;

import java.util.List;

@Component
@StepScope
public class FutureItemReminderTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    TextController textController;

    @Autowired
    ItemService itemService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- TRANSACTION STARTこっち！ ---");
        List<Item> itemList = itemService.findFutureItemList();
        for (Item item : itemList) {
            TwiDto twiDto = new TwiDto(item.getTitle(), item.getUrl(), item.getPublication_date(), null);
            sampleController.post(item.getTeam_id(), textController.futureItemReminder(twiDto));
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
