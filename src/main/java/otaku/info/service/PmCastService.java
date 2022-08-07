//package otaku.info.service;
//
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//import otaku.info.entity.PMCast;
//import otaku.info.repository.PmCastRepository;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//@Service
//@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
//@AllArgsConstructor
//public class PmCastService {
//
//    private PmCastRepository pmCastRepository;
//
//    public PMCast save(PMCast cast) {
//        return  pmCastRepository.save(cast);
//    }
//
//    public List<PMCast> findByPmId(Long pmId) {
//        return pmCastRepository.findByPmId(pmId);
//    }
//}
