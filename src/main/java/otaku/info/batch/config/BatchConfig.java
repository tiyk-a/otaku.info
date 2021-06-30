package otaku.info.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import otaku.info.batch.tasklet.TwitterTasklet;

@EnableBatchProcessing
@Configuration
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final TwitterTasklet twitterTasklet;

    public BatchConfig(JobBuilderFactory jobBuilderFactory,
                       StepBuilderFactory stepBuilderFactory,
                       TwitterTasklet twitterTasklet) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.twitterTasklet = twitterTasklet;
    }

    @Bean
    public Job twitterJob(Step twitterStep) {
        return jobBuilderFactory.get("twitterJob") //Job名を指定
                .flow(twitterStep) //実行するStepを指定
                .end()
                .build();
    }

    @Bean
    public Step twitterStep() {
        return stepBuilderFactory.get("twitterStep") //Step名を指定
                .tasklet(twitterTasklet) //実行するTaskletを指定
                .build();
    }
}
