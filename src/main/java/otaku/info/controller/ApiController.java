package otaku.info.controller;

import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.dto.FAllDto;
import otaku.info.dto.FIMDto;
import otaku.info.dto.ItemTeamDto;
import otaku.info.dto.PDto;
import otaku.info.entity.*;
import otaku.info.enums.MemberEnum;
import otaku.info.enums.TeamEnum;
import otaku.info.error.MyMessageException;
import otaku.info.form.IMForm;
import otaku.info.form.IMVerForm;
import otaku.info.form.ItemByJsonForm;
import otaku.info.form.PForm;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.utils.DateUtils;

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
    IRelMemService iRelMemService;

    @Autowired
    IMService imService;

    @Autowired
    ImVerService imVerService;

    @Autowired
    IMRelMemService imRelMemService;

    @Autowired
    ProgramService programService;

    @Autowired
    PRelService pRelService;

    @Autowired
    IRelService iRelService;

    @Autowired
    IMRelService imRelService;

    @Autowired
    PageTvService pageTvService;

    @Autowired
    ErrorJsonService errorJsonService;

    @Autowired
    TeamService teamService;

    @Autowired
    DateUtils dateUtils;

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
            List<Long> teamIdList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getId).collect(Collectors.toList());

            List<IRelMem> iRelMemList = new ArrayList<>();
            for (IRel irel : irelList) {
                iRelMemList.addAll(iRelMemService.findByIRelId(irel.getI_rel_id()));
            }

            List<Long> memIdList = Arrays.stream(MemberEnum.values()).map(MemberEnum::getId).collect(Collectors.toList());

            itemTeamDto.setItem(item);
            itemTeamDto.setRelList(irelList);
            itemTeamDto.setTeamIdList(teamIdList);
            itemTeamDto.setRelMemList(iRelMemList);
            itemTeamDto.setMemIdList(memIdList);
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
            List<Long> teamIdList = Arrays.stream(TeamEnum.values()).map(TeamEnum::getId).collect(Collectors.toList());

            List<IRelMem> iRelMemList = new ArrayList<>();
            for (IRel irel : irelList) {
                iRelMemList.addAll(iRelMemService.findByIRelId(irel.getI_rel_id()));
            }

            List<Long> memIdList = Arrays.stream(MemberEnum.values()).map(MemberEnum::getId).collect(Collectors.toList());

            itemTeamDto.setItem(item);
            itemTeamDto.setRelList(irelList);
            itemTeamDto.setTeamIdList(teamIdList);
            itemTeamDto.setRelMemList(iRelMemList);
            itemTeamDto.setMemIdList(memIdList);
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

            List<IMRelMem> imRelMemList = new ArrayList<>();
            for (IMRel rel : imRelList) {
                imRelMemList.addAll(imRelMemService.findByImRelId(rel.getIm_rel_id()));
            }

            // relMemListも入れる
            imDto.setRelMemList(imRelMemList);
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

            List<IRelMem> iRelMemList = new ArrayList<>();
            for (IRel irel : irelList) {
                iRelMemList.addAll(iRelMemService.findByIRelId(irel.getI_rel_id()));
            }

            List<Long> memIdList = new ArrayList<>();
            for (IRelMem relMem : iRelMemList) {
                memIdList.add(relMem.getMember_id());
            }

            itemTeamDto.setItem(item1);
            itemTeamDto.setTeamIdList(teamIdList);
            itemTeamDto.setMemIdList(memIdList);
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
     * まとめて一括IMの登録を行う
     *
     * @param forms
     * @return
     */
    @PostMapping("/im/bundle/new")
    public ResponseEntity<Boolean> newBundleIMyVer(@Valid @RequestBody IMVerForm[] forms) {
        for (IMVerForm imVerForm : forms) {
            ResponseEntity<Boolean> responseEntity = newIMyVer(imVerForm);
            if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                return ResponseEntity.status(500).body(false);
            }
        }
        return ResponseEntity.ok(true);
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
        // relMemListも入れる
        List<IMRelMem> imRelMemList = new ArrayList<>();
        for (IMRel imRel : relList) {
            imRelMemList.addAll(imRelMemService.findByImRelId(imRel.getIm_rel_id()));
        }

        dto.setRelMemList(imRelMemList);
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
            List<IMRelMem> imRelMemList = new ArrayList<>();
            for (IMRel imRel : imRelList) {
                imRelMemList.addAll(imRelMemService.findByImRelId(imRel.getIm_rel_id()));
            }

            dto.setRelMemList(imRelMemList);
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
        im.setBlogNotUpdated(true);
        IM imUpdated = imService.save(im);

        // im自体の更新であればteamIdは影響ないしこのteamIdのimrelを取得する必要もない
//        IMRel rel = imRelService.findByImIdTeamId(imUpdated.getIm_id(), teamId).orElse(null);
        FIMDto dto = new FIMDto();
        dto.setIm(imUpdated);

        List<IMRel> imRelList = imRelService.findByItemMId(imUpdated.getIm_id());
        dto.setRelList(imRelList);

        List<ImVer> imVerList = imVerService.findByImId(imUpdated.getIm_id());
        dto.setVerList(imVerList);

        List<IMRelMem> imRelMemList = new ArrayList<>();
        for (IMRel imRel : imRelList) {
            imRelMemList.addAll(imRelMemService.findByImRelId(imRel.getIm_rel_id()));
        }

        dto.setRelMemList(imRelMemList);
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
    public ResponseEntity<Boolean> delIm(@PathVariable Long id){
        logger.debug("accepted");
        try {
            IM im = imService.findById(id);
            im.setDel_flg(true);
            logger.debug("fin");
            imService.save(im);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
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
    public ResponseEntity<List<PDto>> tvAll(@RequestParam("teamId") Long teamId){
        logger.debug("accepted");
        List<PDto> pDtos = new ArrayList<>();
        List<Program> pList = null;

        // 全チームデータ取得の場合
        if (teamId == null || teamId == 5) {
            pList = programService.findByOnAirDate(dateUtils.getToday());
        } else {
            // チーム指定が適切に入っていればそのチームのを返す
            pList = programService.findbyTeamId(teamId);
        }

        List<Long> teamIdList = teamService.findAllTeam().stream().map(e -> e.getTeam_id()).collect(Collectors.toList());

        for (Program p : pList) {
            PDto pDto = new PDto();
            List<PRel> pRelList = pRelService.getListByProgramId(p.getProgram_id());

            pDto.setProgram(p);
            pDto.setPRelList(pRelList);
            pDto.setTeamIdList(teamIdList);
            pDtos.add(pDto);
        }
        logger.debug("fin");
        return ResponseEntity.ok(pDtos);
    }

    /**
     * 商品のデータを更新する（画像以外）
     *
     * @param id データ更新をする商品のID
     * @param pForm 更新される新しいデータ
     * @return Item
     */
    @PostMapping("/tv/{teamId}/{id}")
    public ResponseEntity<Boolean> upTv(@PathVariable Long teamId, @PathVariable Long id, @Valid @RequestBody PForm pForm){
        logger.debug("accepted");
        try {
            // programの更新
            Program p = pageTvService.findById(id);
            p.absorb(pForm);
            Program p_saved = pageTvService.save(p);

            // prelの更新
            List<PRel> relList = pRelService.getListByProgramId(id);
            for (Long[] inner : pForm.getPrel()) {
                // [p_rel_id, program_id, team_id]の形でデータ入ってる
                for (PRel rel : relList) {
                    if (inner[0].equals(rel.getP_rel_id())) {
                        if (!inner[2].equals(rel.getTeam_id())) {
                            rel.setTeam_id(inner[2]);
                            pRelService.save(rel);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
        logger.debug("fin");
        return ResponseEntity.ok(true);
    }

    /**
     * IDから商品を削除する
     *
     * @param id 削除される商品のID
     */
    @DeleteMapping("/tv/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Boolean> delTv(@PathVariable Long id){
        logger.debug("accepted");
        try {
            Program im = pageTvService.findById(id);
            im.setDel_flg(true);
            pageTvService.save(im);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
        logger.debug("fin");
        return ResponseEntity.ok(true);
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
     * errorJsonIdが連携されなかった場合はそのまま登録します
     *
     * @param id 該当のTeamId
     * @return Item
     */
    @PostMapping("/item/team/{id}")
    public ResponseEntity<Item> postItemTeam(@PathVariable Long id, @Valid @RequestBody ItemByJsonForm form) throws MyMessageException {
        logger.debug("postItemTeam teamId=" + id + " errorJsonId=" + form.getJsonId());

        ErrorJson j = null;

        if (form.getJsonId() != null) {
            // 該当のErrorJsonがしっかり存在する場合のみ処理を進める
            j = errorJsonService.findById(form.getJsonId());
        }

        Item savedItem;

        List<Item> regiItemList = new ArrayList<>();

        if (j != null) {
            regiItemList = itemService.isRegistered(form.getItem().getItem_code());
        }

        // item_codeかぶりがない場合、Itemを新規登録
        if (regiItemList.size() == 0) {
            savedItem = itemService.save(form.getItem());

            if (j != null) {
                // errorJsonも解決済みにする
                j.set_solved(true);
                errorJsonService.save(j);
            }

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
    public ResponseEntity<Boolean> delItem(@PathVariable Long id) {
        logger.debug("accepted");
        try {
            Item item = itemService.findByItemId(id).orElse(null);
            if (item != null) {
                item.setDel_flg(true);
                logger.debug("fin");
                itemService.save(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    /**
     * IM+verを登録します。すでにIMがある場合は更新
     *
     * @return Boolean true: success / false: failed
     */
    @PostMapping("/im")
    public ResponseEntity<Boolean> newIMyVer(@Valid @RequestBody IMVerForm imVerForm) {
        logger.debug("accepted");
        Boolean updFlg = false;

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
                if (im.getIm_id() != null && !im.getIm_id().equals(0L)) {
                    im.setBlogNotUpdated(true);
                    updFlg = true;
                }
                IM savedIm = imService.save(im);
                im = savedIm;
            } else {
                im = imService.findById(imVerForm.getIm_id());
            }

             // imrelの登録を行います(irelは更新しない)
            if (imVerForm.getImrel() != null && imVerForm.getImrel().size() > 0) {
                List<List<Integer>> imrelList = imVerForm.getImrel();

                for (List<Integer> rel : imrelList) {
                    // imの新規登録の場合(=imrelはないはず)と更新の場合(=imrelがすでにあるかもしれない)で処理分岐
                    if (!updFlg) {
                        // IM新規登録の場合
                        imRelService.save(new IMRel(null, im.getIm_id(), Long.valueOf(rel.get(2)), null, null, null));
                    } else {
                        // IM更新の場合
                        // rel.get(3)から、irelデータか(-> imrel新規登録)imrelデータか(->imrel更新or変更なし)かを判別して処理分岐
                        Boolean isImrelData = rel.get(3).equals(1);
                        if (isImrelData) {
                            // すでにimrelあるので、teamId確認して更新必要だったら更新する
                            IMRel imRel = imRelService.findByImRelId(Long.valueOf(rel.get(0)));
                            if (!imRel.getTeam_id().equals(Long.valueOf(rel.get(2)))) {
                                imRel.setTeam_id(Long.valueOf(rel.get(2)));
                                imRelService.save(imRel);
                            }
                        } else {
                            // TODO: 処理早くしたいならここをloopの外に出してあげると良い
                            // irelデータなので、新規でImrelを登録してあげる
                            // すでにimrelが登録されてるかもしれないので取得する
                            List<Long> savedImRelTeamIdList = imRelService.findTeamIdByItemMId(im.getIm_id());
                            // 該当teamの登録がすでにないか一応確認
                            Long teamId = savedImRelTeamIdList.stream().filter(e -> e.equals(Long.valueOf(rel.get(2)))).findFirst().orElse(null);
                            if (teamId == null) {
                                // ないのが確認できたら新規登録
                                imRelService.save(new IMRel(null, im.getIm_id(), Long.valueOf(rel.get(2)), null, null, null));
                            }
                        }
                    }
                }
            }

            // imrelMemの登録を行います(irelMemは更新しない)
            if (imVerForm.getImrelm() != null && imVerForm.getImrelm().size() > 0) {
                List<List<Integer>> imrelmList = imVerForm.getImrelm();

                // IDがすでにあれば更新、なければ新規登録をする
                for (List<Integer> imrelm : imrelmList) {
                    // imの新規登録の場合(=imrelMはないはず)と更新の場合(=imrelMがすでにあるかもしれない)で処理分岐

                    if (!updFlg) {
                        // IM新規登録の場合、imrelmemもないはずなので新規登録
                        Long tmpTeamId = MemberEnum.getTeamIdById(Long.valueOf(imrelm.get(2)));
                        IMRel targetImRel = imRelService.findByImIdTeamId(im.getIm_id(), tmpTeamId).orElse(null);

                        // teamIdが登録されていなかったらimrelを登録する
                        if (targetImRel == null) {
                            targetImRel = imRelService.save(new IMRel(null, im.getIm_id(), tmpTeamId, null, null, null));
                        }

                        imRelMemService.save(new IMRelMem(null, targetImRel.getIm_rel_id(), Long.valueOf(imrelm.get(2)), null, null));
                    } else {
                        // IM更新の場合
                        // imrelm.get(3)から、irelMデータか(-> imrelM新規登録)imrelMデータか(->imrelM更新or変更なし)かを判別して処理分岐
                        Boolean isImrelData = imrelm.get(3).equals(1);

                        if (isImrelData) {
                            // すでにimrelMデータあるのでmemberの更新が必要であれば更新してあげる
                            IMRelMem imRelMem = imRelMemService.findByImRelMemId(Long.valueOf(imrelm.get(0)));
                            if (!imRelMem.getMember_id().equals(Long.valueOf(imrelm.get(2)))) {
                                imRelMem.setMember_id(Long.valueOf(imrelm.get(2)));
                                imRelMemService.save(imRelMem);
                            }
                        } else {
                            // TeamIdがまず登録されてるか確認する
                            Long tmpTeamId = MemberEnum.getTeamIdById(Long.valueOf(imrelm.get(2)));
                            IMRel targetImRel = imRelService.findByImIdTeamId(im.getIm_id(), tmpTeamId).orElse(null);

                            // teamIdが登録されていなかったらimrelを登録する
                            if (targetImRel == null) {
                                targetImRel = imRelService.save(new IMRel(null, im.getIm_id(), tmpTeamId, null, null, null));
                            }

                            // 既存でimrelmemの登録がないか確認
                            IMRelMem imRelMem = imRelMemService.findByImRelIdMemId(targetImRel.getIm_rel_id(), tmpTeamId).orElse(null);
                            if (imRelMem == null) {
                                // imrelの用意ができたのでimrelmemを登録する
                                imRelMemService.save(new IMRelMem(null, targetImRel.getIm_rel_id(), Long.valueOf(imrelm.get(2)), null, null));
                            }
                        }
                    }
                }
            }

            // itemのim_idを登録します
            item.setIm_id(im.getIm_id());
            item.setFct_chk(true);
            itemService.save(item);

            // verがあれば登録します
            List<String[]> verArr = imVerForm.getVers();

            if (verArr.size() > 0) {

                for (String[] ver : verArr) {
                    String verName = ver[1];

                    ImVer newVer = new ImVer();
                    newVer.setVer_name(verName);
                    newVer.setIm_id(im.getIm_id());
                    newVer.setDel_flg(false);
                    imVerService.save(newVer);
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
    @PostMapping("/im/upd")
    public ResponseEntity<Boolean> updIMyVer(@Valid @RequestBody IMVerForm imVerForm) {
        logger.debug("accepted");

        try {
            // IMの更新の場合、更新する（verのみの更新もありえるから）
            IM im = imService.findById(imVerForm.getIm_id());
            Boolean updatedFlg = false;
            if (im == null) {
                return ResponseEntity.ok(false);
            }

            // imの更新
            if (!imVerForm.getTitle().equals(im.getTitle())) {
                im.setTitle(imVerForm.getTitle());
                updatedFlg = true;
            }

            if (!imVerForm.getAmazon_image().equals(im.getAmazon_image())) {
                im.setAmazon_image(imVerForm.getAmazon_image());
                updatedFlg = true;
            }

            // IMの要素が変わってるよフラグがtrueであれば更新してあげます
            if (updatedFlg) {
                im.setBlogNotUpdated(true);
                imService.save(im);
            }

            // verの更新[[id,name][id,name][id,name][id,name][id,name][id,name][id,name]]
            // JsonObjectのverを成形し、DBの値と一致してるか確認する
            // formに入ってきたverオブジェクト
            List<String[]> verArr = imVerForm.getVers();
            // DBに保存されてるverたち
            List<ImVer> verList = imVerService.findByImId(im.getIm_id());

            for (String[] ver : verArr) {
                Boolean existsFlg = true;
                Long verId;
                try {
                    verId = Long.parseLong(ver[0]);
                } catch (Exception e) {
                    // この時点でverIdが取得できなかったら新規のVerってことなので一気にver新規登録に飛びます
                    verId = null;
                    existsFlg = false;
                }

                String verName = ver[1];

                // フォームのImverを1つずつDBのImVerと比較し、更新が必要であれば更新する
                if (existsFlg) {
                    for (ImVer imVer : verList) {

                        // verIdは一致するimVerを見つけた
                        if (verId.equals(imVer.getIm_v_id())) {
                            existsFlg = true;

                            // verNameが一致しない場合、フォームから来たverNameで上書きし保存
                            if (!verName.equals(imVer.getVer_name())) {

                                // verNameが空の場合、論理抹消する。空じゃない場合は名前を更新
                                if (verName.equals("")) {
                                    imVer.setDel_flg(true);
                                } else {
                                    imVer.setVer_name(verName);
                                }
                                imVerService.save(imVer);
                            }
                        } else {
                            existsFlg = false;
                        }

                        // DBのImVerを見つけたらこのforループからは抜けていい
                        if (existsFlg) {
                            break;
                        }
                    }
                }

                // そもそもそのverがDBに存在していなかったら新規登録してあげる
                if (!existsFlg) {
                    ImVer newVer = new ImVer();
                    newVer.setVer_name(verName);
                    newVer.setIm_id(im.getIm_id());
                    newVer.setDel_flg(false);
                    imVerService.save(newVer);
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
    @PostMapping("/im/bundle/upd")
    public ResponseEntity<Boolean> updBundleIMyVer(@Valid @RequestBody IMVerForm[] imVerForms) {
        logger.debug("accepted");

        for (IMVerForm imVerForm : imVerForms) {
            ResponseEntity<Boolean> responseEntity = updIMyVer(imVerForm);
            if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                return ResponseEntity.status(500).body(false);
            }
        }
        return ResponseEntity.ok(true);
    }

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
