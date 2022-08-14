package otaku.info.controller;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import otaku.info.entity.*;
import otaku.info.service.*;
import otaku.info.setting.Log4jUtils;
import otaku.info.setting.Setting;
import otaku.info.utils.JsonUtils;
import otaku.info.utils.StringUtilsMine;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class TmpController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("TmpController");

    @Autowired
    Setting setting;

    @Autowired
    ItemService itemService;

    @Autowired
    IMService imService;

    @Autowired
    ProgramService programService;

    @Autowired
    PMService pmService;

    @Autowired
    RakutenController rakutenController;

    @Autowired
    BlogController blogController;

    @Autowired
    BlogPostService blogPostService;

//    public void insertBlogPost() {
//        Boolean flg = true;
//        while (flg) {
//            List<IM> imList = imService.tmpMethod3();
//            if (imList.size() > 0) {
//                for (IM im : imList) {
//                    List<BlogPost> blogPostList = new ArrayList<>();
//
//                    List<Long> teamIdList = StringUtilsMine.stringToLongList(im.getTeamArr());
//                    List<Long> blogEnumList = new ArrayList<>();
//
//                    for (Long teamId : teamIdList) {
//                        TeamEnum teamEnum = TeamEnum.get(teamId);
//                        if (teamEnum != null) {
//                            if (!blogEnumList.contains(teamEnum.getBlogEnumId())) {
//                                blogEnumList.add(teamEnum.getBlogEnumId());
//                            }
//                        }
//
//                    }
//                    for (Long blogEnumId : blogEnumList) {
//                        BlogPost blogPost = blogPostService.findByImIdBlogEnumId(im.getIm_id(), blogEnumId);
//                        if (blogPost.getBlog_post_id() == null) {
//                            List<IMRel> relList = imRelService.findByItemMId(im.getIm_id());
//                            List<IMRel> relSelectedList = relList.stream().filter(e -> TeamEnum.get(e.getTeam_id()).getBlogEnumId().equals(blogEnumId)).collect(Collectors.toList());
//                            blogPost.setBlog_enum_id(blogEnumId);
//                            if (relSelectedList.size() > 0) {
//                                blogPost.setWp_id(relSelectedList.get(0).getWp_id());
//                                blogPost.setInner_image(relSelectedList.get(0).getInner_image());
//                                blogPost.setWp_eye_catch_id(relSelectedList.get(0).getWp_eye_catch_id());
//                            }
//                            blogPost.setIm_id(im.getIm_id());
//                            List<Long> tmp = teamIdList.stream().filter(e -> TeamEnum.get(e).getBlogEnumId().equals(blogEnumId)).collect(Collectors.toList());
//                            blogPost.setTeam_arr(StringUtilsMine.longListToString(tmp));
//                            List<Long> tmp2 = StringUtilsMine.stringToLongList(im.getMemArr()).stream().filter(e -> TeamEnum.get(MemberEnum.get(e).getTeamId()).getBlogEnumId().equals(blogEnumId)).collect(Collectors.toList());
//                            blogPost.setMem_arr(StringUtilsMine.longListToString(tmp2));
//                            blogPostList.add(blogPost);
//                        }
//                    }
//
//                    if (blogPostList.size() > 0) {
//                        blogPostService.saveAll(blogPostList);
//                    }
//                }
//            } else {
//                flg = false;
//            }
//        }
//    }

    /**
     * relテーブルの削除処理2022のデータ
     * 各マスターテーブルにteam/memberデータをinsert
     */
//    public void insertTeamMem() {
//
//        System.out.println("Item start!");
//        // item
//        Boolean continueItemFlg = true;
//        int count = 0;
//        while (continueItemFlg) {
//            count ++;
//            List<Item> itemList = itemService.tmpMethod();
//            if (itemList.size() > 0) {
//                List<Item> updateList = new ArrayList<>();
//                for (Item item : itemList) {
//                    // **teamの処理
//                    String teamArr = "";
//                    // **memberの処理
//                    String memArr = "";
//
//                    List<IRel> iRelList = iRelService.findByItemId(item.getItem_id());
//                    for (IRel iRel : iRelList) {
//                        teamArr = StringUtilsMine.addToStringArr(teamArr, iRel.getTeam_id());
//
//                        List<IRelMem> iRelMemList = iRelMemService.findByIRelId(iRel.getI_rel_id());
//                        for (IRelMem iRelMem : iRelMemList) {
//                            memArr = StringUtilsMine.addToStringArr(memArr, iRelMem.getMember_id());
//                        }
//                    }
//                    item.setTeamArr(teamArr);
//                    item.setMemArr(memArr);
//                    updateList.add(item);
//                }
//                if (updateList.size() > 0) {
//                    itemService.saveAll(updateList);
//                }
//                System.out.println("Item: " + count);
//            } else {
//                continueItemFlg = false;
//            }
//            System.out.println("Item end!");
//        }
//
//        System.out.println("IM start!");
//        // im
//        Boolean continueImFlg = true;
//        int count1 = 0;
//        while (continueImFlg) {
//            count1 ++;
//            List<IM> imList = imService.tmpMethod();
//            if (imList.size() > 0) {
//                List<IM> updateList = new ArrayList<>();
//                for (IM im : imList) {
//                    // **teamの処理
//                    String teamArr = "";
//                    // **memberの処理
//                    String memArr = "";
//
//                    List<IMRel> imRelList = imRelService.findByItemMId(im.getIm_id());
//                    for (IMRel imRel : imRelList) {
//                        teamArr = StringUtilsMine.addToStringArr(teamArr, imRel.getTeam_id());
//
//                        List<IMRelMem> imRelMemList = imRelMemService.findByImRelId(imRel.getIm_rel_id());
//                        for (IMRelMem imRelMem : imRelMemList) {
//                            memArr = StringUtilsMine.addToStringArr(memArr, imRelMem.getMember_id());
//                        }
//                    }
//                    im.setTeamArr(teamArr);
//                    im.setMemArr(memArr);
//                    updateList.add(im);
//                }
//                if (updateList.size() > 0) {
//                    imService.saveAll(updateList);
//                }
//                System.out.println("IM: " + count1);
//            } else {
//                continueImFlg = false;
//            }
//            System.out.println("IM end!");
//        }
//
//        System.out.println("Program start!");
//        //program
//        Boolean continuePFlg = true;
//        int count2 = 0;
//        while (continuePFlg) {
//            count2 ++;
//            List<Program> programList = programService.tmpMethod();
//            if (programList.size() > 0) {
//                List<Program> updateList = new ArrayList<>();
//                for (Program program : programList) {
//                    // **teamの処理
//                    String teamArr = "";
//                    // **memberの処理
//                    String memArr = "";
//
//                    List<PRel> pRelList = pRelService.tmpMethod(program.getProgram_id());
//                    for (PRel pRel : pRelList) {
//                        teamArr = StringUtilsMine.addToStringArr(teamArr, pRel.getTeam_id());
//
//                        List<PRelMem> pRelMemList = pRelMemService.findByPRelId(pRel.getP_rel_id());
//                        for (PRelMem pRelMem : pRelMemList) {
//                            memArr = StringUtilsMine.addToStringArr(memArr, pRelMem.getMember_id());
//                        }
//                    }
//
//                    program.setTeamArr(teamArr);
//                    program.setMemArr(memArr);
//                    updateList.add(program);
//                }
//                if (updateList.size() > 0) {
//                    programService.saveAll(updateList);
//                }
//                System.out.println("Program: " + count2);
//            } else {
//                continuePFlg = false;
//            }
//            System.out.println("Program end!");
//        }
//
//        System.out.println("PM start!");
//        //pm
//        Boolean continuePmFlg = true;
//        int count3 = 0;
//        while (continuePmFlg) {
//            count3 ++;
//            List<PM> pmList = pmService.tmpMethod();
//            if (pmList.size() > 0) {
//                List<PM> updateList = new ArrayList<>();
//                for (PM pm : pmList) {
//                    // **teamの処理
//                    String teamArr = "";
//                    // **memberの処理
//                    String memArr = "";
//
//                    List<PMRel> pmRelList = pmRelService.findByPmIdDelFlg(pm.getPm_id(), false);
//                    for (PMRel pmRel : pmRelList) {
//                        teamArr = StringUtilsMine.addToStringArr(teamArr, pmRel.getTeam_id());
//
//                        List<PMRelMem> pmRelMems = pmRelMemService.findByPRelIdDelFlg(pmRel.getPm_rel_id(), null);
//                        for (PMRelMem pmRelMem : pmRelMems) {
//                            memArr = StringUtilsMine.addToStringArr(memArr, pmRelMem.getMember_id());
//                        }
//                    }
//                    pm.setTeamArr(teamArr);
//                    pm.setMemArr(memArr);
//                    updateList.add(pm);
//                }
//                if (updateList.size() > 0) {
//                    pmService.saveAll(updateList);
//                }
//                System.out.println("PM: " + count3);
//            } else {
//                continuePmFlg = false;
//            }
//            System.out.println("PM end!");
//        }
//        System.out.println("ALL END!");
//    }

    /**
     * relテーブルの削除処理~2022のデータ
     * 各マスターテーブルにteam/memberデータをinsert
     */
//    public void insertTeamMemOld() {
//
//        System.out.println("Item start!");
//        // item
//        Boolean continueItemFlg = true;
//        int count = 0;
//        while (continueItemFlg) {
//            count ++;
//            List<Item> itemList = itemService.tmpMethod2();
//            if (itemList.size() > 0) {
//                List<Item> updateList = new ArrayList<>();
//                for (Item item : itemList) {
//                    // **teamの処理
//                    String teamArr = "";
//                    // **memberの処理
//                    String memArr = "";
//
//                    List<IRel> iRelList = iRelService.findByItemId(item.getItem_id());
//                    for (IRel iRel : iRelList) {
//                        teamArr = StringUtilsMine.addToStringArr(teamArr, iRel.getTeam_id());
//
//                        List<IRelMem> iRelMemList = iRelMemService.findByIRelId(iRel.getI_rel_id());
//                        for (IRelMem iRelMem : iRelMemList) {
//                            memArr = StringUtilsMine.addToStringArr(memArr, iRelMem.getMember_id());
//                        }
//                    }
//                    item.setTeamArr(teamArr);
//                    item.setMemArr(memArr);
//                    updateList.add(item);
//                }
//                if (updateList.size() > 0) {
//                    itemService.saveAll(updateList);
//                }
//                System.out.println("Item: " + count);
//            } else {
//                continueItemFlg = false;
//            }
//            System.out.println("Item end!");
//        }
//
//        System.out.println("IM start!");
//        // im
//        Boolean continueImFlg = true;
//        int count1 = 0;
//        while (continueImFlg) {
//            count1 ++;
//            List<IM> imList = imService.tmpMethod2();
//            if (imList.size() > 0) {
//                List<IM> updateList = new ArrayList<>();
//                for (IM im : imList) {
//                    // **teamの処理
//                    String teamArr = "";
//                    // **memberの処理
//                    String memArr = "";
//
//                    List<IMRel> imRelList = imRelService.findByItemMId(im.getIm_id());
//                    for (IMRel imRel : imRelList) {
//                        teamArr = StringUtilsMine.addToStringArr(teamArr, imRel.getTeam_id());
//
//                        List<IMRelMem> imRelMemList = imRelMemService.findByImRelId(imRel.getIm_rel_id());
//                        for (IMRelMem imRelMem : imRelMemList) {
//                            memArr = StringUtilsMine.addToStringArr(memArr, imRelMem.getMember_id());
//                        }
//                    }
//                    im.setTeamArr(teamArr);
//                    im.setMemArr(memArr);
//                    updateList.add(im);
//                }
//                if (updateList.size() > 0) {
//                    imService.saveAll(updateList);
//                }
//                System.out.println("IM: " + count1);
//            } else {
//                continueImFlg = false;
//            }
//            System.out.println("IM end!");
//        }
//
//        System.out.println("Program start!");
//        //program
//        Boolean continuePFlg = true;
//        int count2 = 0;
//        while (continuePFlg) {
//            count2 ++;
//            List<Program> programList = programService.tmpMethod2();
//            if (programList.size() > 0) {
//                List<Program> updateList = new ArrayList<>();
//                for (Program program : programList) {
//                    // **teamの処理
//                    String teamArr = "";
//                    // **memberの処理
//                    String memArr = "";
//
//                    List<PRel> pRelList = pRelService.tmpMethod(program.getProgram_id());
//                    for (PRel pRel : pRelList) {
//                        teamArr = StringUtilsMine.addToStringArr(teamArr, pRel.getTeam_id());
//
//                        List<PRelMem> pRelMemList = pRelMemService.findByPRelId(pRel.getP_rel_id());
//                        for (PRelMem pRelMem : pRelMemList) {
//                            memArr = StringUtilsMine.addToStringArr(memArr, pRelMem.getMember_id());
//                        }
//                    }
//
//                    program.setTeamArr(teamArr);
//                    program.setMemArr(memArr);
//                    updateList.add(program);
//                }
//                if (updateList.size() > 0) {
//                    programService.saveAll(updateList);
//                }
//                System.out.println("Program: " + count2);
//            } else {
//                continuePFlg = false;
//            }
//            System.out.println("Program end!");
//        }
//
//        System.out.println("PM start!");
//        //pm
//        Boolean continuePmFlg = true;
//        int count3 = 0;
//        while (continuePmFlg) {
//            count3 ++;
//            List<PM> pmList = pmService.tmpMethod2();
//            if (pmList.size() > 0) {
//                List<PM> updateList = new ArrayList<>();
//                for (PM pm : pmList) {
//                    // **teamの処理
//                    String teamArr = "";
//                    // **memberの処理
//                    String memArr = "";
//
//                    List<PMRel> pmRelList = pmRelService.findByPmIdDelFlg(pm.getPm_id(), false);
//                    for (PMRel pmRel : pmRelList) {
//                        teamArr = StringUtilsMine.addToStringArr(teamArr, pmRel.getTeam_id());
//
//                        List<PMRelMem> pmRelMems = pmRelMemService.findByPRelIdDelFlg(pmRel.getPm_rel_id(), null);
//                        for (PMRelMem pmRelMem : pmRelMems) {
//                            memArr = StringUtilsMine.addToStringArr(memArr, pmRelMem.getMember_id());
//                        }
//                    }
//                    pm.setTeamArr(teamArr);
//                    pm.setMemArr(memArr);
//                    updateList.add(pm);
//                }
//                if (updateList.size() > 0) {
//                    pmService.saveAll(updateList);
//                }
//                System.out.println("PM: " + count3);
//            } else {
//                continuePmFlg = false;
//            }
//            System.out.println("PM end!");
//        }
//        System.out.println("ALL END!");
//    }

    /**
     * Program -> PM, Pm related tables tmp method
     * データをマスターに入れる。既存のをやるメソッド
     */
//    public void pmMasterMethod(String from, String to) throws ParseException {
//        // 対象のprogramを取得する
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//        List<Program> programList = programService.findByOnAirDateBeterrn(dateFormat.parse(from), dateFormat.parse(to));
//        // それぞれのprogramに対してpmとそのrelated dataを作成・insertする
//        for (Program p : programList) {
//            // PMを作る：すでにPMないか確認して
//            List<PM> pmList = pmService.findByTitleOnAirDate(p.getTitle(), p.getOn_air_date());
//
//            PM targetPM = null;
//
//            if (pmList.size() == 0) {
//                PM pm = new PM(null, p.getTitle(), p.getDescription(), false, null, null);
//                targetPM = pmService.save(pm);
//
//                // verも登録する
//            } else {
//                targetPM = pmList.stream().sorted().collect(Collectors.toList()).get(0);
//            }
//
//            // verを登録する
//            // すでにverがあるか確認する
//            List<PMVer> verList = pmVerService.findByPmIdStationId(targetPM.getPm_id(), p.getStation_id());
//
//            // verないなら登録する
//            if (verList.size() == 0) {
//                PMVer ver = new PMVer(null, targetPM.getPm_id(), p.getOn_air_date(), p.getStation_id(), false, null, null);
//                pmVerService.save(ver);
//            }
//
//            // pmrelを作る
//            List<PRel> relList = pRelService.getListByProgramId(p.getProgram_id());
//            List<PMRel> pmRelList = pmRelService.findByPmIdDelFlg(targetPM.getPm_id(), null);
//            for (PRel rel : relList) {
//                PMRel targetRel = null;
//                // pmrelになかったら登録する
//                if (pmRelList.stream().noneMatch(e -> e.getTeam_id().equals(rel.getTeam_id()))) {
//                    PMRel newRel = new PMRel(null, targetPM.getPm_id(), rel.getTeam_id(), null, null, false);
//                    PMRel savedRel = pmRelService.save(newRel);
//                    targetRel = savedRel;
//                } else {
//                    targetRel = pmRelList.stream().filter(e -> e.getTeam_id().equals(rel.getTeam_id())).sorted().collect(Collectors.toList()).get(0);
//
//                    // 既存データあるけど削除されてる場合、復活してあげる
//                    if (targetRel.getDel_flg().equals(true)) {
//                        targetRel.setDel_flg(false);
//                        pmRelService.save(targetRel);
//                    }
//                }
//
//                // memも確認する
//                List<PRelMem> memList = pRelMemService.findByPRelId(rel.getP_rel_id());
//                List<PMRelMem> pmMemList = pmRelMemService.findByPRelIdDelFlg(targetRel.getPm_rel_id(), null);
//                for (PRelMem mem : memList) {
//                    if (pmMemList.stream().noneMatch(e -> e.getMember_id().equals(mem.getMember_id()))) {
//                        // 既存なかったら登録
//                        PMRelMem newMem = new PMRelMem(null, targetRel.getPm_rel_id(), mem.getMember_id(), null, null, false);
//                        if (newMem.getPm_rel_id() == null) {
//                            System.out.println("here");
//                        }
//                        pmRelMemService.save(newMem);
//                    } else {
//                        PMRelMem targetMem = pmMemList.stream().filter(e -> e.getMember_id().equals(mem.getMember_id())).findFirst().get();
//
//                        // 既存あるが削除されてる場合、復活してあげる
//                        if (targetMem.getDel_flg().equals(true)) {
//                            targetMem.setDel_flg(false);
//                            pmRelMemService.save(targetMem);
//                        }
//                    }
//                }
//            }
//        }
//    }

//    /**
//     * Item -> ItemRel
//     * Item.team_id, member_id, wp_idが存在しないとエラーになります
//     */
//    public void moveItemToItemRel2() {
//        List<Item> itemList = itemService.findAll();
//        List<ItemRel> saveList = new ArrayList<>();
//        for (Item i : itemList) {
//            if (i.getTeam_id() == null) {
//                continue;
//            }
//
//            ItemRel rel = new ItemRel();
//            rel.setItem_id(i.getItem_id());
//
//            List<Long> teamIdList = getLongIdList(i.getTeam_id());
//            for (Long teamId : teamIdList) {
//                if (teamId == 0) {
//                    continue;
//                }
//                if (rel.getTeam_id() == null) {
//                    rel.setTeam_id(teamId);
//                    saveList.add(rel);
//                } else {
//                    ItemRel newRel = new ItemRel();
//                    BeanUtils.copyProperties(rel, newRel);
//                    newRel.setTeam_id(teamId);
//                    saveList.add(newRel);
//                }
//            }
//        }
//
//        // ここでitemIdとteamIdだけ入れたrelを全部登録する
//        if (saveList.size() > 0) {
//            itemRelService.saveAll(saveList);
//        }
//
//        // memberのあるitemだけ処理入る
//        List<Item> memberItemList = itemList.stream().filter(e -> e.getMember_id() != null).collect(Collectors.toList());
//        logger.debug("memberItemList size: " + memberItemList.size());
//        List<ItemRel> memberSaveList = new ArrayList<>();
//
//        for (Item i : memberItemList) {
//            if (i.getMember_id() == null) {
//                continue;
//            }
//
//            List<Long> mIdList = getLongIdList(i.getMember_id());
//            for (Long mId : mIdList) {
//                Long teamId = MemberEnum.getTeamIdById(mId);
//                ItemRel rel = itemRelService.findByItemIdTeamIdMemberIdNull(i.getItem_id(), teamId);
//
//                if (rel == null) {
//                    ItemRel newRel = new ItemRel();
//                    newRel.setItem_id(i.getItem_id());
//                    newRel.setTeam_id(teamId);
//                    newRel.setMember_id(mId);
//                    memberSaveList.add(newRel);
//                } else {
//                    rel.setMember_id(mId);
//                    memberSaveList.add(rel);
//                }
//            }
//        }
//        itemRelService.saveAll(memberSaveList);
//
//        // wpIdがあったItemだけ、総合ブログのteamIdだったらrelにwpIdセットします
//        List<Item> wpItemList = itemList.stream().filter(e -> e.getWp_id() != null).collect(Collectors.toList());
//        List<ItemRel> wpSaveList = new ArrayList<>();
//
//        for (Item i : wpItemList) {
//            if (i.getWp_id() == null) {
//                continue;
//            }
//
//            List<ItemRel> wpAddRelList = itemRelService.findByItemId(i.getItem_id());
//            for (ItemRel rel : wpAddRelList) {
//                if (TeamEnum.findSubDomainById(Math.toIntExact(rel.getTeam_id())).equals("NA")) {
//                    rel.setWp_id((long)i.getWp_id());
//                    wpSaveList.add(rel);
//                }
//            }
//        }
//
//        if (wpSaveList.size() > 0) {
//            itemRelService.saveAll(wpSaveList);
//        }
//    }

//    /**
//     * ItemMaster -> Rel
//     * Itemmaster.team_id, member_id, wp_idが存在しないとエラーになります
//     */
//    public void moveItemMasterToIMRel2() {
//        List<ItemMaster> itemMList = itemMasterService.findAll();
//        List<IMRel> saveList = new ArrayList<>();
//        for (ItemMaster im : itemMList) {
//            if (im.getTeam_id() == null) {
//                continue;
//            }
//
//            IMRel rel = new IMRel();
//            rel.setItem_m_id(im.getItem_m_id());
//            List<Long> teamIdList = getLongIdList(im.getTeam_id());
//            for (Long teamId : teamIdList) {
//                if (teamId == 0) {
//                    continue;
//                }
//                if (rel.getTeam_id() == null) {
//                    rel.setTeam_id(teamId);
//                    saveList.add(rel);
//                } else {
//                    IMRel newRel = new IMRel();
//                    BeanUtils.copyProperties(rel, newRel);
//                    newRel.setTeam_id(teamId);
//                    saveList.add(newRel);
//                }
//            }
//        }
//
//        if (saveList.size() > 0) {
//            imRelService.saveAll(saveList);
//        }
//
//        // member
//        List<ItemMaster> memberItemList = itemMList.stream().filter(e -> e.getMember_id() != null).collect(Collectors.toList());
//        List<IMRel> memberSaveList = new ArrayList<>();
//
//        for (ItemMaster im : memberItemList) {
//            if (im.getMember_id() == null) {
//                continue;
//            }
//
//            List<Long> memberIdList = getLongIdList(im.getMember_id());
//            for (Long mId : memberIdList) {
//                Long teamId = MemberEnum.getTeamIdById(mId);
//                IMRel rel = imRelService.findByItemIdTeamIdMemberIdNull(im.getItem_m_id(), teamId);
//
//                if (rel == null) {
//                    IMRel newRel = new IMRel();
//                    newRel.setItem_m_id(im.getItem_m_id());
//                    newRel.setTeam_id(teamId);
//                    newRel.setMember_id(mId);
//                    memberSaveList.add(newRel);
//                } else {
//                    rel.setMember_id(mId);
//                    memberSaveList.add(rel);
//                }
//            }
//        }
//
//        if (memberSaveList.size() > 0) {
//            imRelService.saveAll(memberSaveList);
//        }
//
//        // wpId
//        List<ItemMaster> wpIMList = itemMList.stream().filter(e -> e.getWp_id() != null && e.getTeam_id() != null).collect(Collectors.toList());
//        List<IMRel> wpSaveList = new ArrayList<>();
//        for (ItemMaster im : wpIMList) {
//            if (im.getWp_id() == null) {
//                continue;
//            }
//
//            List<IMRel> wpAddRelList = imRelService.findByItemMId(im.getItem_m_id());
//            for (IMRel rel : wpAddRelList) {
//
//                logger.debug(rel.getTeam_id() + " " + rel.getItem_m_id());
//                if (rel.getTeam_id() == 0) {
//                    continue;
//                }
//                if (TeamEnum.findSubDomainById(Math.toIntExact(rel.getTeam_id())).equals("NA")) {
//                    rel.setWp_id((long)im.getWp_id());
//                    wpSaveList.add(rel);
//                }
//            }
//        }
//
//        if (wpSaveList.size() > 0) {
//            imRelService.saveAll(wpSaveList);
//        }
//    }

//    /**
//     * Program -> Rel
//     * Program.team_id, member_idが存在しないとエラーになります
//     */
//    public void moveProgramToPRel2() {
//        List<Program> programList = programService.findall();
//        List<PRel> saveList = new ArrayList<>();
//        for (Program p : programList) {
//            if (p.getTeam_id() == null) {
//                continue;
//            }
//
//            PRel rel = new PRel();
//            rel.setProgram_id(p.getProgram_id());
//            List<Long> teamIdList = getLongIdList(p.getTeam_id());
//            for (Long teamId : teamIdList) {
//                if (teamId == 0) {
//                    continue;
//                }
//                if (rel.getTeam_id() == null) {
//                    rel.setTeam_id(teamId);
//                    saveList.add(rel);
//                } else {
//                    PRel newRel = new PRel();
//                    BeanUtils.copyProperties(rel, newRel);
//                    newRel.setTeam_id(teamId);
//                    saveList.add(newRel);
//                }
//            }
//        }
//
//        if (saveList.size() > 0) {
//            pRelService.saveAll(saveList);
//        }
//
//        // member
//        List<Program> memberProgramList = programList.stream().filter(e -> e.getMember_id() != null).collect(Collectors.toList());
//        List<PRel> memberSaveList = new ArrayList<>();
//
//        for (Program p : memberProgramList) {
//            if (p.getMember_id() == null) {
//                continue;
//            }
//
//            List<Long> memberIdList = getLongIdList(p.getMember_id());
//            for (Long mId : memberIdList) {
//                Long teamId = MemberEnum.getTeamIdById(mId);
//                PRel rel = pRelService.findByItemIdTeamIdMemberIdNull(p.getProgram_id(), teamId);
//
//                if (rel == null) {
//                    PRel newRel = new PRel();
//                    newRel.setProgram_id(p.getProgram_id());
//                    newRel.setTeam_id(teamId);
//                    newRel.setMember_id(mId);
//                    memberSaveList.add(newRel);
//                } else {
//                    rel.setMember_id(mId);
//                    memberSaveList.add(rel);
//                }
//            }
//        }
//
//        if (memberSaveList.size() > 0) {
//            pRelService.saveAll(memberSaveList);
//        }
//    }

    /**
     * teamIdリストを返します
     *
     * @param teamIdStr
     * @return
     */
    private List<Long> getLongIdList(String teamIdStr) {
        List<Long> resultList = new ArrayList<>();
        if (teamIdStr != null) {
            String[] strList = teamIdStr.split(",");
            for (String s : strList) {
                logger.debug(s);
                try {
                    resultList.add(Long.valueOf(s));
                } catch (Exception e) {
                    logger.debug("Error");
                }
            }
        }
        return resultList;
    }

    /**
     * wpIdは総合ブログのを引き継いでいいか判断し、引き継ぐ場合はidを、引き継がない場合はnullを返します
     *
     * @param wpId
     * @return
     */
//    private Long getWpIdJudge(Long wpId, Long teamId) {
//        String subDomain = TeamEnum.findSubDomainById(teamId);
//        if (subDomain.equals("NA")) {
//            return wpId;
//        } else {
//            return 0L;
//        }
//    }

    /**
     * [From] BlogController
     * TmpController内のBlogControllerからお引越してきたメソッドたちはブログのチームごと分岐前のメソッド。走らせたらエラーになってしまうが、とりあえずエラー解消のためheader作成メソッドを持ってきました。もし走らせたいならblogControllerのheader作るメソッド（これと同名）de
     * エラーが出ないように治してね
     * 認証などどのリクエストでも必要なヘッダーをセットする(第2引数がリストではなくチーム1件の場合)。
     *
     * @param headers
     * @return
     */
    public HttpHeaders generalHeaderSet(HttpHeaders headers) {

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = "";
        // 走らせたいならここをチームによってurl変更するように修正
        auth = new String(Base64.getEncoder().encode(setting.getApiPw().getBytes()));
        headers.add("Authorization", "Basic " + auth);
        return headers;
    }

    /**
     * [From] BlogController
     * WpIdからポストの内容を取得します。
     * TmpController内のBlogControllerからお引越してきたメソッドたちはブログのチームごと分岐前のメソッド。走らせたらエラーになってしまうが、とりあえずエラー解消のためheader作成メソッドを持ってきました。もし走らせたいならblogControllerのheader作るメソッド（これと同名）de
     * エラーが出ないように治してね
     *
     * @param wpId
     * @return
     */
    public String requestPostData(String wpId) {
        // 走らせたいならここをチームによってurl変更するように修正
        String finalUrl = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + wpId;
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        return blogController.request(finalUrl, new HttpEntity<>(headers), HttpMethod.GET, "requestPostData()");
    }

    /**
     * [From] BlogController
     * 日付タグをWPとDBに登録します。
     * TmpController内のBlogControllerからお引越してきたメソッドたちはブログのチームごと分岐前のメソッド。走らせたらエラーになってしまうが、とりあえずエラー解消のためheader作成メソッドを持ってきました。もし走らせたいならblogControllerのheader作るメソッド（これと同名）de
     * エラーが出ないように治してね
     *
     * @param date
     * @return
     */
//    public BlogTag registerTag(Date date) {
//        // チームによってurlを変更
//        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "tags/";
//
//        HttpHeaders h = generalHeaderSet(new HttpHeaders()) ;
//        JSONObject jo = new JSONObject();
//        jo.put("name", dateUtils.getYYYYMM(date));
//
//        HttpEntity<String> request = new HttpEntity<>(jo.toString(), h);
//        String res = blogController.request(url, request, HttpMethod.POST);
//
//        JSONObject jsonObject1 = jsonUtils.createJsonObject(res);
//
//        int yyyyMMId;
//        if (jsonObject1.get("id") != null) {
//            yyyyMMId = jsonObject1.getInt("id");
//            String link = jsonObject1.getString("link").replaceAll("^\"|\"$", "");
//            BlogTag blogTag = new BlogTag();
//            blogTag.setTag_name(dateUtils.getYYYYMM(date));
//            blogTag.setWp_tag_id((long) yyyyMMId);
//            blogTag.setLink(link);
//            return blogTagService.save(blogTag);
//        }
//        return new BlogTag();
//    }

//    /**
//     * WPにあるがDBにないタグを保存する
//     *
//     */
//    public void getBlogTagNotSavedOnInfoDb() {
//        // チームによってurlを変更
//        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "tags?_fields[]=id&_fields[]=name&_fields[]=link";
//
//        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//        JSONObject jsonObject = new JSONObject();
//        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//        String res = blogController.request(url, request, HttpMethod.GET);
//        List<BlogTag> blogTagList = new ArrayList<>();
//
//        try {
//            if (JsonUtils.isJsonArray(res)) {
//                JSONArray ja = new JSONArray(res);
//                for (int i=0;i<ja.length();i++) {
//                    Integer wpId = ja.getJSONObject(i).getInt("id");
//                    String tagName = ja.getJSONObject(i).getString("name").replaceAll("^\"|\"$", "");
//                    String link = ja.getJSONObject(i).getString("link").replaceAll("^\"|\"$", "");
//
//                    if (blogTagService.findBlogTagIdByTagName(tagName) == 0) {
//                        BlogTag blogTag = new BlogTag();
//                        blogTag.setWp_tag_id((long)wpId);
//                        blogTag.setTag_name(tagName);
//                        blogTag.setLink(link);
//                        blogTagList.add(blogTag);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // infoDBに保存されていないタグは保存する
//        if (blogTagList.size() > 0) {
//            blogTagService.saveIfNotSaved(blogTagList);
//        }
//    }

    /**
     * [From] BlogController
     */
    public void tmpMethod() {
        String result = "[toc depth='5']";
        result = result + "<br /><h2>test from java</h2>\n<h2>h22</h2><h2>h23</h2><h3>h31</h3><h6>h6</h6>";

        logger.debug(result);

        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "java test");
        jsonObject.put("author", 1);
        jsonObject.put("status", "publish");
        jsonObject.put("content", result);
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/";
        String res = blogController.request(url, request, HttpMethod.POST, "tmpMethod()");
        logger.debug(res);
    }

    /**
     * [From] RakutenController
     * @param searchList
     * @return
     */
    public List<Item> search1(List<String> searchList, Long teamId) throws InterruptedException {
        List<Item> resultList = new ArrayList<>();

        for (String key : searchList) {
            String parameter = "&itemCode=" + key + "&elements=itemCode%2CitemCaption%2CitemName&" + setting.getRakutenAffiliId();
            JSONObject node = rakutenController.request(parameter, teamId);
            if (node != null && !node.equals("")) {

                if (node.has("Items") && !JsonUtils.isJsonArray(node.getString("Items"))) {
                    continue;
                }

                JSONArray items = node.getJSONArray("Items");
                for (int i=0; i<items.length();i++) {
                    try {
                        Item item = new Item();
                        item.setItem_code(key);
                        item.setItem_caption(StringUtilsMine.compressString(items.getJSONObject(i).getString("itemCaption").replaceAll("^\"|\"$", ""), 200));
                        item.setTitle(items.getJSONObject(i).getString("itemName").replaceAll("^\"|\"$", ""));
                        resultList.add(item);
                    } catch (Exception e) {
                        logger.debug(e.getMessage());
                    }
                }
            } else {
                logger.info("Rakutenでデータが見つかりませんでした");
            }
        }
        return resultList;
    }

    /**
     * [From] AnalyzeController
     * 文字列から年月日をみつけ、返します。
     * 発売日、予約締切日などが引っかかる想定。
     *
     * @param text
     * @return
     */
    public List<String> extractYMDList(String text) {
        String regex = "20[0-2]{1}[0-9]{1}(.?)(1[0-2]{1}|0?[1-9])(.?)(3[01]|[12][0-9]|0?[1-9])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<String> publishDateList = new ArrayList<>();
        while (matcher.find()) {
            publishDateList.add(matcher.group());
        }
        return publishDateList;
    }

    /**
     * [From] BlogController
     */
//    public void postAllItemMaster() {
//        Integer year = 2001;
//
//        Map<ItemMaster, List<Item>> itemMasterListMap = new HashMap<>();
//        while (year < 2022) {
//            logger.debug("*** year: " + year);
//            // itemMasterを集める
//            List<ItemMaster> itemMasterList = itemMasterService.findByPublicationYearWpIdNull(year);
//            logger.debug("itemMasterList.size: " + itemMasterList.size());
//            // ひもづくitemを集める
//            itemMasterList.forEach(e -> itemMasterListMap.put(e, itemService.gatherItems(e.getItem_m_id())));
//            // itemMasterを投稿する
//            if (itemMasterListMap.size() > 0) {
//                itemMasterListMap.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().getPublication_date()));
//                for (Map.Entry<ItemMaster, List<Item>> e : itemMasterListMap.entrySet()) {
//                    logger.debug("item_m_id: " + e.getKey().getItem_m_id() + " itemList size: " + e.getValue().size());
//                    blogController.postMasterItem(e.getKey(), e.getValue());
//                }
//            }
//            ++year;
//        }
//    }

    /**
     * [From] BlogController
     * Tmpブログ新商品投稿メソッド(商品マスターごとに投稿するように修正)
     *
     */
//    public void tmpItemPost(List<Item> itemList) {
//        Map<ItemMaster, List<Item>> map = itemUtils.groupItem(itemList);
//        // 対象はwp_idがnullのマスター商品
//        Map<ItemMaster, List<Item>> targetMap = map.entrySet().stream()
//                .filter(e -> IMRelService.getWpIdByItemMId(e.getKey().getItem_m_id()) == null || IMRelService.getWpIdByItemMId(e.getKey().getItem_m_id()).equals(0))
//                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
//        // targetMapのマスタ商品をブログに投稿していく
//        for (Map.Entry<ItemMaster, List<Item>> e : targetMap.entrySet()) {
//            blogController.postMasterItem(e.getKey(), e.getValue());
//        }
//    }

    /**
     * [From] BlogController
     * 商品リストからアイキャッチメディアの登録がない商品だけを引き抜いてリストにし返却します。
     *
     * @param itemList
     * @return Item:
     */
//    public List<Item> selectBlogData(List<Item> itemList) {
//        List<Item> resultList = new ArrayList<>();
//        for (Item item : itemList) {
//            String result = requestPostData(item.getWp_id().toString());
//            Integer featuredMedia = blogController.extractMedia(result);
//            if (featuredMedia == 0) {
//                resultList.add(item);
//            }
//        }
//        return resultList;
//    }

//    /**
//     * [From] BlogController
//     * 商品（マスタじゃない）ページは下書きにする
//     *
//     */
//    public void deleteItemPosts() {
//
//        List<Long> wpIdList = itemService.collectWpId().stream().distinct().collect(Collectors.toList());
//
//        for (Long wpId : wpIdList) {
//            // WPにあるタグを取得する
//            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + wpId;
//            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("status","draft");
//            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//            blogController.request(url, request, HttpMethod.POST);
//        }
//    }

    /**
     * [From] BlogController
     * Nullが入ってるWPIDをコンソールに出力する
     */
    public void listPostsContainsNull() {
        int n = 1;
        boolean flg = true;
        while (flg) {
            logger.debug(n);
            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts?status=publish&per_page=40&page=" + n;

            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
            JSONObject jsonObject = new JSONObject();
            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
            String res = blogController.request(url, request, HttpMethod.GET, "listPostsContainsNull()");

            try {
                if (!JsonUtils.isJsonArray(res)) {
                    continue;
                }

                JSONArray ja = new JSONArray(res);
                for (int i=0;i<ja.length();i++) {
                    Integer wpId = ja.getJSONObject(i).getInt("id");
                    Integer media = ja.getJSONObject(i).getInt("featured_media");
                    if (media > 0) {
                        logger.debug(wpId + ":" + media);
                    }
                }
            } catch (Exception e) {
                flg = false;
                e.printStackTrace();
            }
            ++n;
        }
    }

    /**
     * [From] BlogController
     * 公開中のブログポストのcontentを上書きする（楽天リンクをカードにした）
     *
     */
//    public void updateContent() {
//        int n = 1;
//        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts?status=publish&per_page=40&page=" + n;
//
//        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//        JSONObject jsonObject = new JSONObject();
//        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//        String res = blogController.request(url, request, HttpMethod.GET);
//
//        try {
//            if (JsonUtils.isJsonArray(res)) {
//                JSONArray ja = new JSONArray(res);
//                for (int i=0;i<ja.length();i++) {
//                    Integer wpId = ja.getJSONObject(i).getInt("id");
//                    url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + wpId;
//
//                    HttpHeaders headers1 = generalHeaderSet(new HttpHeaders());
//                    JSONObject jsonObject1 = new JSONObject();
//                    ItemMaster itemMaster = itemMasterService.findByWpId(wpId);
//
//                    if (itemMaster != null && itemMaster.getIm_id() != null) {
//                        List<Item> itemList = itemService.findByMasterId(itemMaster.getIm_id());
//
//                        if (itemList.size() > 0) {
//                            Map<ItemMaster, List<Item>> itemMasterListMap = Collections.singletonMap(itemMaster, itemList);
//                            String text = textController.blogReleaseItemsText(itemMasterListMap).get(0);
//                            jsonObject1.put("content", text);
//                            HttpEntity<String> request1 = new HttpEntity<>(jsonObject1.toString(), headers1);
//                            String r = blogController.request(url, request1, HttpMethod.POST);
//                            logger.debug(r);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        ++n;
//    }

    /**
     * [From] BlogController
     * タイトルを書き換えます
     */
//    public void updateTitle() {
//        List<ItemMaster> itemMasterList = itemMasterService.findWpIdNotNull();
//
//        for (ItemMaster itemMaster : itemMasterList) {
//            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//            JSONObject jsonObject = new JSONObject();
//            String title = textController.createBlogTitle(itemMaster.getPublication_date(), itemMaster.getTitle());
//            jsonObject.put("title", title);
//            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//
//            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + itemMaster.getWp_id();
//            blogController.request(url, request, HttpMethod.POST);
//        }
//    }

    /**
     * [From] BlogController
     * 既存のWP投稿に対して、DBのタグby teamにyyyyMMタグを追加してWPにポストします。
     */
//    public void addTag() {
//        getBlogTagNotSavedOnInfoDb();
//        List<ItemMaster> itemMasterList = itemMasterService.findWpIdNotNull();
//
//        for (ItemMaster itemMaster : itemMasterList) {
//            HttpHeaders headers = generalHeaderSet(new HttpHeaders());
//            JSONObject jsonObject = new JSONObject();
//
//            Integer[] tags = new Integer[itemMaster.getTags().length + 1];
//            System.arraycopy(itemMaster.getTags(), 0, tags, 0, itemMaster.getTags().length);
//
//            int yyyyMMId = dateUtils.getBlogYYYYMMTag(itemMaster.getPublication_date());
//
//            // もし年月タグがまだ存在しなかったら先に登録する
//            if (yyyyMMId == 0) {
//                yyyyMMId = Math.toIntExact(registerTag(itemMaster.getPublication_date()).getBlog_tag_id());
//            }
//            tags[itemMaster.getTags().length] = yyyyMMId;
//            jsonObject.put("tags", tags);
//            HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
//
//            // 商品ページ投稿更新
//            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts/" + itemMaster.getWp_id();
//            blogController.request(url, request, HttpMethod.POST);
//        }
//    }

    /**
     * [From] BlogController
     * 公開済み投稿でfeatured_mediaの設定があるものを返却します
     * メソッド一部間違えてたから使用箇所でまた実行した方がいいかも
     * @return Map\<WpId, featuredMediaId>
     */
    public Map<Integer, Integer> getPublishedWpIdFeaturedMediaList() {
        Map<Integer, Integer> resultMap = new HashMap<>();

        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);

        int n = 1;
        boolean nextFlg = true;

        while (nextFlg) {
            String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "posts?status=publish&_fields[]=featured_media&_fields[]=id&per_page=100&page=" + n;
            String res = blogController.request(url, request, HttpMethod.GET, "getPublishedWpIdFeaturedMediaList()");

            // レスポンスを成形
            try {
                if (!JsonUtils.isJsonArray(res)) {
                    continue;
                }
                JSONArray ja = new JSONArray(res);

                if (ja.length() > 0) {
                    for (int i=0; i < ja.length(); i++) {
                        if (ja.getJSONObject(i).getInt("featured_media") != 0) {
                            resultMap.put(ja.getJSONObject(i).getInt("id"), ja.getJSONObject(i).getInt("featured_media"));
                        }
                    }
                    ++n;
                }
            } catch (Exception e) {
                nextFlg = false;
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    /**
     * [From] BlogController
     * WP featuredMediaIDからそのメディアのeternalPathを取得し返却します
     * @param mediaIdList
     * @return Map<featuredMediaId, imagePath>
     */
    public Map<Integer, String> getMediaUrlByMediaId(List<Integer> mediaIdList) {
        Map<Integer, String> resultMap = new HashMap<>();

        int start = 0;
        int end = mediaIdList.size() -1;
        boolean next100Flg = true;

        if (end > 99) {
            end = 99;
        }

        List<String> mediaIrListStrList = new ArrayList<>();

        while (next100Flg && start < end) {
            String tmp = mediaIdList.subList(start, end).stream().map(Object::toString).collect(Collectors.joining(","));
            mediaIrListStrList.add(tmp);
            if (mediaIdList.size() > end + 1) {
                start += 100;
                end += 100;

                if (mediaIdList.size() -1 < end) {
                    end = mediaIdList.size() -1;
                }
            } else {
                next100Flg = false;
            }
        }

        logger.debug("mediaIrListStrList.size(): " + mediaIrListStrList.size());
        for (String mediaIdStr : mediaIrListStrList) {
            String res = getMediaUrl(mediaIdStr);
            // レスポンスを成形
            try {
                if (!JsonUtils.isJsonArray(res)) {
                    continue;
                }
                JSONArray ja = new JSONArray(res);

                if (ja.length() > 0) {
                    for (int i=0; i < ja.length(); i++) {
                        resultMap.put(ja.getJSONObject(i).getInt("id"), ja.getJSONObject(i).getString("source_url").replaceAll("^\"|\"$", ""));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }

    /**
     * [From] BlogController
     * 引数のmediaIdのWP eternalPathを取得し返却します。
     *
     * @param mediaId
     * @return eternalPath
     */
    private String getMediaUrl(String mediaId) {
        String url = setting.getBlogWebUrl() + setting.getBlogApiPath() + "media?slug=" + mediaId + "&_fields[]=id&_fields[]=source_url&per_page=100";

        // リクエスト送信
        HttpHeaders headers = generalHeaderSet(new HttpHeaders());
        JSONObject jsonObject = new JSONObject();
        HttpEntity<String> request = new HttpEntity<>(jsonObject.toString(), headers);
        return blogController.request(url, request, HttpMethod.GET, "getMediaUrl()");
    }
}
