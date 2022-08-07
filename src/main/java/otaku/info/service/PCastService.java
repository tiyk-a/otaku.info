//package otaku.info.service;
//
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//import otaku.info.entity.PCast;
//import otaku.info.repository.PCastRepository;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//@Service
//@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
//@AllArgsConstructor
//public class PCastService {
//
//    protected PCastRepository pCastRepository;
//
//    public List<PCast> findByProgramId(Long programId) {
//        return pCastRepository.findByProgramId(programId);
//    }
//}
