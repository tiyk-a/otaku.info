package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.LineController;
import otaku.info.entity.Item;
import otaku.info.entity.Program;
import otaku.info.searvice.ItemService;
import otaku.info.searvice.ProgramService;

import java.util.ArrayList;
import java.util.List;


/**
 * DBに新商品でfct_chk未完了のデータがあればLINEへ通知します。
 *
 */
@Component
@StepScope
public class DbNotifyTasklet implements Tasklet {

    @Autowired
    ItemService itemService;

    @Autowired
    ProgramService programService;

    @Autowired
    LineController lineController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- DB Insert Notify START ---");
        List<String> lineList = new ArrayList<>();

        if (itemService.waitingFctChk()) {
            List<Item> itemList = itemService.findByFctChk(false);
            if (itemList.size() > 0) {
                lineList.add("【fct_chk待ち商品】" + itemList.size() + "件あります");
            }
        }

        if (programService.waitingFctChk()) {
            List<Program> programList = programService.findByFctChk(0);
            if (programList.size() > 0) {
                lineList.add("【fct_chk待ちTV】" + programList.size() + "件あります");
            }
        }

        if (lineList.size() > 0) {
            lineController.postAll(lineList);
        }
        System.out.println("--- DB Insert Notify END ---");
        return RepeatStatus.FINISHED;
    }
}
