package otaku.info.batch.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.BlogController;
import otaku.info.enums.TeamEnum;
import otaku.info.setting.Log4jUtils;

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

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("BlogUpdateTasklet");

    @Autowired
    BlogController blogController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.debug("①固定商品ページ");
        logger.debug(System.getProperty("user.name"));
        // もし月末が近かったら来月のWpタグ(yyyyMM)があるか確認し、なかったら追加する。
        List<String> domainList = TeamEnum.getAllSubDomain();
        for (String subDomain : domainList) {
            blogController.addNextMonthTag(subDomain);
        }
        // 近日発売新商品情報を更新
        try {
            blogController.updateReleaseItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("①固定商品ページ完了");
        logger.debug("②固定TV出演情報ページ");
        logger.debug(System.getProperty("user.name"));
        try {
            blogController.updateTvPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("②固定TV出演情報ページ完了");
        return RepeatStatus.FINISHED;
    }
}
