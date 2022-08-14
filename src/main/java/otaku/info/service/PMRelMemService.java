//package otaku.info.service;
//
//import com.sun.istack.Nullable;
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import otaku.info.entity.PMRelMem;
//import otaku.info.repository.PMRelMemRepository;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//@Service
//@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
//@AllArgsConstructor
//public class PMRelMemService {
//
//    @Autowired
//    PMRelMemRepository pmRelMemRepository;
//
//    public PMRelMem save(PMRelMem mem) {
//        return pmRelMemRepository.save(mem);
//    }
//
//    public List<PMRelMem> saveAll(List<PMRelMem> memList) {
//        return pmRelMemRepository.saveAll(memList);
//    }
//
//    public PMRelMem findByPmRelMemId(Long id) {
//        return pmRelMemRepository.findByPmRelMemId(id).orElse(null);
//    }
//
//    public List<PMRelMem> findByPRelIdDelFlg(Long pmRelId, @Nullable Boolean delFlg) {
//        if (delFlg == null) {
//            return pmRelMemRepository.findByPRelId(pmRelId);
//        } else {
//            return pmRelMemRepository.findByPRelIdDelFlg(pmRelId, delFlg);
//        }
//    }
//
//    public PMRelMem findByPmRelIdMemId(Long pmRelId, Long memId) {
//        return pmRelMemRepository.findByPmRelIdMemId(pmRelId, memId).orElse(null);
//    }
//
//    public PMRelMem findByPmIdMemId(Long pmId, Long memId) {
//        return pmRelMemRepository.findByPmIdMemId(pmId, memId).orElse(null);
//    }
//
//    public List<PMRelMem> findByPmRelIdListDelFlg(List<Long> pmRelIdList, Boolean delFlg) {
//        return pmRelMemRepository.findByPmRelIdListDelFlg(pmRelIdList, delFlg);
//    }
//}
