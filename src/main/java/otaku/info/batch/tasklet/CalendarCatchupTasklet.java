package otaku.info.batch.tasklet;

import com.google.api.services.calendar.model.Event;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.CalendarApiController;
import otaku.info.controller.LoggerController;
import otaku.info.entity.IM;
import otaku.info.entity.IMRel;
import otaku.info.entity.Item;
import otaku.info.enums.TeamEnum;
import otaku.info.service.IMRelService;
import otaku.info.service.IMService;
import otaku.info.service.ItemService;
import otaku.info.utils.StringUtilsMine;

import java.util.List;

/**
 * Google Calendarの予定をキャッチアップします
 * カレンダー登録がされていないIMRELデータがあれば、登録します
 */
@Component
@StepScope
public class CalendarCatchupTasklet implements Tasklet {

    @Autowired
    CalendarApiController calendarApiController;

    @Autowired
    LoggerController loggerController;

    @Autowired
    IMService imService;

    @Autowired
    ItemService itemService;

    @Autowired
    IMRelService imRelService;

    @Autowired
    StringUtilsMine stringUtilsMine;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        loggerController.printCalendarCatchupTaskletLogger("*** CalendarCatchupTasklet START ***");
        // カレンダー登録がされていないIMRELを集めます
        List<IMRel> imRelList = imRelService.findByCalIdIsNull();

        loggerController.printCalendarCatchupTaskletLogger("imRelList size: " + imRelList.size());
        if (imRelList.size() > 0) {
            for (IMRel imRel : imRelList) {
                IM im = imService.findById(imRel.getIm_id());
                String url = stringUtilsMine.getAmazonLinkFromCard(im.getAmazon_image()).orElse(null);

                if (url == null) {
                    List<Item> itemList = itemService.findByMasterId(im.getIm_id());
                    for (Item item : itemList) {
                        if (item.getUrl() != null && !item.getUrl().isEmpty()) {
                            url = item.getUrl();
                            break;
                        }
                    }
                }
                loggerController.printCalendarCatchupTaskletLogger("Event post:" + im.getIm_id() + ":" + im.getTitle());

                Event event = calendarApiController.postEvent(TeamEnum.get(imRel.getTeam_id()).getCalendarId(), im.getPublication_date(), im.getPublication_date(), im.getTitle(), url, true);

                loggerController.printCalendarCatchupTaskletLogger("event insert status: " + event.getId());

                if (event.getId() != null) {
                    imRel.setCalendar_id(event.getId());
                    imRelService.save(imRel);
                    loggerController.printCalendarCatchupTaskletLogger("Imrel updated: " + imRel.getIm_rel_id());
                }
            }
        }

        loggerController.printCalendarCatchupTaskletLogger("*** CalendarCatchupTasklet END ***");
        return RepeatStatus.FINISHED;
    }
}
