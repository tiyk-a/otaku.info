package otaku.info.batch.scheduler;

import java.util.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.Job;
import java.util.HashMap;

@Component
public class Scheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("rakutenSearchJob")
    private Job rakutenSearchJob;

    @Autowired
    @Qualifier("futureItemReminderJob")
    private Job futureItemReminderJob;

    @Autowired
    @Qualifier("itemCountdownJob")
    private Job itemCountdownJob;

    @Autowired
    @Qualifier("publishAnnouncementJob")
    private Job publishAnnouncementJob;

    @Scheduled(cron = "${cron.rakutenSearch}")
    public void run1(){
        Map<String, JobParameter> confMap = new HashMap<String, JobParameter>();
        confMap.put("run1", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(rakutenSearchJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

    }

    @Scheduled(cron = "${cron.futureItemReminder}")
    public void run2(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run2", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(futureItemReminderJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    @Scheduled(cron = "${cron.itemCountdown}")
    public void run3(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run3", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(itemCountdownJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    @Scheduled(cron = "${cron.publishAnnounce}")
    public void run4(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run4", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(publishAnnouncementJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}
