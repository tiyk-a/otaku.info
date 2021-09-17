package otaku.info.batch.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Handle EnableScheduling depend on the work environment
 * https://github.com/spring-projects/spring-boot/issues/12682
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "setting", name="enableScheduling", havingValue="true", matchIfMissing = true)
public class SchedulingConfiguration {
}
