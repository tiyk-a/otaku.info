package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.BlogController;
import otaku.info.controller.LoggerController;
import otaku.info.enums.BlogEnum;

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

    @Autowired
    LoggerController loggerController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        loggerController.printBlogUpdateTasklet("①固定商品ページ");
//        logger.debug(System.getProperty("user.name"));
//        // もし月末が近かったら来月のWpタグ(yyyyMM)があるか確認し、なかったら追加する。
        BlogEnum[] blogEnumArr = BlogEnum.values();
        for (BlogEnum blogEnum : blogEnumArr) {
            blogController.addNextMonthTag(blogEnum);
        }
        // 近日発売新商品情報を更新
        try {
            blogController.updateReleaseItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loggerController.printBlogUpdateTasklet("①固定商品ページ完了");
        loggerController.printBlogUpdateTasklet("②固定TV出演情報ページ");
        loggerController.printBlogUpdateTasklet(System.getProperty("user.name"));
        try {
            blogController.updateTvPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loggerController.printBlogUpdateTasklet("②固定TV出演情報ページ完了");
        loggerController.printBlogUpdateTasklet("③明日の1日の予定投稿");
        try {
            for (BlogEnum blogEnum : blogEnumArr) {
                blogController.createDailySchedulePost(blogEnum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loggerController.printBlogUpdateTasklet("③明日の1日の予定投稿完了");
        return RepeatStatus.FINISHED;
    }
}
