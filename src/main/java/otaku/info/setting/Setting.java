package otaku.info.setting;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "setting")
public class Setting {

    private String test;
    private String imageitem;

    public String getBlogUpdate() {
        return test;
    }

    public String getImageitem() {
        return imageitem;
    }
}
