package otaku.info.batch.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import otaku.info.controller.LineController;
import otaku.info.enums.TeamEnum;
import otaku.info.setting.Log4jUtils;
import otaku.info.setting.Setting;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Twitter Favバッチ
 * 【注意】ジャニ以外のアカウントも乗っかってます
 * チームIDを順番にpyTwi2へ飛ばすだけの処理内容
 *
 */
@Component
@StepScope
public class TwFavTasklet implements Tasklet {
    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("TwFavTasklet");

    @Autowired
    Setting setting;

    @Autowired
    LineController lineController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        logger.debug("ジャニTwitter Fav START");
        lineController.post("Fab Job始まったよ！");
        List<Long> teamIdList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getId).collect(Collectors.toList());
        for (Long teamId : teamIdList) {

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            logger.info("teamId=" + teamId + "の検索＆ファボ中");
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPythonTwitter() + "twSearch?q=" + TeamEnum.get(teamId).getMnemonic() + "&teamId=" + teamId, String.class);
            logger.info("teamId=" + teamId + "の検索＆ファボ結果：" + Objects.requireNonNull(response.getBody()));
        }
        logger.debug("ジャニTwitter Fav END");

        logger.debug("ジャニ以外Twitter Fav START");
        // 100: @LjtYdg, 101: @ChiccaSalak, 102: @BlogChicca, 103: @Berry_chicca
        Map<Integer, String> idMap = new HashMap<>() {
            {
                put(100, "");
                put(101, "");
                put(102, "");
                put(103, "");
            }
        };

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        for (Map.Entry<Integer, String> entry : idMap.entrySet()) {
            logger.info("teamId=" + entry.getKey() + "のフォロバ中");
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPythonTwitter() + "twSearch?q=" + entry.getValue() + "&teamId=" + entry.getKey(), String.class);
            logger.info("teamId=" + entry.getKey() + "のフォロバ結果：" + Objects.requireNonNull(response.getBody()));
        }
        logger.debug("ジャニ以外Twitter Fav END");
        return RepeatStatus.FINISHED;
    }
}
