package otaku.info;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextListener;

@EnableBatchProcessing
@SpringBootApplication
@EnableAutoConfiguration
public class InfoApplication {

	public static void main(String[] args) {
		SpringApplication.run(InfoApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
		return restTemplateBuilder.build();
	}

	@Bean
	public RequestContextListener requestContextListener() {
		return new RequestContextListener();
	}
}
