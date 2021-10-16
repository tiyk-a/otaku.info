package otaku.info.batch.scheduler;

import java.util.*;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.Job;
import otaku.info.controller.LineController;

import java.util.HashMap;

@Component
public class Scheduler {

    @Autowired
    LineController lineController;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("itemSearchJob")
    private Job itemSearchJob;

    @Autowired
    @Qualifier("futureItemReminderJob")
    private Job futureItemReminderJob;

    @Autowired
    @Qualifier("yahooItemSearchJob")
    private Job yahooItemSearchJob;

    @Autowired
    @Qualifier("publishAnnouncementJob")
    private Job publishAnnouncementJob;

    @Autowired
    @Qualifier("itemSearchMemberJob")
    private Job itemSearchMemberJob;

    @Autowired
    @Qualifier("tvJob")
    private Job tvJob;

    @Autowired
    @Qualifier("tvPostJob")
    private Job tvPostJob;

    @Autowired
    @Qualifier("tvAlertJob")
    private Job tvAlertJob;

    @Autowired
    @Qualifier("updateUrlJob")
    private Job updateUrlJob;

    @Autowired
    @Qualifier("blogUpdateJob")
    private Job blogUpdateJob;

    @Autowired
    @Qualifier("blogMediaJob")
    private Job blogMediaJob;

    @Scheduled(cron = "${cron.itemSearch}")
    public void run1(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<String, JobParameter>();
        confMap.put("run1", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(itemSearchJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run1: " + diff);
    }

    @Scheduled(cron = "${cron.futureItemReminder}")
    public void run2(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run2", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(futureItemReminderJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run2: " + diff);
    }

    @Scheduled(cron = "${cron.yahooItemSearch}")
    public void run3(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run3", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(yahooItemSearchJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run3: " + diff);
    }

    @Scheduled(cron = "${cron.publishAnnounce}")
    public void run4(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run4", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(publishAnnouncementJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run4: " + diff);
    }

    @Scheduled(cron = "${cron.itemSearchMember}")
    public void run5(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run5", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(itemSearchMemberJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run5: " + diff);
    }

    @Scheduled(cron = "${cron.tvSearch}")
    public void run6(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run6", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run6: " + diff);
    }

    @Scheduled(cron = "${cron.tvPost}")
    public void run7(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run7", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvPostJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run7: " + diff);
    }

    @Scheduled(cron = "${cron.tvAlert}")
    public void run9(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run9", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvAlertJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run9: " + diff);
    }

    @Scheduled(cron = "${cron.updateUrl}")
    public void run10(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run10", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(updateUrlJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run10: " + diff);
    }

    // 固定ページの更新
    @Scheduled(cron = "${cron.blogUpdate}")
    public void run11(){
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run11", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(blogUpdateJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        System.out.println("run11: " + diff);
    }

    @Scheduled(cron = "${cron.blogMedia}")
    public void run12(){
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run12", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(blogMediaJob, jobParameters);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            lineController.post(System.currentTimeMillis() + ": " + ex.getMessage());
        }
    }
}
