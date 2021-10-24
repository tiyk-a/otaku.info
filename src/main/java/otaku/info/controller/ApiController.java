package otaku.info.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.dto.FIMDto;
import otaku.info.entity.IMRel;
import otaku.info.entity.Item;
import otaku.info.entity.ItemMaster;
import otaku.info.entity.Program;
import otaku.info.form.IMForm;
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
    ProgramService programService;

    @Autowired
    ItemMasterService itemMasterService;

    @Autowired
    IMRelService imRelService;

    @Autowired
    PageTvService pageTvService;

    /**
     * IDから商品を取得し返す
     *
     * @param id 取得する商品のID
     * @return Item
     */
    @GetMapping("/im/{id}")
    public ResponseEntity<FIMDto> getIm(@PathVariable Long teamId, @PathVariable Long id){
        logger.debug("accepted");
        ItemMaster im = itemMasterService.findById(id);
        IMRel rel = imRelService.findByImIdTeamId(im.getItem_m_id(), teamId);
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
        List<ItemMaster> imList = itemMasterService.findByTeamIdNotDeleted(id);
        List<FIMDto> dtoList = new ArrayList<>();

        for (ItemMaster im : imList) {
            FIMDto dto = new FIMDto();
            BeanUtils.copyProperties(im, dto);
            IMRel rel = imRelService.findByImIdTeamId(im.getItem_m_id(), id);
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
        ItemMaster im = itemMasterService.findById(id);
        im.absorb(imForm);
        ItemMaster item = itemMasterService.save(im);
        IMRel rel = imRelService.findByImIdTeamId(item.getItem_m_id(), teamId);
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
        ItemMaster im = itemMasterService.findById(id);
        im.setDel_flg(true);
        logger.debug("fin");
        itemMasterService.save(im);
    }

    @GetMapping("/im/blog")
    public ResponseEntity<Boolean> upImBlog(@RequestParam("imId") Integer imId, @RequestParam("team") Integer team) throws JSONException, InterruptedException {
        logger.debug("accepted");
        ItemMaster im = itemMasterService.findById((long) imId);
        List<ItemMaster> list = new ArrayList<>();
        list.add(im);
        logger.debug("fin");
        if (im != null) {
            blogController.postOrUpdate(list, (long) team);
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/im/merge")
    public ResponseEntity<Boolean> mergeIm(@RequestParam("ord") Integer ord, @RequestParam("into") Integer into) throws JSONException, InterruptedException {
        logger.debug("accepted");
        try {
            boolean existsIntoIm = itemMasterService.exists((long) into);
            if (existsIntoIm) {
                ItemMaster im = itemMasterService.findById((long) ord);
                im.setDel_flg(true);
                im.setMerge_im_id((long) into);
                itemMasterService.save(im);

                List<Item> itemList = itemService.findByMasterId(im.getItem_m_id());
                if (itemList.size() > 0) {
                    itemList.forEach(e -> e.setItem_m_id((long) into));
                    itemService.saveAll(itemList);
                }
            }
            logger.debug("fin");
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("fin");
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
}
