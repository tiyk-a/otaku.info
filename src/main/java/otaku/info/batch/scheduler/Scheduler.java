package otaku.info.batch.scheduler;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Scheduler {
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;

    @Scheduled(cron = "${cron.cron1}")
    public void reportCurrentTime() throws Exception{
        JobParameters param = new JobParametersBuilder().addString("twitterJob",
                String.valueOf(System.currentTimeMillis())).toJobParameters();
        JobExecution execution =  jobLauncher.run(job, param);
        System.out.println("Job Execution Status: " + execution.getStatus());
    }
}
