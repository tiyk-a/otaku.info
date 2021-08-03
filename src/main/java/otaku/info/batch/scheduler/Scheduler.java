package otaku.info.batch.scheduler;

import java.util.*;

import org.slf4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.Job;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

    @Autowired
    @Qualifier("rakutenSearchMemberJob")
    private Job rakutenSearchMemberJob;

    @Autowired
    @Qualifier("tvJob")
    private Job tvJob;

    @Autowired
    @Qualifier("tvPostJob")
    private Job tvPostJob;

    @Autowired
    @Qualifier("dbNotifyJob")
    private Job dbNotifyJob;

    @Autowired
    @Qualifier("tvAlertJob")
    private Job tvAlertJob;

    Logger logger1 = org.slf4j.LoggerFactory.getLogger("otaku.info.batch1");

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

    @Scheduled(cron = "${cron.rakutenSearchMember}")
    public void run5(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run5", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(rakutenSearchMemberJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    @Scheduled(cron = "${cron.tvSearch}")
    public void run6(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run6", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    @Scheduled(cron = "${cron.tvPost}")
    public void run7(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run7", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvPostJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    @Scheduled(cron = "${cron.dbNotify}")
    public void run8(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run8", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(dbNotifyJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    @Scheduled(cron = "${cron.tvAlert}")
    public void run9(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run9", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvAlertJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    @ExceptionHandler(Throwable.class)
    public void exceptionHandler(Throwable t) {
        logger1.info(t.toString());
    }
}
