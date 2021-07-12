package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import otaku.info.searvice.TeamService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController("/search")
@AllArgsConstructor
public class Controller {

    private static final List<String> searchList = new ArrayList<String>(Arrays.asList("雑誌", "CD", "DVD"));

    @Autowired
    private TeamService teamService;

    /**
     * アフィリサイトの検索キーワードを生成し返却する
     * @param artist アーティスト名
     * return List<String> 検索キーワードリスト
     */
    public List<String> affiliSearchWord(String artist){
        // アフィリサイトでの検索ワード一覧
        List<String> resultList = new ArrayList<>();
        searchList.forEach(arr -> resultList.add(String.join(" ",artist, arr)));
//        if (teamService.getMnemonic(artist) != null) {
//            searchList.forEach(arr -> resultList.add(String.join(" ",teamService.getMnemonic(artist), arr)));
//        }
        return resultList;
    }
}
