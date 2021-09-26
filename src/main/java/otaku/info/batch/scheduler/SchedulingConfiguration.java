package otaku.info.batch.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Handle EnableScheduling depend on the work environment
 * https://github.com/spring-projects/spring-boot/issues/12682
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "setting", name="enableScheduling", havingValue="true", matchIfMissing = true)
public class SchedulingConfiguration {

    /**
     * 複数バッチの同時実行（時間被り）を行えるようにする
     * https://atl2.net/springboot/scheduled%E3%81%A7%E5%90%8C%E6%99%82%E5%AE%9F%E8%A1%8C%E3%81%99%E3%82%8B/
     *
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        return taskScheduler;
    }
}
