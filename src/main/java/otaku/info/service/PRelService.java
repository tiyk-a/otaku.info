//package otaku.info.service;
//
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import otaku.info.entity.PRel;
//import otaku.info.entity.PRelMem;
//import otaku.info.repository.PRelRepository;
//
//import javax.transaction.Transactional;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
//@AllArgsConstructor
//public class PRelService {
//
//    @Autowired
//    PRelMemService pRelMemService;
//
//    @Autowired
//    PRelRepository pRelRepository;
//
//    public List<PRel> tmpMethod(Long teamId) {
//        return pRelRepository.tmpMethod(teamId);
//    }
//
//    /**
//     * その番組のrel全てを返します
//     * @param programId
//     * @return
//     */
//    public List<PRel> getListByProgramId(Long programId) {
//        List<PRel> relList = pRelRepository.findAllByProgramId(programId);
//        if (relList == null) {
//            relList = new ArrayList<>();
//        }
//        return relList;
//    }
//
//    /**
//     * チームID("n,n,n,n,n")をLongListにして返します。
//     *
//     * @return
//     */
//    public List<Long> getTeamIdList(Long programId) {
//        List<Long> teamIdList = pRelRepository.findTeamIdListByProgramId(programId);
//        if (teamIdList == null) {
//            teamIdList = new ArrayList<>();
//        }
//        return teamIdList;
//    }
//
//    /**
//     * メンバーID("n,n,n,n,n")をLongListにして返します。
//     *
//     * @return
//     */
//    public List<Long> getMemberIdList(Long programId) {
//        List<PRel> relList = pRelRepository.findAllByProgramId(programId);
//        List<Long> memIdList = new ArrayList<>();
//
//        for (PRel rel : relList) {
//            List<PRelMem> memList = pRelMemService.findByPRelId(rel.getP_rel_id());
//            if (!memList.isEmpty()) {
//                for (PRelMem relMem : memList) {
//                    memIdList.add(relMem.getMember_id());
//                }
//            }
//        }
//        return memIdList;
//    }
//
//    public PRel save(PRel rel) {
//        return pRelRepository.save(rel);
//    }
//
//    public List<PRel> saveAll(List<PRel> relList) {
//        List<PRel> tmpList = removeExistRecord(relList);
//        return pRelRepository.saveAll(tmpList);
//    }
//
//    public List<PRel> findAll() {
//        return pRelRepository.findAll();
//    }
//
//    public List<PRel> removeExistRecord(List<PRel> imRelList) {
//        List<PRel> returnList = new ArrayList<>();
//        for (PRel rel : imRelList) {
//            boolean exists = pRelRepository.existsByElem(rel.getProgram_id(), rel.getTeam_id()) > 0;
//            if (!exists && rel.getProgram_id() != null) {
//                returnList.add(rel);
//            }
//        }
//        return returnList;
//    }
//
//    public PRel findByProgramIdTeamId(Long pId, Long teamId) {
//        return pRelRepository.findByProgramIdTeamId(pId, teamId);
//    }
//}
