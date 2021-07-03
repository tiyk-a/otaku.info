package otaku.info.controller;

//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//import otaku.info.searvice.csv.CsvService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController("/search")
public class Controller {

//    private static CsvService csvService;
//    private static final String CSV_PATH = "AffiliSearchGenre.csv";
    private static final List<String> searchList = new ArrayList<String>(Arrays.asList("雑誌", "CD", "DVD"));

    /**
     * アフィリサイトの検索キーワードを生成し返却する
     * @param artist アーティスト名
     * return List<String> 検索キーワードリスト
     */
    public static List<String> affiliSearchWord(String artist){
        // アフィリサイトでの検索ワード一覧
//        List<List<String>> searchList = csvService.getCsv(CSV_PATH);
        List<String> resultList = new ArrayList<>();
        searchList.forEach(arr -> resultList.add(String.join(" ",artist, arr)));
        return resultList;
    }
}
