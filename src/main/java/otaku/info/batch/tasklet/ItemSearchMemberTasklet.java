package otaku.info.batch.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.LoggerController;
import otaku.info.controller.SampleController;
import otaku.info.dto.MemberSearchDto;
import otaku.info.service.MemberService;

import java.util.ArrayList;
import java.util.List;

/**
 * メンバー個人の商品検索処理
 *
 */
@Component
@StepScope
public class ItemSearchMemberTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    MemberService memberService;

    /**
     * 各チーム個人名での新商品検索
     * ①楽天
     * ②Yahoo
     * 
     * @param contribution
     * @param chunkContext
     * @return
     * @throws Exception
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<MemberSearchDto> dtoList = new ArrayList<>();
        memberService.findAllMember().forEach(e -> dtoList.add(e.convertToMemberSearchDto()));
        for (MemberSearchDto dto : dtoList) {
            loggerController.printItemSearchMemberTasklet("***** SEARCH: " + dto.getMember_name() + "*****");
            sampleController.searchItem(dto.getTeam_id(), dto.getMember_name(), dto.getMember_id(), 1L);
            loggerController.printItemSearchMemberTasklet("***** END: " + dto.getMember_name() + "*****");
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return RepeatStatus.FINISHED;
    }
}
