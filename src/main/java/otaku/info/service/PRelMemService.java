//package otaku.info.service;
//
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import otaku.info.entity.PRelMem;
//import otaku.info.repository.PRelMemRepository;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//@Service
//@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
//@AllArgsConstructor
//public class PRelMemService {
//
//    @Autowired
//    PRelMemRepository pRelMemRepository;
//
//    public List<PRelMem> findByPRelId(Long relId) {
//        return pRelMemRepository.findByPRelId(relId);
//    }
//
//    public PRelMem save(PRelMem relMem) {
//        return pRelMemRepository.save(relMem);
//    }
//}
