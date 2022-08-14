//package otaku.info.service;
//
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import otaku.info.entity.IRelMem;
//import otaku.info.repository.IRelMemRepository;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//@Service
//@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
//@AllArgsConstructor
//public class IRelMemService {
//
//    @Autowired
//    IRelMemRepository iRelMemRepository;
//
//    public List<IRelMem> saveAll(List<IRelMem> relMemList) {
//        return iRelMemRepository.saveAll(relMemList);
//    }
//
//    /**
//     *
//     *
//     * @param itemId
//     * @return
//     */
//    public List<IRelMem> findByIRelId(Long itemId) {
//        return iRelMemRepository.findByIRelId(itemId);
//    }
//}
