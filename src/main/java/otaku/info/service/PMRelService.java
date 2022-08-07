//package otaku.info.service;
//
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import otaku.info.entity.PMRel;
//import otaku.info.repository.PMRelRepository;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//@Service
//@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
//@AllArgsConstructor
//public class PMRelService {
//
//    @Autowired
//    PMRelRepository pmRelRepository;
//
//    public PMRel save(PMRel rel) {
//        return pmRelRepository.save(rel);
//    }
//
//    public List<PMRel> saveAll(List<PMRel> relList) {
//        return pmRelRepository.saveAll(relList);
//    }
//
//    public PMRel findByPmRelId(Long id) {
//        return pmRelRepository.findByPmRelId(id);
//    }
//
//    public List<Long> findTeamIdByProgramId(Long pId) {
//        return pmRelRepository.findTeamIdByProgramId(pId);
//    }
//
//    public List<PMRel> findByPmIdDelFlg(Long pmId, Boolean delFlg) {
//        if (delFlg == null) {
//            return pmRelRepository.findByPmId(pmId);
//        } else {
//            return pmRelRepository.findByPmIdDelFlg(pmId, delFlg);
//        }
//    }
//
//    public PMRel findByPmIdTeamId(Long pmId, Long teamId) {
//        return pmRelRepository.findByPmIdTeamId(pmId, teamId).orElse(null);
//    }
//
//    public List<Long> getTeamIdList(Long pmId) {
//        return pmRelRepository.getTeamIdList(pmId);
//    }
//}
