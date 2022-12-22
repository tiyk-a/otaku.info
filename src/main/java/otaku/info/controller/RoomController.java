package otaku.info.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
     * TODO：ここ使ってない
     * @return
     */
    @GetMapping("/")
    public ResponseEntity<RoomFront> getRoot() {
        // 画面に表示するオブジェクト
        RoomFront roomFront = new RoomFront();
        roomFront.setUserList(roomSampleDataService.findUserIdList());
        roomFront.setLikeCount(roomUserService.findLikeCountByUserId(setting.getRoomUserId()));
        RoomSampleData roomSampleData = roomSampleDataService.findByDataId("latestMyLike", setting.getRoomUserId());
        roomFront.setLatestLikeCount(Integer.parseInt(roomSampleData.getData1()));
        return ResponseEntity.ok(roomFront);
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
        // いいね数でソートしたいのでtmpマップを用意する
        Map<Integer, List<String>> tmpResMap = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<String, Integer> entry : likedUserCountMap.entrySet()) {
            String userName = roomUserService.findUserNameByUserId(entry.getKey());
            if (userName == null) {
                userName = entry.getKey();
            }

            List<String> tmpList;
            if (tmpResMap.containsKey(entry.getValue())) {
                tmpList = tmpResMap.get(entry.getValue());
            } else {
                tmpList = new ArrayList<>();
            }
            tmpList.add(userName);
            tmpResMap.put(entry.getValue(), tmpList);
        }

        // mapの中、いいねもらった数ごとにまとまってるのでレスポンスに当てるよう成形する
        for (Map.Entry<Integer, List<String>> entry : tmpResMap.entrySet()) {
            entry.getValue().forEach(e -> resList.add(entry.getKey() + ":" + e));
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
        seekLikeInner(targetUser);
        return ResponseEntity.ok(true);
    }

    /**
     * 自分の最新いいね数を更新します
     * APIで取得、取ったデータはroom_sample_dataにtmpデータとして保存
     *
     * @return
     */
    @GetMapping("/latest_mylike")
    public ResponseEntity<Integer> getLatestMyLike() {
        String url = setting.getRoomApi() + setting.getRoomUserId() + "/collects?limit=1";
        RestTemplate restTemplate = new RestTemplate();

        // API飛ばしたくない時はここ
        // res = devData();
        System.out.println(url);
        String res = restTemplate.getForObject(url, String.class);
        Integer count = 0;

        // ここからAPI結果の処理
        if (StringUtils.hasText(res)) {
            // ここで詰め込む
            JSONObject jo = null;
            try {
                jo = new JSONObject(res);
                if (jo.get("status").equals("success")) {
                    JSONArray dataArray = (JSONArray) jo.get("data");
                    JSONObject jsonObject1 = (JSONObject) dataArray.get(0);
                    JSONObject user = (JSONObject) jsonObject1.get("user");
                    count = (int) user.get("likes");

                    RoomSampleData roomSampleData = roomSampleDataService.findByDataId("latestMyLike", setting.getRoomUserId());
                    if (roomSampleData == null) {
                        roomSampleData = new RoomSampleData();
                        roomSampleData.setUser_id(setting.getRoomUserId());
                        roomSampleData.setData_id("latestMyLike");
                    }

                    roomSampleData.setData1(count.toString());
                    roomSampleDataService.save(roomSampleData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok(count);
    }

    /**
     * アカウントのリンク入力したらID読み出して、その人の最近1000件いいね誰にしてるのかをだす、そしたら比較できる
     * 1ユーザーについてだけ調べる
     * 時間かかるからフロントに返せない。DBに保存しよう。
     */
    public void seekLikeInner(String targetUser) {

        if (targetUser == null || targetUser.equals("")) {
            return;
        }

        // 自分ユーザーを調べる場合で、room_userを更新したい時はこれをtrueにしたい。
        // バッチで動かす時、1日1回は自分のいいねカウントを更新したい→今日いくついいねできるかわかるかな？って思ってる
        boolean myUserIdFlg = false;
        if (targetUser.equals(setting.getRoomUserId())) {
            myUserIdFlg = true;
        }

        String url = setting.getRoomApi() + targetUser + setting.getRoomLike();
        Boolean nextFlg = true;

        RoomLikeDto roomLikeDto = new RoomLikeDto();
        roomLikeDto.setNextUrl(url);
        roomLikeDto.setCount(0);
        while (nextFlg) {
            roomLikeDto = seekLike(roomLikeDto, targetUser, myUserIdFlg);
            if (roomLikeDto.getCount() >= 1000 || roomLikeDto.getNextUrl().equals("")) {
                nextFlg = false;
            }
        }

        // room_userにレコードなかったら登録
        searchAndInsertRoomUser(targetUser);
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
     * myUserIdFlg = trueの場合、roomUserを更新する
     *
     * @param roomLikeDto
     * @return
     */
    private RoomLikeDto seekLike(RoomLikeDto roomLikeDto, String userId, Boolean myUserIdFlg) {
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

                    // 1つめエレメントの時だけtrue
                    Boolean fstElemFlg = true;
                    for (Object obj : dataArray) {
                        JSONObject jsonObject1 = (JSONObject) obj;
                        JSONObject userO = (JSONObject) jsonObject1.get("user");
                        String likedUserId = userO.get("id").toString();
                        String username = userO.get("username").toString();
                        userSet.add(likedUserId + ":" + username);
                        count ++;

                        // フラグが合致してたらroomUserのいいねカウントだけ更新する
                        // 私ユーザーでバッチ流した時にいいねカウントも更新したいの
                        if (myUserIdFlg && fstElemFlg && userO.has("likes")) {
                            RoomUser roomUser = roomUserService.findByUserId(userId);
                            roomUser.setLike_count(userO.getInt("likes"));
                            roomUserService.save(roomUser);
                            fstElemFlg = false;
                        }
                    }

                    String stringUserSet = String.join(",", userSet);
                    roomSampleData.setData1(stringUserSet);

                    roomSampleDataService.save(roomSampleData);
                    roomLikeDto.setCount(count);

                    JSONObject metaObj = (JSONObject) jo.get("meta");
                    if (count < 1000 && !metaObj.get("next_page").equals("")) {
                        roomLikeDto.setNextUrl(metaObj.get("next_page").toString());
                    } else {
                        roomLikeDto.setNextUrl("");
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
     * コレから7日以内：毎日カウント
     * 7日以上経過：偶数日には偶数IDのみカウント、奇数日には奇数ID飲みカウント
     */
    public void execRoomLikeCount() {
        // 昨日もらった良いねとの差分集計（その人から何件良いねもらってるか、昨日その人から何件いいねもらったか

        // *** 私が昨日いいねした人からレスがあるか、昨日いいねした人ひ過去何件良いねしてて何件返してもらってるか
        try {
            // 昨日私がした良いねを全部回収する（誰にいくついいねしたか）
            seekLikeInner(setting.getRoomUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // *** APIを叩いて最新100件の商品を取得する
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // *** いいね管理DBの方いく。DBより、updateFlg = trueの場合は更新
        try {
            // いいね数に変動があった商品リスト
            List<RoomMyItem> roomMyItemList = roomMyItemService.findUpdTarget();

            if (roomMyItemList.size() > 0) {
                for (RoomMyItem roomMyItem : roomMyItemList) {
                    // 投稿日から処理有無判断を行う
                    boolean importFlg = checkIfImportToday(roomMyItem.getPostedDate(), roomMyItem.getItem_id());

                    // 今日インポート対象ではなかったらAPI呼ばず次のレコードへ
                    if (!importFlg) {
                        continue;
                    }

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
                    List<String> newLikedUserList = new ArrayList<>(targetUserIdList);
                    List<String> newMinusUserList = new ArrayList<>(currentLikeUserList);

                    newLikedUserList.removeAll(currentLikeUserList);
                    newMinusUserList.removeAll(targetUserIdList);

                    RoomItemLike roomItemLike = new RoomItemLike();
                    roomItemLike.setItem_id(roomMyItem.getItem_id());
                    roomItemLike.setAdded_user(String.join(",", newLikedUserList));
                    roomItemLike.setMinus_user(String.join(",", newMinusUserList));
                    roomItemLikeService.save(roomItemLike);

                    // いいね数更新の記録が終わったので、マスタデータも更新する
                    roomMyItem.setNewLikeCount(0);
                    roomMyItemService.save(roomMyItem);

                    // added_userに追加したユーザーのうち、RoomUserに登録がない人は登録します
                    List<String> existUserList = roomUserService.findUserIdListByUserId(newLikedUserList);
                    List<String> diff = new ArrayList<>(newLikedUserList);
                    diff.removeAll(existUserList);
                    if (diff.size() > 0) {
                        for (String userId : diff) {
                            searchAndInsertRoomUser(userId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 引数で渡された商品（RoomMyItemから必要項目だけ抽出して渡してる）が
     * 今日いいねカウント更新すべきか、明日すべきかを判定する
     *
     * @param postedDateStr
     * @param itemId
     * @return
     * @throws ParseException
     */
    private boolean checkIfImportToday(String postedDateStr, String itemId) throws ParseException {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date postedDate = sdFormat.parse(postedDateStr);
        boolean importFlg = true;
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));

        // 7日以内に投稿されたコレは処理対象、もっと前なら処理対象外を決める
        if (postedDate.before(sevenDaysAgo)) {
            boolean dateFlg = new DateTime(postedDate).getDayOfMonth() % 2 == 0;
            int lastDigit = Integer.parseInt(itemId.substring(itemId.length() -1));
            if (dateFlg) {
                // 偶数日の場合、itemIdが2で割り切れるなら処理対象、割れないなら処理対象外
                if (lastDigit % 2 != 0) {
                    importFlg = false;
                }
            } else {
                // 奇数日の場合、itemIdが2で割り切れないなら処理対象、割れるなら処理対象外
                if (lastDigit % 2 == 0) {
                    importFlg = false;
                }
            }
        }
        return importFlg;
    }

    /**
     * APIで楽天ROOMユーザー検索して登録する
     *
     * @param userId
     */
    public void searchAndInsertRoomUser(String userId) {
        if (userId == null || userId.equals("")) {
            return;
        }

        String userName = roomUserService.findUserNameByUserId(userId);
        if (userName != null) {
            return;
        }

        String url2 = setting.getRoomApi() + userId + "/collects?limit=1";
        RestTemplate restTemplate = new RestTemplate();

        // API飛ばしたくない時はここ
        // res = devData();
        System.out.println(url2);
        String res = restTemplate.getForObject(url2, String.class);

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

    /**
     *
     * @param roomLikeDto
     * @return
     */
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

/**
 * フロント画面表示の時に使うデータ
 * 用のクラス
 */
@Getter
@Setter
class RoomFront {
    // 初期表示で使いたいユーザーリスト
    List<String> userList;

    // 私のアカウントのいいねカウント
    int likeCount;

    // roomSampleDataから最新のいいねカウントがある場合は取得
    int latestLikeCount;
}