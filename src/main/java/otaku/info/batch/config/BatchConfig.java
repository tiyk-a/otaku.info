package otaku.info.batch.config;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import otaku.info.batch.tasklet.*;

/**
 * https://github.com/making/spring-boot-batch-multi-jobs
 */
@Configuration
@AllArgsConstructor
class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ItemSearchTasklet itemSearchTasklet;
    private final FutureItemReminderTasklet futureItemReminderTasklet;
    private final YahooItemSearchTasklet yahooItemSearchTasklet;
    private final PublishAnnounceTasklet publishAnnounceTasklet;
    private final ItemSearchMemberTasklet itemSearchMemberTasklet;
    private final TvPostTasklet tvPostTasklet;
    private final TvAlertTasklet tvAlertTasklet;
    private final UpdateUrlTasklet updateUrlTasklet;
    private final BlogUpdateTasklet blogUpdateTasklet;
    private final BlogCatchupTasklet blogCatchupTasklet;
    private final TvTasklet tvTasklet;
    private final TwFavTasklet twFavTasklet;
    private final TwFolBTasklet twFolBTasklet;
//    private final CalendarCatchupTasklet calendarCatchupTasklet;

    @Bean
    Step itemSearchStep() {
        return stepBuilderFactory.get("itemSearchStep")
                .tasklet(itemSearchTasklet)
                .build();
    }

    @Bean
    Job itemSearchJob() {
        return this.jobBuilderFactory.get("itemSearchJob").incrementer(new RunIdIncrementer())
            .start(itemSearchStep()).build();
    }

    @Bean
    Step futureItemReminderStep() {
        return stepBuilderFactory.get("futureItemReminderStep")
                .tasklet(futureItemReminderTasklet)
                .build();
    }

    @Bean
    Job futureItemReminderJob() {
        return this.jobBuilderFactory.get("futureItemReminderJob").incrementer(new RunIdIncrementer())
            .start(futureItemReminderStep()).build();
    }

    @Bean
    Step yahooItemSearchStep() {
        return stepBuilderFactory.get("yahooItemSearchStep")
                .tasklet(yahooItemSearchTasklet)
                .build();
    }

    @Bean
    Job yahooItemSearchJob() {
        return this.jobBuilderFactory.get("yahooItemSearchJob").incrementer(new RunIdIncrementer())
                .start(yahooItemSearchStep()).build();
    }

    @Bean
    Step publishAnnouncementStep() {
        return stepBuilderFactory.get("publishAnnouncementStep")
                .tasklet(publishAnnounceTasklet)
                .build();
    }

    @Bean
    Job publishAnnouncementJob() {
        return this.jobBuilderFactory.get("publishAnnouncementJob").incrementer(new RunIdIncrementer())
                .start(publishAnnouncementStep()).build();
    }

    /**
     * 個人名で楽天商品を検索します
     *
     * @return
     */
    @Bean
    Step itemSearchMemberStep() {
        return stepBuilderFactory.get("itemSearchMemberStep")
                .tasklet(itemSearchMemberTasklet)
                .build();
    }

    @Bean
    Job itemSearchMemberJob() {
        return this.jobBuilderFactory.get("itemSearchMemberJob").incrementer(new RunIdIncrementer())
                .start(itemSearchMemberStep()).build();
    }

    /**
     * TV番組の検索を投げます
     *
     * @return
     */
    @Bean
    Step tvStep() {
        return stepBuilderFactory.get("tvStep")
                .tasklet(tvTasklet)
                .build();
    }

    @Bean
    Job tvJob() {
        return this.jobBuilderFactory.get("tvJob").incrementer(new RunIdIncrementer())
                .start(tvStep()).build();
    }

    /**
     * 取得済みTV番組の情報をグループごとに投稿します。
     *
     * @return
     */
    @Bean
    Step tvPostStep() {
        return stepBuilderFactory.get("tvPostStep")
                .tasklet(tvPostTasklet)
                .build();
    }

    @Bean
    Job tvPostJob() {
        return this.jobBuilderFactory.get("tvPostJob").incrementer(new RunIdIncrementer())
                .start(tvPostStep()).build();
    }

    /**
     * 今から指定時間内に始まるTV番組を通知します。
     *
     * @return
     */
    @Bean
    Step tvAlertStep() {
        return stepBuilderFactory.get("tvAlertStep")
                .tasklet(tvAlertTasklet)
                .build();
    }

    @Bean
    Job tvAlertJob() {
        return this.jobBuilderFactory.get("tvAlertJob").incrementer(new RunIdIncrementer())
                .start(tvAlertStep()).build();
    }

    @Bean
    Step updateUrlStep() {
        return stepBuilderFactory.get("updateUrlStep")
                .tasklet(updateUrlTasklet)
                .build();
    }

    @Bean
    Job updateUrlJob() {
        return this.jobBuilderFactory.get("updateUrlJob").incrementer(new RunIdIncrementer())
                .start(updateUrlStep()).build();
    }

    @Bean
    Step blogUpdateStep() {
        return stepBuilderFactory.get("blogUpdateStep")
                .tasklet(blogUpdateTasklet)
                .build();
    }

    @Bean
    Job blogUpdateJob() {
        return this.jobBuilderFactory.get("blogUpdateJob").incrementer(new RunIdIncrementer())
                .start(blogUpdateStep()).build();
    }

    @Bean
    Step blogCatchupStep() {
        return stepBuilderFactory.get("blogCatchupStep")
                .tasklet(blogCatchupTasklet)
                .build();
    }

//    @Bean
//    Step calendarCatchupStep() {
//        return stepBuilderFactory.get("calendarCatchupStep")
//                .tasklet(calendarCatchupTasklet)
//                .build();
//    }

//    @Bean
//    Job blogCatchupJob() {
//        return this.jobBuilderFactory.get("blogCatchupJob").incrementer(new RunIdIncrementer())
//                .start(blogCatchupStep())
//                .next(calendarCatchupStep())
//                .build();
//    }

    @Bean
    Step twFavStep() {
        return stepBuilderFactory.get("twFavStep")
                .tasklet(twFavTasklet)
                .build();
    }

    @Bean
    Job twFavJob() {
        return this.jobBuilderFactory.get("twFavJob").incrementer(new RunIdIncrementer())
                .start(twFavStep()).build();
    }

    @Bean
    Step twFolBStep() {
        return stepBuilderFactory.get("twFolBStep")
                .tasklet(twFolBTasklet)
                .build();
    }

    @Bean
    Job twFolBJob() {
        return this.jobBuilderFactory.get("twFolBJob").incrementer(new RunIdIncrementer())
                .start(twFolBStep()).build();
    }
}
