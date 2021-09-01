package otaku.info.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.entity.Item;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class ItemUtils {

    @Autowired
    private DateUtils dateUtils;

    /**
     * 引数で渡されてきたItemリストをpublication_dateで間引いて返します。
     * 発売日-今日が100で割り切れる日
     * 発売日-今日が100日以下で10で割り切れる日
     *
     * @param itemList
     * @return
     */
    public List<Item> roundByPublicationDate(List<Item> itemList) {

        List<Item> resultList = new ArrayList<>();

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        Date today = todayCal.getTime();

        for (Item item : itemList) {
            int daysToPublication = dateUtils.dateDiff(today, item.getPublication_date());

            // 100日以上先の商品は100で割り切れる日のみ追加する
            if (daysToPublication > 100 && daysToPublication % 100 == 0) {
                resultList.add(item);
            } else if (daysToPublication > 10 && daysToPublication % 10 == 0) {
                // 発売日まで10日~100日以内の商品は10で割り切れる日のみ追加する
                resultList.add(item);
            } else {
                // 発売日まで10日以内の商品は全て追加する
                resultList.add(item);
            }
        }
        return resultList;
    }
}
