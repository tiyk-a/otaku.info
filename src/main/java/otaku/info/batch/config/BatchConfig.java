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

    private final RakutenSearchTasklet rakutenSearchTasklet;
    private final FutureItemReminderTasklet futureItemReminderTasklet;
    private final PublishAnnounceTasklet publishAnnounceTasklet;
    private final RakutenSearchMemberTasklet rakutenSearchMemberTasklet;
    private final TvPostTasklet tvPostTasklet;
    private final TvAlertTasklet tvAlertTasklet;
    private final UpdateUrlTasklet updateUrlTasklet;
    private final BlogUpdateTasklet blogUpdateTasklet;
    private final BlogMediaTasklet blogMediaTasklet;

    private final TvTasklet tvTasklet;

    @Bean
    Step rakutenSearchStep() {
        return stepBuilderFactory.get("rakutenSearchStep") //Step名を指定
                .tasklet(rakutenSearchTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job rakutenSearchJob() {
        return this.jobBuilderFactory.get("rakutenSearchJob").incrementer(new RunIdIncrementer())
            .start(rakutenSearchStep()).build();
    }

    @Bean
    Step futureItemReminderStep() {
        return stepBuilderFactory.get("futureItemReminderStep") //Step名を指定
                .tasklet(futureItemReminderTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job futureItemReminderJob() {
        return this.jobBuilderFactory.get("futureItemReminderJob").incrementer(new RunIdIncrementer())
            .start(futureItemReminderStep()).build();
    }

    @Bean
    Step publishAnnouncementStep() {
        return stepBuilderFactory.get("publishAnnouncementStep") //Step名を指定
                .tasklet(publishAnnounceTasklet) //実行するTaskletを指定
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
    Step rakutenSearchMemberStep() {
        return stepBuilderFactory.get("rakutenSearchMemberStep") //Step名を指定
                .tasklet(rakutenSearchMemberTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job rakutenSearchMemberJob() {
        return this.jobBuilderFactory.get("rakutenSearchMemberJob").incrementer(new RunIdIncrementer())
                .start(rakutenSearchMemberStep()).build();
    }

    /**
     * TV番組の検索を投げます
     *
     * @return
     */
    @Bean
    Step tvStep() {
        return stepBuilderFactory.get("tvStep") //Step名を指定
                .tasklet(tvTasklet) //実行するTaskletを指定
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
        return stepBuilderFactory.get("tvPostStep") //Step名を指定
                .tasklet(tvPostTasklet) //実行するTaskletを指定
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
        return stepBuilderFactory.get("tvAlertStep") //Step名を指定
                .tasklet(tvAlertTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job tvAlertJob() {
        return this.jobBuilderFactory.get("tvAlertJob").incrementer(new RunIdIncrementer())
                .start(tvAlertStep()).build();
    }

    @Bean
    Step updateUrlStep() {
        return stepBuilderFactory.get("updateUrlStep") //Step名を指定
                .tasklet(updateUrlTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job updateUrlJob() {
        return this.jobBuilderFactory.get("updateUrlJob").incrementer(new RunIdIncrementer())
                .start(updateUrlStep()).build();
    }

    @Bean
    Step blogUpdateStep() {
        return stepBuilderFactory.get("blogUpdateStep") //Step名を指定
                .tasklet(blogUpdateTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job blogUpdateJob() {
        return this.jobBuilderFactory.get("blogUpdateJob").incrementer(new RunIdIncrementer())
                .start(blogUpdateStep()).build();
    }

    @Bean
    Step blogMediaStep() {
        return stepBuilderFactory.get("blogMediaStep") //Step名を指定
                .tasklet(blogMediaTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job blogMediaJob() {
        return this.jobBuilderFactory.get("blogMediaJob").incrementer(new RunIdIncrementer())
                .start(blogMediaStep()).build();
    }
}
