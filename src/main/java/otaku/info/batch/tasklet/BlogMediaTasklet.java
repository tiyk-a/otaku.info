package otaku.info.batch.tasklet;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.BlogController;
import otaku.info.entity.ItemMaster;
import otaku.info.searvice.ItemMasterService;
import java.util.*;

import java.util.List;

@Component
@StepScope
public class BlogMediaTasklet implements Tasklet {

    @Autowired
    ItemMasterService itemMasterService;

    @Autowired
    BlogController blogController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- Blog画像設定 START ---");
        // 対象商品(wp_idがあり昨日以降に最終更新がされておりimage1が存在する)を取得
        Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DATE, -100);

        List<ItemMaster> itemMasterList = itemMasterService.findWpIdNotNullImage1Exists();

        // 対象商品マスタが存在すれば処理実行
        if (itemMasterList.size() > 0) {
            blogController.loadMedia(itemMasterList);
        }
        System.out.println("--- Blog画像設定 END ---");
        return RepeatStatus.FINISHED;
    }
}
