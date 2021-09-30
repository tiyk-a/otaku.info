//package otaku.info.controller;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import org.json.*;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.AllArgsConstructor;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import otaku.info.entity.Item;
//import org.springframework.stereotype.Controller;
//import otaku.info.searvice.ItemService;
//import otaku.info.searvice.TeamService;
//import otaku.info.utils.DateUtils;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//
//@Controller
//@RequestMapping("/manage")
//@AllArgsConstructor
//public class ManageController {
//
//    private final ItemService itemService;
//
//    private final TeamService teamService;
//
//    private final DateUtils dateUtils;
//
//    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//    /**
//     * ログ一覧を返す
//     *
//     * @param model model
//     * @return String(item.html)
//     */
//    @GetMapping("/item")
//    public String index(Model model){
//
//        List<Item> items = itemService.findByFctChk(false);
//        model.addAttribute("items", items);
//        return "manage/item";
//    }
//
//    @GetMapping("/notif")
//    public String notif(@RequestParam(name = "date", required = false) String date, Model model) throws ParseException {
//        Date targetDate = null;
//        if (date != null && date.length() == 8) {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//            targetDate = sdf.parse(date);
//        }
//        if (date == null || date.length() != 8) {
//            targetDate = Calendar.getInstance().getTime();
//        }
//        Date firstDate = dateUtils.getFirstDate(targetDate);
//        Date lastDate = dateUtils.getLastDate(targetDate);
//        List<Item> items = itemService.findItemsBetween(firstDate, lastDate);
//        Map<Long, String> teamList = teamService.getAllIdNameMap();
//        List<Date> dateList = new ArrayList<>();
//        items.forEach(e -> dateList.add(e.getPublication_date()));
//        Date latestDate = dateUtils.getLatestDate(dateList);
//        model.addAttribute("targetDate", targetDate);
//        model.addAttribute("latestDate", latestDate);
//        model.addAttribute("firstDate", firstDate);
//        model.addAttribute("lastDate", lastDate);
//        model.addAttribute("items", items);
//        model.addAttribute("teamList", teamList);
//        return "manage/notif";
//    }
//
//    @ResponseBody
//    @PostMapping("/dlt_flg")
//    public void dlt_flg(HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
//        InputStream stream = request.getInputStream();
//
//        //文字列のバッファを構築
//        StringBuffer sb = new StringBuffer();
//        String line = "";
//        //文字型入力ストリームを作成
//        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
//        //読めなくなるまでwhile文で回す
//        while((line = br.readLine()) != null) {
//            sb.append(line);
//        }
//        String script = sb.toString();
//
//        //3. 解析して中身をとりだします。
//        //ObjectMapperオブジェクトの宣言
//        ObjectMapper mapper = new ObjectMapper();
//
//        //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
//        JsonNode node = mapper.readTree(script);
//        List<Integer> idList = mapper.convertValue(node.get("items"), ArrayList.class);
//        List<Long> leftIdList = new ArrayList<>();
//        // 削除処理を行う（fct_chkも更新）
//        if (itemService.deleteByItemIdList(idList)) {
//            // 全件無事に削除処理できたら
//            response.setStatus(200);
//            // 成功したレコードのIDを返す
//            leftIdList = itemService.getItemIdListByDlt_flg(idList, true);
//        } else {
//            // 失敗したレコードがある場合
//            response.setStatus(500);
//            //削除に成功したものor失敗したもののitemIdをarrayにして返却したい(itemIdのリストからどちらかを取得)
//            leftIdList = itemService.getItemIdListByDlt_flg(idList, false);
//        }
//        JSONArray jsonArray = new JSONArray();
//        leftIdList.forEach(e -> jsonArray.put(e));
//        JSONObject jo = new JSONObject();
//        jo.put("idList",jsonArray);
//        response.getWriter().write(jo.toString());
//    }
//
//    @PostMapping("/update_pd")
//    public void update_pd(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
//        InputStream stream = request.getInputStream();
//
//        //文字列のバッファを構築
//        StringBuffer sb = new StringBuffer();
//        String line = "";
//        //文字型入力ストリームを作成
//        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
//        //読めなくなるまでwhile文で回す
//        while((line = br.readLine()) != null) {
//            sb.append(line);
//        }
//        String script = sb.toString();
//
//        //3. 解析して中身をとりだします。
//        //ObjectMapperオブジェクトの宣言
//        ObjectMapper mapper = new ObjectMapper();
//
//        //JSON形式をクラスオブジェクトに変換。クラスオブジェクトの中から必要なものだけを取りだす
//        JsonNode node = mapper.readTree(script);
//
//        // Map<ItemId, publicationDate>
//        Map<Long, Date> updateItemMap = new HashMap<>();
//        node.forEach(n -> {
//            try {
//                updateItemMap.put(n.get(0).asLong(),new Date(sdf.parse(n.get(1).asText()).getTime()));
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        });
//        List<Long> leftIdList = new ArrayList<>();
//        if (itemService.updateAllPublicationDate(updateItemMap)) {
//            // 全件無事に更新が完了した場合
//            response.setStatus(200);
//            // 成功したレコードのIDを返す
//            leftIdList.addAll(updateItemMap.keySet());
//        } else {
//            // 更新失敗したレコードがある場合
//            response.setStatus(500);
//            leftIdList = itemService.getItemIdListNotUpdated(updateItemMap);
//        }
//        JSONArray jsonArray = new JSONArray();
//        leftIdList.forEach(e -> jsonArray.put(e));
//        JSONObject jo = new JSONObject();
//        jo.put("idList",jsonArray);
//        response.getWriter().write(jo.toString());
//    }
//}
