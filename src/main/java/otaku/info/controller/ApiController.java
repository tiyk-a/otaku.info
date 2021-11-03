package otaku.info.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import otaku.info.dto.FIMDto;
import otaku.info.dto.FTopDTO;
import otaku.info.entity.*;
import otaku.info.form.IMForm;
import otaku.info.form.IMVerForm;
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

    /**
     * トップ画面用のデータ取得メソッド
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<FTopDTO> getTop(@PathVariable Long id){
        logger.debug("accepted");
        FTopDTO dto = new FTopDTO();

        List<Item> itemList = itemService.findByTeamIdFutureNotDeletedNoIM(id);
        List<IM> imList = imService.findByTeamIdFuture(id);
        List<Item> itemList1 = itemService.findByTeamIdFutureNotDeletedWIM(id);
        List<FIMDto> fimDtoList = new ArrayList<>();

        for (IM im : imList) {
            // TODO: modify
            IMRel rel = imRelService.findByImIdTeamId(im.getIm_id(), id);
            FIMDto imDto = new FIMDto();
            BeanUtils.copyProperties(im, imDto);
            if (rel != null && rel.getWp_id() != null) {
                imDto.setWp_id(rel.getWp_id());
            }

            // verも追加
            List<ImVer> verList = imVerService.findByImId(im.getIm_id());
            imDto.setVerList(verList);
            fimDtoList.add(imDto);
        }

        dto.setI(itemList);
        dto.setIm(fimDtoList);
        dto.setIim(itemList1);
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
        IMRel rel = imRelService.findByImIdTeamId(im.getIm_id(), teamId);
        FIMDto dto = new FIMDto();
        BeanUtils.copyProperties(im, dto);
        if (rel != null && rel.getWp_id() != null) {
            dto.setWp_id(rel.getWp_id());
        }
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
            BeanUtils.copyProperties(im, dto);
            IMRel rel = imRelService.findByImIdTeamId(im.getIm_id(), id);
            if (rel != null && rel.getWp_id() != null) {
                dto.setWp_id(rel.getWp_id());
            }
            dtoList.add(dto);
        }
        logger.debug("fin");
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 商品のデータを更新する
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
        IM item = imService.save(im);
        IMRel rel = imRelService.findByImIdTeamId(item.getIm_id(), teamId);
        FIMDto dto = new FIMDto();
        BeanUtils.copyProperties(item, dto);
        dto.setWp_id(rel.getWp_id());
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

                // im_relの登録を行います
                // TODO: 万一、すでに該当のrelがある場合存在チェックしてないから被って問題になるけど、多分大丈夫。
                if (savedIm != null) {
                    IMRel rel = new IMRel();
                    rel.setTeam_id(imVerForm.getTeamId());
                    rel.setIm_id(savedIm.getIm_id());
                    imRelService.save(rel);
                }
                im = savedIm;
            } else {
                im = imService.findById(imVerForm.getIm_id());
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
    public ResponseEntity<Boolean> chkItem(@RequestParam("itemId") Long itemId, @RequestParam("imId") Long imId) {
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

            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }
}
