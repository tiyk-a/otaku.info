package otaku.info.batch.tasklet;

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
import otaku.info.controller.LoggerController;
import otaku.info.enums.TeamEnum;
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

    @Autowired
    LoggerController loggerController;

    @Autowired
    Setting setting;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        loggerController.printTwFavTasklet("ジャニTwitter Fav START");
        List<Long> teamIdList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getId).collect(Collectors.toList());
        for (Long teamId : teamIdList) {

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            loggerController.printTwFavTasklet("teamId=" + teamId + "の検索＆ファボ中");
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPythonTwitter() + "twSearch?q=" + TeamEnum.get(teamId).getMnemonic() + "&teamId=" + teamId, String.class);
            loggerController.printTwFavTasklet("teamId=" + teamId + "の検索＆ファボ結果：" + Objects.requireNonNull(response.getBody()));
        }
        loggerController.printTwFavTasklet("ジャニTwitter Fav END");

        loggerController.printTwFavTasklet("ジャニ以外Twitter Fav START");
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
            loggerController.printTwFavTasklet("teamId=" + entry.getKey() + "のフォロバ中");
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPythonTwitter() + "twSearch?q=" + entry.getValue() + "&teamId=" + entry.getKey(), String.class);
            loggerController.printTwFavTasklet("teamId=" + entry.getKey() + "のフォロバ結果：" + Objects.requireNonNull(response.getBody()));
        }
        loggerController.printTwFavTasklet("ジャニ以外Twitter Fav END");
        return RepeatStatus.FINISHED;
    }
}
