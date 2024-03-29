package otaku.info.utils;

import com.atilika.kuromoji.ipadic.Tokenizer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import otaku.info.controller.TextController;
import otaku.info.entity.IM;
import otaku.info.entity.Item;
import otaku.info.service.IMService;
import otaku.info.service.ItemService;
import otaku.info.setting.Log4jUtils;

import java.util.*;

/**
 * Itemの処理を色々行う
 *
 */
@Component
public class ItemUtils {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("ItemUtils");

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private ItemService itemService;

    @Autowired
    private IMService imService;

    @Autowired
    private TextController textController;

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

    /**
     * 新しく商品マスター登録が必要かどうかを判定する。
     * 登録が必要な場合、0を返す。
     * 不要な場合、使用すべきマスターのIDを返す。
     *
     * @return
     */
    public Long judgeNewMaster(Item targetItem) {
        // 同じチーム（が含まれてる）、同じ日発売でマスタ商品の登録があるの商品を取得する。
        List<Item> itemList = itemService.findSimilarItemList(targetItem);

        Map<Long, List<Item>> masterItemMap = new HashMap<>();
        for (Item item : itemList) {

            if (item.getIm_id() != null) {
                List<Item> tmpList = new ArrayList<>();
                if (masterItemMap.size() > 0 && masterItemMap.containsKey(item.getIm_id())) {
                    tmpList = masterItemMap.get(item.getIm_id());
                }
                tmpList.add(item);
                masterItemMap.put(item.getIm_id(), tmpList);
            }
        }

        // 該当しそうなマスタ商品を探す
        // 結果を詰める[imId, 1st check score, 2nd check score]
        Set<Long[]> resultSet = new HashSet<>();
        for (Map.Entry<Long, List<Item>> e : masterItemMap.entrySet()) {

            if (e.getValue().size() == 0) {
                continue;
            }

            // 同じマスタ商品を持つ商品リストのそれぞれのタイトルとターゲット商品のタイトルを比較
            int falseCount = 0;
            Integer score = 0;
            for (Item item : e.getValue()) {
                String str1 = item.getTitle().replaceAll("(\\[.*?\\])|(\\/)| |　|(【.*?】)|(\\(.*?\\))|(\\（.*?\\）)", "");
                String str2 = targetItem.getTitle().replaceAll("(\\[.*?\\])|(\\/)| |　|(【.*?】)|(\\(.*?\\))|(\\（.*?\\）)", "");
                score = textController.getSimilarScoreByJaroWinklerDistance(str1, str2);
                if (score < 70) {
                    ++ falseCount;
                }
            }

            // 一致70点以下の商品数/トータル商品数が半数を超えていたら別商品と判断し次のマスター商品との比較へ
            if ((float) falseCount/e.getValue().size() >= 0.5) {
                continue;
            } else {
                Long[] longArray = {e.getKey(), (long) score, 0L};
                resultSet.add(longArray);
            }

            // 1つ目クリアしたら次のチェックへ
            for (Item item : e.getValue()) {
                // 日本語分割処理
                Tokenizer tokenizer = new Tokenizer();
                List<String> itemWordList = new ArrayList<>();
                List<String> targetWordList = new ArrayList<>();

                tokenizer.tokenize(item.getTitle().replaceAll("(\\[.*?\\])|(\\/)| |　|(【.*?】)|(\\(.*?\\))|(\\（.*?\\）)", "")).forEach(j -> itemWordList.add(j.getSurface()));
                tokenizer.tokenize(targetItem.getTitle().replaceAll("(\\[.*?\\])|(\\/)| |　|(【.*?】)|(\\(.*?\\))|(\\（.*?\\）)", "")).forEach(j -> targetWordList.add(j.getSurface()));

                itemWordList.addAll(targetWordList);
                int joinedLength = itemWordList.size();

                // かぶっている要素はそれぞれ削除しておく
                Set<String> set = new HashSet<>(itemWordList);
                itemWordList.clear();
                itemWordList.addAll(set);

                int distinctLength = itemWordList.size();

                // 重複しない要素が1つも残らない場合
                if (distinctLength == 0) {
                    Long[] tmpLong = resultSet.stream().filter(j -> j[0].equals(e.getKey())).findFirst().orElse(new Long[]{e.getKey(), 0L, 0L});
                    resultSet.remove(tmpLong);
                    tmpLong[2] = (long) distinctLength;
                    resultSet.add(tmpLong);
                } else if ((float) distinctLength / joinedLength < 0.6) {
                    // 重複しない要素数/重複削除前要素数が0.6より小さい場合同じ商品と判定
                    Long[] tmpLong = resultSet.stream().filter(j -> j[0].equals(e.getKey())).findFirst().orElse(new Long[]{e.getKey(), 0L, 0L});
                    resultSet.remove(tmpLong);
                    tmpLong[2] = (long) distinctLength;
                    resultSet.add(tmpLong);
                } else {
                    // 重複しない要素数/重複削除前要素数が0.5より大きい場合は違う商品と判定、配列から削除する
                    resultSet.removeIf(j -> j[0].equals(e.getKey()));
                }
            }
        }

        if (resultSet.size() > 0) {
            if (resultSet.size() == 1) {
                return resultSet.stream().findFirst().get()[0];
            } else {
                Long[] longArray = new Long[3];
                for (Long[] data : resultSet) {
                    if (longArray[0] == null) {
                        longArray = data;
                    } else {
                        // 1はスコアが高い方が強い=1が強いときtrue
                        boolean chk1 = data[1] > longArray[1];
                        // 2は値が低い方が強い=2が強いときtrue
                        boolean chk2 = data[2] > longArray[2];

                        if ((chk1 && !chk2) || (!chk1 && !chk2)) {
                            longArray = data;
                        }
                    }
                }
                return longArray[0];
            }
        }
        return 0L;
    }
}
