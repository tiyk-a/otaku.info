package otaku.info.batch.tasklet;

import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.SampleController;
import otaku.info.entity.Item;
import otaku.info.entity.Team;
import otaku.info.searvice.ItemService;
import otaku.info.searvice.MemberService;
import otaku.info.searvice.TeamService;

import java.util.HashMap;
import java.util.List;

@Component
@StepScope
public class RakutenSearchTasklet implements Tasklet {

    @Autowired
    SampleController sampleController;

    @Autowired
    TeamService teamService;

    @Autowired
    ItemService itemService;

    @Autowired
    MemberService memberService;

    Logger logger = org.slf4j.LoggerFactory.getLogger(RakutenSearchTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("--- 楽天新商品検索 START ---");
        // tmp
        if (memberService.countDupl()) {
            System.out.println("***** DELETE AQUI!!! *****");
            // 重複したメンバーIDリスト
            List<Long> memberList = memberService.getDupl();
            // 重複したメンバーを含むItemリスト
            List<Item> itemList = itemService.getDuplMemberItemList(memberList);
            List<Item> updateList = new ArrayList<>();
            for (Item item : itemList) {
                if (item.getMember_id().contains(",")) {
                    List<Long> memberIdList = new ArrayList<>();
                    List.of(item.getMember_id().split(",")).forEach(e -> memberIdList.add((long)Integer.parseInt(e)));
                    String updateStr = "";
                    for (Long memberId : memberIdList) {
                        String memberName = memberService.getMemberName(memberId);
                        if (updateStr.equals("")) {
                            updateStr = memberService.getFstMemberId(memberName);
                        } else {
                            updateStr = updateStr + "," + memberService.getFstMemberId(memberName);
                        }
                    }
                    item.setMember_id(updateStr);
                } else {
                    Long memberId = (long)Integer.parseInt(item.getMember_id());
                    // 前方のメンバーIDをセット
                    String memberName = memberService.getMemberName(memberId);
                    item.setMember_id(memberService.getFstMemberId(memberName));
                }
                updateList.add(item);
            }
            itemService.updateAll(updateList);
            System.out.println("***** UPDATED!!! *****");
            memberService.deleteAll(memberList);
            System.out.println("***** DELETED DUPLICATED MEMBERS!!! *****");
        }
        // tmp
        List<Team> teamList = teamService.findAllTeam();
        Map<Long, String> artistMap = new HashMap<Long, String>();
        teamList.forEach(t -> artistMap.put(t.getTeam_id(), t.getTeam_name()));
        for (Map.Entry<Long, String> artist : artistMap.entrySet()) {
            logger.info("***** START: " + artist.getValue() + "*****");
            sampleController.searchItem(artist.getKey(), artist.getValue(), 0L);
            logger.info("***** END: " + artist + "*****");
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
        logger.info("--- 楽天新商品検索 END ---");
        return RepeatStatus.FINISHED;
    }
}
