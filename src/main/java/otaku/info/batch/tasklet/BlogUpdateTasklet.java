package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.BlogController;
import otaku.info.enums.TeamEnum;

import java.util.List;

/**
 * ブログ固定ページを更新します
 * ①商品ページ（トップ）
 * ②TVページ
 * 各ドメイン更新します
 *
 */
@Component
@StepScope
public class BlogUpdateTasklet implements Tasklet {

    @Autowired
    BlogController blogController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- Blog Update START ---");
        System.out.println("①固定商品ページ");
        System.out.println(System.getProperty("user.name"));
        // もし月末が近かったら来月のWpタグ(yyyyMM)があるか確認し、なかったら追加する。
        List<String> domainList = TeamEnum.getAllSubDomain();
        for (String subDomain : domainList) {
            blogController.addNextMonthTag(subDomain);
        }
        // 近日発売新商品情報を更新
        blogController.updateReleaseItems();
        System.out.println("①固定商品ページ完了");
        System.out.println("②固定TV出演情報ページ");
        System.out.println(System.getProperty("user.name"));
        blogController.updateTvPage();
        System.out.println("②固定TV出演情報ページ完了");
        System.out.println("--- Blog Update END ---");
        return RepeatStatus.FINISHED;
    }
}
