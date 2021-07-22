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
    private final ItemCountdownTasklet itemCountdownTasklet;
    private final PublishAnnounceTasklet publishAnnounceTasklet;
    private final RakutenSearchMemberTasklet rakutenSearchMemberTasklet;

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
    Step itemCountdownStep() {
        return stepBuilderFactory.get("futureItemReminderStep") //Step名を指定
                .tasklet(itemCountdownTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job itemCountdownJob() {
        return this.jobBuilderFactory.get("itemCountdownJob").incrementer(new RunIdIncrementer())
                .start(itemCountdownStep()).build();
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
}
