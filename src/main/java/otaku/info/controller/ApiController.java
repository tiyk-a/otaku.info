package otaku.info.controller;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.dto.FAllDto;
import otaku.info.dto.FIMDto;
import otaku.info.dto.ItemTeamDto;
import otaku.info.entity.*;
import otaku.info.error.MyMessageException;
import otaku.info.form.IMForm;
import otaku.info.form.IMVerForm;
import otaku.info.form.ItemByJsonForm;
import otaku.info.form.PForm;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("ApiController");

    @Autowired
    BlogController blogController;

    @Autowired
    ItemService itemService;

    @Autowired
    IMService imService;

    @Autowired
    ImVerService imVerService;

    @Autowired
    ProgramService programService;

    @Autowired
    IRelService iRelService;

    @Autowired
    IMRelService imRelService;

    @Autowired
    PageTvService pageTvService;

    @Autowired
    ErrorJsonService errorJsonService;

    /**
     * トップ画面用のデータ取得メソッド
     *
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity<FAllDto> getAll(){
        logger.debug("accepted");
        FAllDto dto = new FAllDto();

        List<ItemTeamDto> itemTeamDtoList = new ArrayList<>();

        // IMがない未来のItemを取得する（どこかのチームで登録されてれば取得しない）
        List<Item> itemList = itemService.findFutureNotDeletedNoIM();
        for (Item item : itemList) {
            ItemTeamDto itemTeamDto = new ItemTeamDto();
            List<IRel> irelList = iRelService.findByItemId(item.getItem_id());
            List<Long> teamIdList = new ArrayList<>();
            for (IRel irel : irelList) {
                if (!teamIdList.contains(irel.getTeam_id())) {
                    teamIdList.add(irel.getTeam_id());
                }
            }
            itemTeamDto.setItem(item);
            itemTeamDto.setTeamIdList(teamIdList);
            itemTeamDtoList.add(itemTeamDto);
        }

        List<ErrorJson> errorJsonList = errorJsonService.isNotSolved();
        dto.setI(itemTeamDtoList);
        dto.setErrJ(errorJsonList);
        logger.debug("fin");
        return ResponseEntity.ok(dto);
    }

    /**
     * 各グループ画面用のデータ取得メソッド
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<FAllDto> getTop(@PathVariable Long id){
        logger.debug("accepted");

        // IMがない未来のItemを取得する（他チームで登録されてれば取得しない）
        List<Item> itemList = itemService.findByTeamIdFutureNotDeletedNoIM(id);

        // 未来のIMを取得する
        List<IM> imList = imService.findByTeamIdFuture(id);
        List<Item> itemList1 = itemService.findByTeamIdFutureNotDeletedWIM(id);
        List<ErrorJson> errorJsonList = errorJsonService.findByTeamIdNotSolved(id);

        logger.debug("accepted");
        FAllDto dto = new FAllDto();

        List<ItemTeamDto> itemTeamDtoList = new ArrayList<>();

        // IMのないItemリスト
        for (Item item : itemList) {
            ItemTeamDto itemTeamDto = new ItemTeamDto();
            List<IRel> irelList = iRelService.findByItemId(item.getItem_id());
            List<Long> teamIdList = new ArrayList<>();
            for (IRel irel : irelList) {
                if (!teamIdList.contains(irel.getTeam_id())) {
                    teamIdList.add(irel.getTeam_id());
                }
            }
            itemTeamDto.setItem(item);
            itemTeamDto.setTeamIdList(teamIdList);
            itemTeamDtoList.add(itemTeamDto);
        }

        // IMリスト
        List<FIMDto> fimDtoList = new ArrayList<>();
        for (IM im : imList) {
            // TODO: modify
            FIMDto imDto = new FIMDto();
            imDto.setIm(im);

            // verも追加
            List<ImVer> verList = imVerService.findByImId(im.getIm_id());
            imDto.setVerList(verList);

            // relListも入れる
            List<IMRel> imRelList = imRelService.findByItemMId(im.getIm_id());
            imDto.setRelList(imRelList);
            fimDtoList.add(imDto);
        }

        // IMのあるItemリスト
        List<ItemTeamDto> itemTeamDtoList1 = new ArrayList<>();
        for (Item item1 : itemList1) {
            ItemTeamDto itemTeamDto = new ItemTeamDto();
            List<IRel> irelList = iRelService.findByItemId(item1.getItem_id());
            List<Long> teamIdList = new ArrayList<>();
            for (IRel irel : irelList) {
                if (!teamIdList.contains(irel.getTeam_id())) {
                    teamIdList.add(irel.getTeam_id());
                }
            }
            itemTeamDto.setItem(item1);
            itemTeamDto.setTeamIdList(teamIdList);
            itemTeamDtoList1.add(itemTeamDto);
        }

        dto.setI(itemTeamDtoList);
        dto.setIm(fimDtoList);
        dto.setIim(itemTeamDtoList1);
        dto.setErrJ(errorJsonList);
        logger.debug("fin");
        return ResponseEntity.ok(dto);
    }

    /**
     * IDから商品を取得し返す
     *
     * @param id 取得する商品のID
     * @return Item
     */
    @GetMapping("/im/{id}")
    public ResponseEntity<FIMDto> getIm(@PathVariable Long teamId, @PathVariable Long id){
        logger.debug("accepted");
        IM im = imService.findById(id);
        IMRel rel = imRelService.findByImIdTeamId(im.getIm_id(), teamId).orElse(null);
        List<IMRel> relList = new ArrayList<>();
        relList.add(rel);
        List<ImVer> imVerList = imVerService.findByImId(im.getIm_id());

        FIMDto dto = new FIMDto();

        dto.setIm(im);
        dto.setRelList(relList);
        dto.setVerList(imVerList);
        logger.debug("fin");
        return ResponseEntity.ok(dto);
    }

    /**
     * 指定Teamidの商品を未来発売日順に取得し返す、削除されていない商品のみ。
     *
     * @param id 取得するTeamId
     * @return Item
     */
    @GetMapping("/im/team/{id}")
    public ResponseEntity<List<FIMDto>> getTeam(@PathVariable Long id){
        logger.debug("getTeam teamId=" + id);
        List<IM> imList = imService.findByTeamIdNotDeleted(id);
        List<FIMDto> dtoList = new ArrayList<>();

        for (IM im : imList) {
            FIMDto dto = new FIMDto();
            dto.setIm(im);

            List<IMRel> imRelList = imRelService.findByItemMId(im.getIm_id());
            dto.setRelList(imRelList);

            List<ImVer> imVerList = imVerService.findByImId(im.getIm_id());
            dto.setVerList(imVerList);
            dtoList.add(dto);
        }
        logger.debug("fin");
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 商品のデータを更新する
     * IMの更新なので、
     *
     * @param id データ更新をする商品のID
     * @param imForm 更新される新しいデータ
     * @return Item
     */
    @PostMapping("/im/{teamId}/{id}")
    public ResponseEntity<FIMDto> upIm(@PathVariable Long teamId, @PathVariable Long id, @Valid @RequestBody IMForm imForm){
        logger.debug("accepted");
        IM im = imService.findById(id);
        im.absorb(imForm);
        IM imUpdated = imService.save(im);

        // im自体の更新であればteamIdは影響ないしこのteamIdのimrelを取得する必要もない
//        IMRel rel = imRelService.findByImIdTeamId(imUpdated.getIm_id(), teamId).orElse(null);
        FIMDto dto = new FIMDto();
        dto.setIm(imUpdated);

        List<IMRel> imRelList = imRelService.findByItemMId(imUpdated.getIm_id());
        dto.setRelList(imRelList);

        List<ImVer> imVerList = imVerService.findByImId(imUpdated.getIm_id());
        dto.setVerList(imVerList);

        logger.debug("fin");
        return ResponseEntity.ok(dto);
    }

    /**
     * IDから商品を削除する
     *
     * @param id 削除される商品のID
     */
    @DeleteMapping("/im/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delIm(@PathVariable Long id){
        logger.debug("accepted");
        IM im = imService.findById(id);
        im.setDel_flg(true);
        logger.debug("fin");
        imService.save(im);
    }

    /**
     * 指定のIMをブログ投稿します
     *
     */
    @GetMapping("/im/blog")
    public ResponseEntity<Boolean> upImBlog(@RequestParam("imId") Long imId, @RequestParam("team") Long team) throws InterruptedException {
        logger.debug("accepted");
        IM im = imService.findById(imId);
        List<IM> list = new ArrayList<>();
        list.add(im);
        logger.debug("fin");
        if (im != null) {
            blogController.postOrUpdate(list, team);
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    /**
     * TV一覧を返す
     *
     * @return リスト
     */
    @GetMapping("/tv")
    public ResponseEntity<List> tvAll(@RequestParam("pageSize") Optional<Integer> pageSize, @RequestParam("page") Optional<Integer> page){
        logger.debug("accepted");
        // page size
        int evalPageSize = pageSize.orElse(50);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? 50 : page.get() - 1;
        Page<Program> imPage = pageTvService.findAll(evalPage, evalPageSize);
        logger.debug("fin");
        return ResponseEntity.ok(imPage.stream().collect(Collectors.toList()));
    }

    @GetMapping("/tv/team/{id}")
    public ResponseEntity<List<Program>> getTvTeam(@PathVariable Long id){
        logger.debug("accepted");
        List<Program> pList = programService.findbyTeamId(id);
        logger.debug("fin");
        return ResponseEntity.ok(pList);
    }

    /**
     * IDから商品を取得し返す
     *
     * @param id 取得する商品のID
     * @return Item
     */
    @GetMapping("/tv/{id}")
    public ResponseEntity<Program> getTv(@PathVariable Long id){
        logger.debug("accepted");
        Program im = pageTvService.findById(id);
        logger.debug("fin");
        return ResponseEntity.ok(im);
    }

    /**
     * 商品のデータを更新する（画像以外）
     *
     * @param id データ更新をする商品のID
     * @param pForm 更新される新しいデータ
     * @return Item
     */
    @PostMapping("/tv/{teamId}/{id}")
    public ResponseEntity<Program> upTv(@PathVariable Long teamId, @PathVariable Long id, @Valid @RequestBody PForm pForm){
        logger.debug("accepted");
        Program p = pageTvService.findById(id);
        p.absorb(pForm);
        Program item = pageTvService.save(p);
        logger.debug("fin");
        return ResponseEntity.ok(item);
    }

    /**
     * IDから商品を削除する
     *
     * @param id 削除される商品のID
     */
    @DeleteMapping("/tv/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delTv(@PathVariable Long id){
        logger.debug("accepted");
        Program im = pageTvService.findById(id);
        im.setDel_flg(true);
        logger.debug("fin");
        pageTvService.save(im);
    }

    /**
     * 指定Teamidの商品を未来発売日順に取得し返す、削除されていない商品のみ。
     *
     * @param id 取得するTeamId
     * @return Item
     */
    @GetMapping("/item/team/{id}")
    public ResponseEntity<List<Item>> getItemTeam(@PathVariable Long id) {
        logger.debug("getItemTeam teamId=" + id);
        List<Item> imList = itemService.findByTeamIdNotDeleted(id);
        logger.debug("fin");
        return ResponseEntity.ok(imList);
    }

    /**
     * 指定商品(Item)を新規登録します。
     * Itemとi_relを作ります
     * 無事に登録できた場合はそのteamIdのerrorJsonとItem(未来)リストを取得し直して返却します
     *
     * @param id 該当のTeamId
     * @return Item
     */
    @PostMapping("/item/team/{id}")
    public ResponseEntity<Item> postItemTeam(@PathVariable Long id, @Valid @RequestBody ItemByJsonForm form) throws MyMessageException {
        logger.debug("postItemTeam teamId=" + id + " errorJsonId=" + form.getJsonId());

        // 該当のErrorJsonがしっかり存在する場合のみ処理を進める
        ErrorJson j = errorJsonService.findById(form.getJsonId());

        Item savedItem;

        if (j != null) {
            List<Item> regiItemList = itemService.isRegistered(form.getItem().getItem_code());
            if (regiItemList.size() == 0) {
                // item_codeかぶりがない場合、Itemを新規登録
                savedItem = itemService.save(form.getItem());

                // errorJsonも解決済みにする
                j.set_solved(true);
                errorJsonService.save(j);

                // Itemは今新規登録したため、該当のirelは絶対ないはず。基本的には。なのでチェックなしでそのままirelの登録は入ってよし
                IRel rel = new IRel();
                rel.setTeam_id(id);
                rel.setItem_id(savedItem.getItem_id());
                IRel savedRel = iRelService.save(rel);
            } else {
                String siteIdList = regiItemList.stream().map(Item::getItem_code).collect(Collectors.joining(","));
                // すでにそのitem_codeの商品登録がある場合（楽天かyahooかどっちかにそのitem_codeの商品がある）、本当に登録するかを確認するようにメッセージを返却する
                throw new MyMessageException("そのitem_codeの商品登録がすでにある", "item_code=" + form.getItem().getItem_code(), "site_id=" + siteIdList);
            }
        } else {
            // リクエストで送られてきたerrorJsonのIDがなんかDBに存在しない場合、jsonそんなのないよって返してあげる
            throw new MyMessageException("errorJsonのIDがDBにないです", "errorJsonId", form.getJsonId().toString());
        }

        logger.debug("fin");
        return ResponseEntity.ok(savedItem);
    }

    /**
     * 商品のデータを更新する
     *
     * @param id データ更新をする商品のID
     * @param form 更新される新しいデータ
     * @return Item
     */
    @PostMapping("/item/{teamId}/{id}")
    public ResponseEntity<Item> upItem(@PathVariable Long teamId, @PathVariable Long id, @Valid @RequestBody Item form) {
        logger.debug("accepted");
        Item item = itemService.findByItemId(id).orElse(new Item());
        item.absorb(form);
        Item savedItem = itemService.save(item);
        logger.debug("fin");
        return ResponseEntity.ok(savedItem);
    }

    /**
     * IDから商品を削除する
     *
     * @param id 削除される商品のID
     */
    @DeleteMapping("/item/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delItem(@PathVariable Long id) {
        logger.debug("accepted");
        Item item = itemService.findByItemId(id).orElse(null);
        if (item != null) {
            item.setDel_flg(true);
            logger.debug("fin");
            itemService.save(item);
        }
    }

    /**
     * IM+verを登録します
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/im")
    public ResponseEntity<Boolean> newIMyVer(@Valid @RequestBody IMVerForm imVerForm) {
        logger.debug("accepted");

        try {
            IM im = null;
            Item item = itemService.findByItemId(imVerForm.getItem_id()).orElse(null);

            if (item == null) {
                return ResponseEntity.ok(false);
            }

            // im_idが入っていたらverだけ追加処理処理、入っていなかったらim新規登録とあればver追加処理、と判断（ここではimのタイトル変更などはできない）
            if (imVerForm.getIm_id() == null || imVerForm.getIm_id() == 0) {

                // 対象のItemが見つからなかったら処理しません。見つかったら処理する。
                im = new IM();

                // 上書きしてくれるから新規登録も更新もこれだけでいけるはず
                BeanUtils.copyProperties(imVerForm, im);
                IM savedIm = imService.save(im);
                im = savedIm;
            } else {
                im = imService.findById(imVerForm.getIm_id());
            }

            // im_relの登録を行います（指示を出したteam以外のteamについても、itemに紐づいてたらim_rel登録します）
            // TODO: 万一、すでに該当のrelがある場合存在チェックしてないから被って問題になるけど、多分大丈夫。
            List<Long> teamIdList = iRelService.findTeamIdByItemId(item.getItem_id());
            List<Long> existTeamIdList = imRelService.findTeamIdByItemMId(im.getIm_id());

            for (Long teamId : teamIdList) {
                if (!existTeamIdList.contains(teamId)) {
                    IMRel newRel = new IMRel();
                    newRel.setTeam_id(teamId);
                    newRel.setIm_id(im.getIm_id());
                    imRelService.save(newRel);
                }
            }

            // itemのim_idを登録します
            item.setIm_id(im.getIm_id());
            item.setFct_chk(true);
            itemService.save(item);

            // verがあれば登録します
            if (imVerForm.getVerArr().length > 0) {
                for (int i=0;i<imVerForm.getVerArr().length;i++) {
                    String verName = imVerForm.getVerArr()[i];
                    boolean verExists = imVerService.existtVerNameImId(verName, im.getIm_id());
                    if (!verExists) {
                        ImVer ver = new ImVer();
                        ver.setVer_name(verName);
                        ver.setIm_id(im.getIm_id());
                        imVerService.save(ver);
                    }
                }
            }

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    /**
     * TODO:IM+verを更新します。画面から、verのver_name & ver_idをセットにして渡せれば実現可能
     *
     * @return Boolean true: success / false: failed
     */
//    @PostMapping("/im/{imId}")
//    public ResponseEntity<Boolean> updIMyVer(@PathVariable Long imId, @Valid @RequestBody IMVerForm imVerForm) {
//        logger.debug("accepted");
//
//        try {
//            IM im = imService.findById(imId);
//            if (im == null) {
//                return ResponseEntity.ok(false);
//            }
//
//            // imの更新
//            if (!imVerForm.getTitle().equals(im.getTitle())) {
//                im.setTitle(imVerForm.getTitle());
//                imService.save(im);
//            }
//
//            // verの更新
//            if (imVerForm.getVerArr().length > 0) {
//                List<ImVer> verList = imVerService.findByImId(im.getIm_id());
//                for (String ver : imVerForm.getVerArr()) {
//                    ImVer originVer = verList.stream().filter(e -> e.getVer_name().equals(ver)).findFirst().orElse(null);
//                    if (originVer != null) {
//                        if (!originVer.getVer_name().equals(ver.getVer_name())) {
//                            originVer.setVer_name(ver.getVer_name());
//                            imVerService.save(originVer);
//                        }
//                    } else {
//                        ImVer newVer = new ImVer();
//                        newVer.setVer_name(ver.getVer_name());
//                        newVer.setIm_id(imVerForm.getIm_id());
//                        imVerService.save(newVer);
//                    }
//                }
//            }
//            logger.debug("fin");
//            return ResponseEntity.ok(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.ok(false);
//        }
//    }

    /**
     * Itemにim_idを追加してfct_chkを更新します（既存imある場合ですね）
     *
     * @return Boolean true: success / false: failed
     */
    @GetMapping("/im/chk")
    public ResponseEntity<Boolean> chkItem(@RequestParam("itemId") Long itemId, @RequestParam("imId") Long imId, @RequestParam("teamId") Long teamId) {
        logger.debug("accepted");

        try {
            Item item = itemService.findByItemId(itemId).orElse(null);
            IM im = imService.findById(imId);

            if (item == null || im == null) {
                return ResponseEntity.ok(false);
            }

            item.setIm_id(imId);
            item.setFct_chk(true);
            itemService.save(item);

            // imrelがない場合は作成します
            IMRel rel = imRelService.findByImIdTeamId(imId, teamId).orElse(null);
            if (rel == null) {
                IMRel newRel = new IMRel();
                newRel.setTeam_id(teamId);
                newRel.setIm_id(imId);
                imRelService.save(newRel);
            }

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/im/search")
    public ResponseEntity<List<IM>> searchOtherTeamIM(@RequestParam("key") String key, @RequestParam("excludeTeamId") Long excludeTeamId) {
        if (key.equals("") ) {
            // TODO: excludeTeamIdがnullやundefinedの時の対処がないよ
            return ResponseEntity.ok(new ArrayList<>());
        }

        return ResponseEntity.ok(imService.findByKeyExcludeTeamId(key, excludeTeamId));
    }
}
