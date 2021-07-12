package otaku.info.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import otaku.info.batch.tasklet.FutureItemReminderTasklet;
import otaku.info.batch.tasklet.RakutenSearchTasklet;

@Configuration
class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final RakutenSearchTasklet rakutenSearchTasklet;
    private final FutureItemReminderTasklet futureItemReminderTasklet;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                       RakutenSearchTasklet rakutenSearchTasklet, FutureItemReminderTasklet futureItemReminderTasklet) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.rakutenSearchTasklet = rakutenSearchTasklet;
        this.futureItemReminderTasklet = futureItemReminderTasklet;
    }

    @Bean
    Step step1() {
        return stepBuilderFactory.get("rakutenSearchStep") //Step名を指定
                .tasklet(rakutenSearchTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Step step2() {
        return stepBuilderFactory.get("step2").tasklet((contribution, chunkContext) -> {
            System.out.println("step2 has run");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    Job rakutenSearchJob() {
        return this.jobBuilderFactory.get("rakutenSearchJob").incrementer(new RunIdIncrementer())
            .start(step1()).next(step2()).build();
    }

    @Bean
    Step anotherStep() {
        return stepBuilderFactory.get("futureItemReminderStep") //Step名を指定
                .tasklet(futureItemReminderTasklet) //実行するTaskletを指定
                .build();
    }

    @Bean
    Job futureItemReminderJob() {
        return this.jobBuilderFactory.get("futureItemReminderJob").incrementer(new RunIdIncrementer())
            .start(anotherStep()).build();
    }
}
