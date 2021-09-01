package otaku.info.batch.tasklet;

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

import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
public class RakutenSearchMemberTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    MemberService memberService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println("--- 楽天新商品検索（個人） START ---");
        List<MemberSearchDto> dtoList = new ArrayList<>();
        memberService.findAllMember().forEach(e -> dtoList.add(e.convertToDto()));
        for (MemberSearchDto dto : dtoList) {
            System.out.println("***** SEARCH: " + dto.getMember_name() + "*****");
            sampleController.searchItem(dto.getTeam_id(), dto.getMember_name(), dto.getMember_id());
            System.out.println("***** END: " + dto.getMember_name() + "*****");
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        System.out.println("--- 楽天新商品検索（個人） END ---");
        return RepeatStatus.FINISHED;
    }
}
