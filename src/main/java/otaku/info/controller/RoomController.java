package otaku.info.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.RoomLikeDto;
import otaku.info.entity.RoomItemLike;
import otaku.info.entity.RoomMyItem;
import otaku.info.entity.RoomSampleData;
import otaku.info.entity.RoomUser;
import otaku.info.service.RoomItemLikeService;
import otaku.info.service.RoomMyItemService;
import otaku.info.service.RoomSampleDataService;
import otaku.info.service.RoomUserService;
import otaku.info.setting.Setting;
import otaku.info.utils.JsonUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
    RoomMyItemService roomMyItemService;

    @Autowired
    RoomItemLikeService roomItemLikeService;

    @Autowired
    RoomUserService roomUserService;

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
        // TODO: そのうち変えるね、ユーザーネーム返したり
        List<String> userList = roomSampleDataService.findUserIdList();
        return ResponseEntity.ok(userList);
    }

    /**
     * いいねすべき人を返す
     * パラメータによってどんな人を返すか変える
     *
     * @return
     */
    @GetMapping("/sug")
    public ResponseEntity<List<String>> getSuggestion() {
        // 今は前日いいねしてくれた人（added_user）
        List<String> userList = roomItemLikeService.findByCreatedInADay();
        Map<String, Integer> likedUserCountMap = new HashMap<>();

        // カンマでsplitして、あと数をカウント
        for (String users : userList) {
            List<String> tmp = List.of(users.split(","));
            for (String u : tmp) {
                if (likedUserCountMap.containsKey(u)) {
                    Integer count = likedUserCountMap.get(u) + 1;
                    likedUserCountMap.put(u, count);
                } else {
                    likedUserCountMap.put(u, 1);
                }
            }
        }

        List<String> resList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : likedUserCountMap.entrySet()) {
            String userName = roomUserService.findUserNameByUserId(entry.getKey());
            if (userName == null) {
                userName = entry.getKey();
            }
            resList.add(entry.getValue() + ":" + userName);
        }
        return ResponseEntity.ok(resList);
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
        System.out.println(roomLikeDto.getNextUrl());
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

    /**
     * いいね数カウントのバッチジョブ中身
     */
    public void execRoomLikeCount() {
        // *** APIを叩いて最新100件の商品を取得する
        String url = setting.getRoomApi() + setting.getRoomUserId() + setting.getRoomCollects();
        Boolean nextFlg = true;
        RoomLikeDto roomLikeDto = new RoomLikeDto();
        roomLikeDto.setNextUrl(url);

        while (nextFlg) {
            roomLikeDto = roomLikeCount(roomLikeDto);
            if (roomLikeDto.getNextUrl().equals("")) {
                nextFlg = false;
            }
        }

        // *** いいね管理DBの方いく。DBより、updateFlg = trueの場合は更新
        // いいね数に変動があった商品リスト
        List<RoomMyItem> roomMyItemList = roomMyItemService.findUpdTarget();

        if (roomMyItemList.size() > 0) {
            for (RoomMyItem roomMyItem : roomMyItemList) {
                RoomItemLikeUpd roomItemLikeUpd = new RoomItemLikeUpd();
                RoomLikeDto roomLikeDto1 = new RoomLikeDto();
                // リクエスト URL: https://room.rakuten.co.jp/api/1700183991491820/users_liked?limit=100
                String url1 = setting.getRoomApi() + roomMyItem.getItem_id() + setting.getRoomUsersLiked();
                roomLikeDto1.setNextUrl(url1);
                roomItemLikeUpd.setRoomLikeDto(roomLikeDto1);
                roomItemLikeUpd.setUserIdNameMap(new HashMap<>());

                Boolean nextFlg1 = true;
                while (nextFlg1) {
                    roomItemLikeUpd = roomLikeUpd(roomItemLikeUpd);
                    if (roomItemLikeUpd.getRoomLikeDto().getNextUrl().equals("")) {
                        nextFlg1 = false;
                    }
                }

                // データ集め終わったので、差分を更新
                // APIで取ってきたデータ
                Map<String, String> targetUserIdNameMap = roomItemLikeUpd.getUserIdNameMap();
                List<String> targetUserIdList = new ArrayList<>(targetUserIdNameMap.keySet());
                // DBに保存してあるデータ
                List<RoomItemLike> roomItemLikeList = roomItemLikeService.findByItemId(roomMyItem.getItem_id());
                List<String> currentLikeUserList = collectCurrentLikeUser(roomItemLikeList);

                // APIでとってきたデータとDB保存から、差分を見つけ保存する
                List<String> newLikedUserList = compareArrayElemsNotArg1Contains(currentLikeUserList, targetUserIdList);
                List<String> newMinusLuserList = compareArrayElemsNotArg1Contains(targetUserIdList, currentLikeUserList);

                RoomItemLike roomItemLike = new RoomItemLike();
                roomItemLike.setItem_id(roomMyItem.getItem_id());
                roomItemLike.setAdded_user(String.join(",", newLikedUserList));
                roomItemLike.setMinus_user(String.join(",", newMinusLuserList));
                roomItemLikeService.save(roomItemLike);

                // いいね数更新の記録が終わったので、マスタデータも更新する
                roomMyItem.setNewLikeCount(0);
                roomMyItemService.save(roomMyItem);
            }
        }
    }

    /**
     * 引数2のうち、引数1に入っていない要素を返す
     *
     * @param currentLikeUserList
     * @param targetUserIdList
     * @return
     */
    private List<String> compareArrayElemsNotArg1Contains(List<String> currentLikeUserList, List<String> targetUserIdList) {
        List<String> resList = new ArrayList<>();
        if (targetUserIdList == null || targetUserIdList.size() == 0) {
            return resList;
        }

        for (String target : targetUserIdList) {
            if (!currentLikeUserList.contains(target)) {
                resList.add(target);
            }
        }
        return resList;
    }

    /**
     * 既存でいいねしてる人だけ返す。
     * いいねしたけど消した人とかは弾く
     *
     * @param roomItemLikeList
     * @return
     */
    private List<String> collectCurrentLikeUser(List<RoomItemLike> roomItemLikeList) {
        // そもそも既存いいねが0の場合
        if (roomItemLikeList.size() == 0) {
            return new ArrayList<>();
        }

        // 既存いいねと、いいね取り消した人を集める
        List<String> tmpLikeUserStrList = new ArrayList<>();
        List<String> tmpMinusUserStrList = new ArrayList<>();
        for (RoomItemLike roomItemLike : roomItemLikeList) {
            tmpLikeUserStrList.add(roomItemLike.getAdded_user());
            tmpMinusUserStrList.add(roomItemLike.getMinus_user());
        }

        // いいね取り消した人がいるなら、マップに詰める（回数と一緒に）
        Map<String, Integer> minusUserMap = new HashMap<>();
        if (tmpMinusUserStrList.size() > 0) {
            for (String tmpMinusUserStr : tmpMinusUserStrList) {
                List<String> tmp = List.of(tmpMinusUserStr.split(","));
                for (String tmpUser : tmp) {
                    if (minusUserMap.containsKey(tmpUser)) {
                        Integer count = minusUserMap.get(tmpUser);
                        count ++;
                        minusUserMap.put(tmpUser, count);
                    } else {
                        minusUserMap.put(tmpUser, 1);
                    }
                }
            }
        }

        // いいねした人がいるなら、マップに詰める（回数と一緒に）
        Map<String, Integer> likeUserMap = new HashMap<>();
        if (tmpLikeUserStrList.size() > 0) {
            for (String tmpLikeUserStr : tmpLikeUserStrList) {
                List<String> tmp = List.of(tmpLikeUserStr.split(","));
                for (String tmpUser : tmp) {
                    if (likeUserMap.containsKey(tmpUser)) {
                        Integer count = likeUserMap.get(tmpUser);
                        count ++;
                        likeUserMap.put(tmpUser, count);
                    } else {
                        likeUserMap.put(tmpUser, 1);
                    }
                }
            }
        }

        // 戻り値に使うリストを用意
        List<String> resList = new ArrayList<>();
        for (Map.Entry<String, Integer> e : likeUserMap.entrySet()) {
            if (e.getValue() == 1) {
                // 1回だけいいねつけてる人なら文句なしにリストへ
                resList.add(e.getKey());
            } else {
                // 複数回いいねしてる人なら、最新版でいいねになってたらリストへ
                if (minusUserMap.containsKey(e.getKey())) {
                    Integer minusCount = minusUserMap.get(e.getKey());
                    if (e.getValue() > minusCount) {
                        resList.add(e.getKey());
                    }
                }
            }
        }
        return resList;
    }

    public RoomLikeDto roomLikeCount(RoomLikeDto roomLikeDto) {
        RestTemplate restTemplate = new RestTemplate();

        // API飛ばしたくない時はここ
        // res = devData();
        System.out.println(roomLikeDto.getNextUrl());
        String res = restTemplate.getForObject(roomLikeDto.getNextUrl(), String.class);

        // ここからAPI結果の処理
        if (StringUtils.hasText(res)) {
            // ここで詰め込む
            JSONObject jo = null;
            try {
                jo = new JSONObject(res);
                if (jo.get("status").equals("success")) {
                    JSONArray dataArray = (JSONArray) jo.get("data");
                    for (Object obj : dataArray) {
                        JSONObject jsonObject1 = (JSONObject) obj;

                        // 商品IDを取得
                        String itemId = jsonObject1.getString("id");

                        RoomMyItem roomMyItem = roomMyItemService.findByItemId(itemId);
                        if (roomMyItem == null) {
                            // DBにない商品を見つけたら、新規商品登録
                            roomMyItem = new RoomMyItem();
                            roomMyItem.setItem_id(itemId);
                            roomMyItem.setLikes((int) jsonObject1.get("likes"));
                            roomMyItem.setPostedDate(jsonObject1.getString("created_at"));
                            roomMyItem.setNewLikeCount((int) jsonObject1.get("likes"));
                            roomMyItemService.save(roomMyItem);
                        } else {
                            // DBにある商品だったら、いいねカウントだけ更新しようか
                            int currentLikes = roomMyItem.getLikes();
                            int newLikes = (int) jsonObject1.get("likes");

                            // 差分がある場合は更新
                            if (currentLikes != newLikes) {
                                roomMyItem.setLikes(newLikes);
                                roomMyItem.setNewLikeCount(newLikes - currentLikes);
                                roomMyItemService.save(roomMyItem);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONObject metaObj = (JSONObject) jo.get("meta");
            if (metaObj.keySet().contains("next_page")) {
                roomLikeDto.setNextUrl(metaObj.get("next_page").toString());
            } else {
                roomLikeDto.setNextUrl("");
            }
        }
        return roomLikeDto;
    }

    /**
     * 1商品にいいねした人についての情報を集めます
     * @param roomItemLikeUpd
     * @return
     */
    public RoomItemLikeUpd roomLikeUpd(RoomItemLikeUpd roomItemLikeUpd) {
        RestTemplate restTemplate = new RestTemplate();
        RoomLikeDto roomLikeDto = roomItemLikeUpd.getRoomLikeDto();

        // API飛ばしたくない時はここ
        // res = devData();
        System.out.println(roomLikeDto.getNextUrl());
        String res = restTemplate.getForObject(roomLikeDto.getNextUrl(), String.class);

        // ここからAPI結果の処理
        if (StringUtils.hasText(res)) {
            // ここで詰め込む
            JSONObject jo = null;
            try {
                jo = new JSONObject(res);
                if (jo.get("status").equals("success")) {
                    JSONArray dataArray = (JSONArray) jo.get("data");

                    Map<String, String> userIdNameMap = roomItemLikeUpd.getUserIdNameMap();

                    for (Object obj : dataArray) {
                        JSONObject jsonObject1 = (JSONObject) obj;
                        userIdNameMap.put(jsonObject1.getString("id"), jsonObject1.getString("username"));
                    }
                    roomItemLikeUpd.setUserIdNameMap(userIdNameMap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONObject metaObj = (JSONObject) jo.get("meta");
            if (metaObj.keySet().contains("next_page")) {
                roomLikeDto.setNextUrl(metaObj.get("next_page").toString());
            } else {
                roomLikeDto.setNextUrl("");
            }
            roomItemLikeUpd.setRoomLikeDto(roomLikeDto);
        }
        return roomItemLikeUpd;
    }

    public void userIdToName() {
        List<String> userIdList = roomItemLikeService.findAll().stream().map(e -> e.getAdded_user()).collect(Collectors.toList());
        List<String> noDupUserIdList = new ArrayList<>();
        for (String userIdStr : userIdList) {
            List<String> tmp = List.of(userIdStr.split(","));
            for (String u : tmp) {
                if (!noDupUserIdList.contains(u)) {
                    noDupUserIdList.add(u);
                }
            }
        }

        for (String userId : noDupUserIdList) {
            if (userId == null || userId.equals("")) {
                continue;
            }

            String userName = roomUserService.findUserNameByUserId(userId);
            if (userName != null) {
                continue;
            }

            String url = setting.getRoomApi() + userId + "/collects?limit=1";
            RestTemplate restTemplate = new RestTemplate();

            // API飛ばしたくない時はここ
            // res = devData();
            System.out.println(url);
            String res = restTemplate.getForObject(url, String.class);

            // ここからAPI結果の処理
            if (StringUtils.hasText(res)) {
                // ここで詰め込む
                JSONObject jo = null;
                try {
                    jo = new JSONObject(res);
                    if (jo.get("status").equals("success")) {
                        JSONArray dataArray = (JSONArray) jo.get("data");

                        JSONObject jsonObject1 = (JSONObject) dataArray.get(0);
                        JSONObject userO = (JSONObject) jsonObject1.get("user");
                        String likedUserId = userO.get("id").toString();
                        String username = userO.get("username").toString();
                        RoomUser roomUser = new RoomUser();
                        roomUser.setUser_id(likedUserId);
                        roomUser.setUsername(username);
                        Object followable = userO.get("is_followable");
                        if (followable == null) {
                            roomUser.setFollow(true);
                        } else if (followable.equals("null")) {
                            roomUser.setFollow(true);
                        } else {
                            roomUser.setFollow(false);
                        }
                        roomUser.setLike_count((int) userO.get("likes"));

                        roomUser.setUser_rank(userO.get("rank").toString());
                        roomUserService.save(roomUser);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

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

/**
 * いいね数更新の調査はrecursiveでデータ持ち回る必要があるので
 * それ用のクラス
 */
@Getter
@Setter
class RoomItemLikeUpd {
    RoomLikeDto roomLikeDto;
    Map<String, String> userIdNameMap;
}