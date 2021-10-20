package otaku.info.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.AllArgsConstructor;
import otaku.info.entity.ItemMaster;
import otaku.info.entity.Program;
import otaku.info.form.IMForm;
import otaku.info.form.PForm;
import otaku.info.service.PageItemMasterService;
import otaku.info.service.PageTvService;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiController {

    @Autowired
    PageItemMasterService pageItemMasterService;

    @Autowired
    PageTvService pageTvService;

    /**
     * 商品一覧を返す
     *
     * @return リスト
     */
    @GetMapping("/im")
    public ResponseEntity<List> imAll(@RequestParam("pageSize") Optional<Integer> pageSize, @RequestParam("page") Optional<Integer> page){
        // page size
        int evalPageSize = pageSize.orElse(50);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? 50 : page.get() - 1;
        Page<ItemMaster> imPage = pageItemMasterService.findAll(evalPage, evalPageSize);
        return ResponseEntity.ok(imPage.stream().collect(Collectors.toList()));
    }

    /**
     * IDから商品を取得し返す
     *
     * @param id 取得する商品のID
     * @return Item
     */
    @GetMapping("/im/{id}")
    public ResponseEntity<ItemMaster> getIm(@PathVariable Long id){
        ItemMaster im = pageItemMasterService.findById(id);
        return ResponseEntity.ok(im);
    }

    /**
     * 商品のデータを更新する（画像以外）
     *
     * @param id データ更新をする商品のID
     * @param imForm 更新される新しいデータ
     * @return Item
     */
    @PutMapping("/im/{id}")
    public ResponseEntity<ItemMaster> upIm(@PathVariable Long id, @Valid @RequestBody IMForm imForm){
        ItemMaster im = pageItemMasterService.findById(id);
        im.absorb(imForm);
        ItemMaster item = pageItemMasterService.save(im);
        return ResponseEntity.ok(item);
    }

    /**
     * IDから商品を削除する
     *
     * @param id 削除される商品のID
     */
    @DeleteMapping("/im/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delIm(@PathVariable Long id){
        ItemMaster im = pageItemMasterService.findById(id);
        im.setDel_flg(true);
        pageItemMasterService.save(im);
    }

    /**
     * TV一覧を返す
     *
     * @return リスト
     */
    @GetMapping("/tv")
    public ResponseEntity<List> tvAll(@RequestParam("pageSize") Optional<Integer> pageSize, @RequestParam("page") Optional<Integer> page){
        // page size
        int evalPageSize = pageSize.orElse(50);
        // Evaluate page. If requested parameter is null or less than 0 (to
        // prevent exception), return initial size. Otherwise, return value of
        // param. decreased by 1.
        int evalPage = (page.orElse(0) < 1) ? 50 : page.get() - 1;
        Page<Program> imPage = pageTvService.findAll(evalPage, evalPageSize);
        return ResponseEntity.ok(imPage.stream().collect(Collectors.toList()));
    }

    /**
     * IDから商品を取得し返す
     *
     * @param id 取得する商品のID
     * @return Item
     */
    @GetMapping("/tv/{id}")
    public ResponseEntity<Program> getTv(@PathVariable Long id){
        Program im = pageTvService.findById(id);
        return ResponseEntity.ok(im);
    }

    /**
     * 商品のデータを更新する（画像以外）
     *
     * @param id データ更新をする商品のID
     * @param pForm 更新される新しいデータ
     * @return Item
     */
    @PutMapping("/tv/{id}")
    public ResponseEntity<Program> upTv(@PathVariable Long id, @Valid @RequestBody PForm pForm){
        Program p = pageTvService.findById(id);
        p.absorb(pForm);
        Program item = pageTvService.save(p);
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
        Program im = pageTvService.findById(id);
        im.setDel_flg(true);
        pageTvService.save(im);
    }
}