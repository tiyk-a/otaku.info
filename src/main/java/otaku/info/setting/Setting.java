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
    private String imageItem;
    private String blogApiUrl;
    private String blogWebUrl;
    private String tvKingdom;
    private String pythonTwitter;
    private String lineUrl;
    private String rakutenApiUrl;
    private String rakutenAffiliId;
    private String rakutenApiDefParam;
    private String blogPw;

}
