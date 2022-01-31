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
 * Twitter Follow Backバッチ
 * 【注意】ジャニ以外のアカウントも乗っかってます
 * チームIDを順番にpyTwi2へ飛ばすだけの処理内容
 *
 */
@Component
@StepScope
public class TwFolBTasklet implements Tasklet {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("TwFolBTasklet");

    @Autowired
    Setting setting;

    @Autowired
    LineController lineController;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        logger.debug("ジャニTwitter Follow Back START");
        lineController.post("Follow Back Job始まったよ！");

        List<Long> teamIdList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getId).collect(Collectors.toList());
        for (Long teamId : teamIdList) {

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            logger.info("teamId=" + teamId + "のフォロバ中");
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPythonTwitter() + "twFolB?teamId=" + teamId, String.class);
            logger.info("teamId=" + teamId + "のフォロバ結果：" + Objects.requireNonNull(response.getBody()));
        }
        logger.debug("ジャニTwitter Follow Back END");

        logger.debug("ジャニ以外Twitter Follow Back START");
        // 100: @LjtYdg, 101: @ChiccaSalak, 102: @BlogChicca, 103: @Berry_chicca
        int[] idArr = {100, 101, 102, 103};
        List<Integer> idList = Arrays.stream(idArr).boxed().collect(Collectors.toList());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        for (Integer id : idList) {
            logger.info("teamId=" + id + "のフォロバ中");
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPythonTwitter() + "twFolB?teamId=" + id, String.class);
            logger.info("teamId=" + id + "のフォロバ結果：" + Objects.requireNonNull(response.getBody()));
        }
        logger.debug("ジャニ以外Twitter Follow Back END");
        return RepeatStatus.FINISHED;
    }
}
