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
    private String sixtonesApiPw;
    private String snowmanApiPw;
    private String naniwaApiPw;
    private String kinpriApiPw;
    private String tvKingdom;
    private String pyTwi2;
    private String lineUrl;
    private String rakutenApiUrl;
    private String rakutenAffiliId;
    private String rakutenApiDefParam;
    private String apiPw;
//    private String blogCardPre;
//    private String blogCardPos;
    private String yahooShoppingApi;
    private String yahooShoppingApiPos;
    private String yahooPhraseApi;
    private String amazonCardHead;
    private String amazonCardPos;
    private String calendarCredential;
    private String generatedImage;
    private String youtuberedential;
    private String imageItem;
}
