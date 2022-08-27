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
import otaku.info.controller.LineController;
import otaku.info.controller.LoggerController;
import otaku.info.enums.TeamEnum;
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

    @Autowired
    LoggerController loggerController;

    @Autowired
    LineController lineController;

    @Autowired
    Setting setting;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        loggerController.printTwFolBTasklet("ジャニTwitter Follow Back START");

        List<Long> teamIdList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getId).collect(Collectors.toList());
        for (Long teamId : teamIdList) {

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

            loggerController.printTwFolBTasklet("teamId=" + teamId + "のフォロバ中");
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPyTwi2() + "twFolB?teamId=" + teamId, String.class);
            loggerController.printTwFolBTasklet("teamId=" + teamId + "のフォロバ結果：" + Objects.requireNonNull(response.getBody()));
        }
        loggerController.printTwFolBTasklet("ジャニTwitter Follow Back END");

        loggerController.printTwFolBTasklet("ジャニ以外Twitter Follow Back START");
        // 100: @LjtYdg, 101: @ChiccaSalak, 102: @BlogChicca, 103: @Berry_chicca
        int[] idArr = {100, 101, 102, 103, 104};
        List<Integer> idList = Arrays.stream(idArr).boxed().collect(Collectors.toList());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        for (Integer id : idList) {
            loggerController.printTwFolBTasklet("teamId=" + id + "のフォロバ中");
            ResponseEntity<String> response = restTemplate.getForEntity(setting.getPyTwi2() + "twFolB?teamId=" + id, String.class);
            loggerController.printTwFolBTasklet("teamId=" + id + "のフォロバ結果：" + Objects.requireNonNull(response.getBody()));
        }
        loggerController.printTwFolBTasklet("ジャニ以外Twitter Follow Back END");
        lineController.post("フォロバの処理が走りました");
        return RepeatStatus.FINISHED;
    }
}
