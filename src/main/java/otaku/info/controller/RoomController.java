package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.RoomLikeDto;
import otaku.info.entity.RoomSampleData;
import otaku.info.service.RoomSampleDataService;
import otaku.info.setting.Setting;
import otaku.info.utils.JsonUtils;

import java.io.File;
import java.util.*;

/**
 * 楽天ROOMのコントローラ
 */
@RestController
@RequestMapping("/api/room")
@AllArgsConstructor
public class RoomController {

    @Autowired
    RoomSampleDataService roomSampleDataService;

    @Autowired
    JsonUtils jsonUtils;

    @Autowired
    Setting setting;

    /**
     * すでにDigしてあるユーザーリストを返す
     * @return
     */
    @GetMapping("/")
    public ResponseEntity<List<String>> getRoot() {
        // TODO: そのうち帰るね、ユーザーネーム返したり
        List<String> userList = roomSampleDataService.findUserIdList();
        return ResponseEntity.ok(userList);
    }

    /**
     * アカウントのリンク入力したらID読み出して、その人の最近1000件いいね誰にしてるのかをだす、そしたら比較できる
     * 1ユーザーについてだけ調べる
     * 時間かかるからフロントに返せない。DBに保存しよう。
     */
    @PostMapping("/seek_like")
    public ResponseEntity<Boolean> seekLike(@RequestBody Map<String, Object> input) {
        if (!input.containsKey("user_id")) {
            return ResponseEntity.ok(true);
        }

        String targetUser = input.get("user_id").toString();
        String url = setting.getRoomApi() + targetUser + setting.getRoomLike();
        Boolean nextFlg = true;

        RoomLikeDto roomLikeDto = new RoomLikeDto();
        roomLikeDto.setNextUrl(url);
        roomLikeDto.setCount(0);
        while (nextFlg) {
            roomLikeDto = seekLike(roomLikeDto, targetUser);
            if (roomLikeDto.getCount() >= 1000 || roomLikeDto.getNextUrl().equals("")) {
                nextFlg = false;
            }
        }

        return ResponseEntity.ok(true);
    }

    /**
     * 指定アカウントがいいねしたアカウントを取得
     */
    @PostMapping("/likes")
    public ResponseEntity<Set<String>> likes(@RequestBody Map<String, Object> input) {
        Set<String> resSet = new HashSet<>();

        if (!input.containsKey("user_id")) {
            return ResponseEntity.ok(resSet);
        }

        String targetUser = input.get("user_id").toString();
        RoomSampleData roomSampleData = roomSampleDataService.findByDataId("Top1000", targetUser);
        if (roomSampleData == null) {
            return ResponseEntity.ok(resSet);
        } else {
            return ResponseEntity.ok(Set.of(roomSampleData.getData1().split(",")));
        }
    }

    /**
     * LIKEを数える
     *
     * @param roomLikeDto
     * @return
     */
    private RoomLikeDto seekLike(RoomLikeDto roomLikeDto, String userId) {
        RestTemplate restTemplate = new RestTemplate();
        int count = roomLikeDto.getCount();

        RoomSampleData roomSampleData = roomSampleDataService.findByDataId("Top1000", userId);
        Set<String> userSet;
        Set<String> userNameSet;
        if (roomSampleData == null) {
            roomSampleData = new RoomSampleData();
            roomSampleData.setData_id("Top1000");
            roomSampleData.setUser_id(userId);
            userSet = new HashSet<>();
        } else if (roomSampleData.getData1() == null) {
            userSet = new HashSet<>();
        } else {
            userSet = new HashSet<>(Set.of(roomSampleData.getData1().split(",")));
        }

        // https://room.rakuten.co.jp/api/1000001788282356/likes_collect
        String res = "";
        // API飛ばしたくない時はここ
//        res = devData();
        res = restTemplate.getForObject(roomLikeDto.getNextUrl(), String.class);

        // ここから
        if (StringUtils.hasText(res)) {
            // ここで詰め込む
            JSONObject jo = null;
            try {
                jo = new JSONObject(res);
                if (jo.get("status").equals("success")) {
                    JSONArray dataArray = (JSONArray) jo.get("data");
                    for (Object obj : dataArray) {
                        JSONObject jsonObject1 = (JSONObject) obj;
                        JSONObject userO = (JSONObject) jsonObject1.get("user");
                        String likedUserId = userO.get("id").toString();
                        String username = userO.get("username").toString();
                        userSet.add(likedUserId + ":" + username);
                        count ++;
                    }

                    String stringUserSet = String.join(",", userSet);
                    roomSampleData.setData1(stringUserSet);

                    roomSampleDataService.save(roomSampleData);
                    roomLikeDto.setCount(count);

                    JSONObject metaObj = (JSONObject) jo.get("meta");
                    if (count < 1000 && !metaObj.get("next_page").equals("")) {
                        roomLikeDto.setNextUrl(metaObj.get("next_page").toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return roomLikeDto;
    }

    /**
     * フォロー外すべきユーザーを知りたい
     * - [ ] 私がフォローしていて、リコレばかりの人
     */
    public void method2() {}

    /**
     * 1000いいね今日誰に幾つしたらいいか知りたい
     */
    public void method3() {}

    public void method4() {}

    public void method5() {}

    public void method6() {}

    public void method7() {}

    /**
     * APIを使いたくない時、JSON型で保存したデータをここから取れます
     *
     * @return
     */
    private String devData () {
        Scanner filein;
        String res = "";

        try {
            filein = new Scanner(new File("/Users/chiara/Desktop/json.txt"));

            while (filein.hasNext()) {
                // convert all words to lower case before putting them in the set.
                res =  res + filein.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

}
