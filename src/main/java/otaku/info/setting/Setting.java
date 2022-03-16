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
    private String blogWebUrl;
    private String blogApiPath;
    private String blogHttps;
    private String blogDomain;
    private String sixtonesBlogPw;
    private String snowmanBlogPw;
    private String naniwaBlogPw;
    private String kinpriBlogPw;
    private String tvKingdom;
    private String pythonTwitter;
    private String lineUrl;
    private String rakutenApiUrl;
    private String rakutenAffiliId;
    private String rakutenApiDefParam;
    private String blogPw;
//    private String blogCardPre;
//    private String blogCardPos;
    private String yahooShoppingApi;
    private String yahooShoppingApiPos;
    private String yahooPhraseApi;
    private String amazonCardHead;
    private String amazonCardPos;
    private String calendarCredential;
    private String generatedImage;
}
