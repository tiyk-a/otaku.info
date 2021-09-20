package otaku.info.batch.tasklet;

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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@StepScope
public class BlogMediaTasklet implements Tasklet {

    @Autowired
    ItemMasterService itemMasterService;

    @Autowired
    BlogController blogController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        System.out.println("--- Blog画像設定 START ---");

        // eye catch mediaの設定がないwpIdのリストを取得する
        List<Integer> wpIdList = blogController.findNoEyeCatchPosts();
        System.out.println("wpIdList:" + wpIdList.size());

        // wpIdからitemMasterを取得。2021年以降発売の商品だけ、画像設定対象にする
        Date date = java.sql.Date.valueOf("2020-12-31");

        if (wpIdList.size() > 0) {
            List<ItemMaster> itemMasterList = itemMasterService.findByWpIdList(wpIdList);
            System.out.println("itemMasterList:" + itemMasterList.size());
            List<ItemMaster> tmpList = itemMasterList.stream().filter(e -> e.getPublication_date().after(date)).collect(Collectors.toList());
            itemMasterList.forEach(e -> System.out.println(e.getItem_m_id() + " " + e.getPublication_date()));
            System.out.println("date: " + date + " tmpList:" + tmpList.size());

            // 対象商品マスタが存在すれば処理実行
            if (itemMasterList.size() > 0) {
                blogController.loadMedia(tmpList, false);
            }
        }

        System.out.println("--- Blog画像設定 END ---");
        return RepeatStatus.FINISHED;
    }
}
