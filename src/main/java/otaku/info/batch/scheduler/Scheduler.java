package otaku.info.batch.scheduler;

import java.util.*;

import org.apache.log4j.Logger;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.Job;
import otaku.info.controller.LineController;
import otaku.info.setting.Log4jUtils;

import java.util.HashMap;

@Component
public class Scheduler {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("Scheduler");

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
    @Qualifier("blogPostJob")
    private Job blogPostJob;

//    @Autowired
//    @Qualifier("blogCatchupJob")
//    private Job blogCatchupJob;

    @Autowired
    @Qualifier("roomLikeCountJob")
    private Job roomLikeCountJob;

    /**
     * Twitterファボ
     */
    @Autowired
    @Qualifier("twFavJob")
    private Job twFavJob;

    /**
     * Twitterフォロバ
     */
    @Autowired
    @Qualifier("twFolBJob")
    private Job twFolBJob;

    @Scheduled(cron = "${cron.itemSearch}")
    public void run1() {
        logger.debug("--- run1: 楽天新商品検索 START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<String, JobParameter>();
        confMap.put("run1", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(itemSearchJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run1: 楽天新商品検索 " + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run1: " + diff);
        logger.debug("--- run1: 楽天新商品検索 END ---");
    }

    @Scheduled(cron = "${cron.futureItemReminder}")
    public void run2() {
        logger.debug("--- run2: 未発売商品リマインダー START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run2", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(futureItemReminderJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run2: 未発売商品リマインダー" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run2: " + diff);
        logger.debug("--- run2: 未発売商品リマインダー END ---");
    }

    @Scheduled(cron = "${cron.yahooItemSearch}")
    public void run3() {
        logger.debug("--- run3: Yahoo新商品検索 START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run3", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(yahooItemSearchJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run3: Yahoo新商品検索" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run3: " + diff);
        logger.debug("--- run3: Yahoo新商品検索 END ---");
    }

    @Scheduled(cron = "${cron.publishAnnounce}")
    public void run4() {
        logger.debug("--- run4: 商品発売日アナウンス START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run4", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(publishAnnouncementJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run4: 商品発売日アナウンス" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run4: " + diff);
        logger.debug("--- run4: 商品発売日アナウンス END ---");
    }

    @Scheduled(cron = "${cron.itemSearchMember}")
    public void run5() {
        logger.debug("--- run5: 新商品検索（個人） START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run5", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(itemSearchMemberJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run5: 新商品検索（個人）" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run5: " + diff);
        logger.debug("--- run5: 新商品検索（個人） END ---");
    }

    @Scheduled(cron = "${cron.tvSearch}")
    public void run6() {
        logger.debug("--- run6: TV検索 START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run6", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run6: TV検索" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run6: " + diff);
        logger.debug("--- run6: TV検索 END ---");
    }

    @Scheduled(cron = "${cron.tvPost}")
    public void run7() {
        logger.debug("--- run7: TV番組投稿処理 START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run7", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvPostJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run7: TV番組投稿処理" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run7: " + diff);
        logger.debug("--- run7: TV番組投稿処理 END ---");
    }

//    @Scheduled(cron = "${cron.blogCatchup}")
//    public void run8() {
//        logger.debug("--- run8: IMブログ投稿キャッチアップ START ---");
//        Long startTime = System.currentTimeMillis();
//        Map<String, JobParameter> confMap = new HashMap<>();
//        confMap.put("run8", new JobParameter(System.currentTimeMillis()));
//        JobParameters jobParameters = new JobParameters(confMap);
//        try {
//            jobLauncher.run(blogCatchupJob, jobParameters);
//        }catch (Exception ex) {
//            logger.debug(ex.getMessage());
//            lineController.post("run8: IMブログ投稿キャッチアップ" + ex.getMessage());
//        }
//        Long endTime = System.currentTimeMillis();
//        Long diff = endTime - startTime;
//        logger.debug("run8: " + diff);
//        logger.debug("--- run8: IMブログ投稿キャッチアップ END ---");
//    }

    @Scheduled(cron = "${cron.tvAlert}")
    public void run9() {
        logger.debug("--- run9: TVアラート START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run9", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(tvAlertJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run9: TVアラート" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run9: " + diff);
        logger.debug("--- run9: TVアラート END ---");
    }

    @Scheduled(cron = "${cron.updateUrl}")
    public void run10() {
        logger.debug("--- run10: DB商品アフェリリンク更新 START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run10", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(updateUrlJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run10: DB商品アフェリリンク更新" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run10: " + diff);
        logger.debug("--- run10: DB商品アフェリリンク更新 END ---");
    }

    // 固定ページの更新
    @Scheduled(cron = "${cron.blogUpdate}")
    public void run11() {
        logger.debug("--- run11: Blog Update START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run11", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(blogUpdateJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run11: Blog Update" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run11: " + diff);
        logger.debug("--- run11: Blog Update END ---");
    }

    /**
     * Twitterファボ
     */
    @Scheduled(cron = "${cron.twFav}")
    public void run12() {
        logger.debug("--- run12: Twitter Fav START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run12", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(twFavJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run12: Twitter Fav" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run12: " + diff);
        logger.debug("--- run12: Twitter Fav END ---");
    }

    /**
     * Twitterフォロバ
     */
    @Scheduled(cron = "${cron.twFolB}")
    public void run13() {
        logger.debug("--- run13: Twitter Follow Back START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run13", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(twFolBJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run13: Twitter Follow Back" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run13: " + diff);
        logger.debug("--- run13: Twitter Follow Back END ---");
    }

    /**
     * ブログ投稿処理
     */
    @Scheduled(cron = "${cron.blogUpd}")
    public void run14() {
        logger.debug("--- run14: Twitter Blog Post START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run14", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(blogPostJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run14: Twitter Blog Post" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run14: " + diff);
        logger.debug("--- run14: Twitter Blog Post END ---");
    }

    /**
     * 楽天ROOM前日とのいいね差分カウント
     */
    @Scheduled(cron = "${cron.roomLikeCount}")
    public void run15() {
        logger.debug("--- run15: ROOM like count START ---");
        Long startTime = System.currentTimeMillis();
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("run15", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameters = new JobParameters(confMap);
        try {
            jobLauncher.run(roomLikeCountJob, jobParameters);
        }catch (Exception ex) {
            logger.debug(ex.getMessage());
            lineController.post("run15: ROOM like count" + ex.getMessage());
        }
        Long endTime = System.currentTimeMillis();
        Long diff = endTime - startTime;
        logger.debug("run15: " + diff);
        logger.debug("--- run15: ROOM like count END ---");
    }
}
