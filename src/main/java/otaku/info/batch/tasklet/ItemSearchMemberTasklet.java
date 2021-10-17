package otaku.info.batch.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.SampleController;
import otaku.info.dto.MemberSearchDto;
import otaku.info.searvice.MemberService;
import otaku.info.setting.Log4jUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
public class ItemSearchMemberTasklet implements Tasklet {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger();

    @Autowired
    SampleController sampleController;

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
        memberService.findAllMember().forEach(e -> dtoList.add(e.convertToDto()));
        for (MemberSearchDto dto : dtoList) {
            logger.debug("***** SEARCH: " + dto.getMember_name() + "*****");
            sampleController.searchItem(dto.getTeam_id(), dto.getMember_name(), dto.getMember_id(), 1L);
            logger.debug("***** END: " + dto.getMember_name() + "*****");
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        return RepeatStatus.FINISHED;
    }
}
