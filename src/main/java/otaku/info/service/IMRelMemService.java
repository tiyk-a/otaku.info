package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.IMRelMem;
import otaku.info.repository.IMRelMemRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class IMRelMemService {

    @Autowired
    IMRelMemRepository imRelMemRepository;

    public IMRelMem findByImRelMemId(Long imRelMemId) {
        return imRelMemRepository.findByImRelMemId(imRelMemId);
    }

    public List<IMRelMem> findByImRelId(Long imRelId) {
        return imRelMemRepository.findByImRelId(imRelId);
    }

    public List<IMRelMem> findByImRelIdNotDeleted(Long imRelId) {
        return imRelMemRepository.findByImRelIdNotDeleted(imRelId);
    }

    public List<IMRelMem> findByImRelIdListNotDeleted(List<Long> imRelIdList) {
        return imRelMemRepository.findByImRelIdListNotDeleted(imRelIdList);
    }

    public IMRelMem save(IMRelMem rel) {
        return imRelMemRepository.save(rel);
    }

    public void saveAll(List<IMRelMem> relMemList) {
        for (IMRelMem rel : relMemList) {
            int count = count(rel.getIm_rel_id(), rel.getMember_id());
            if (count == 0) {
                imRelMemRepository.save(rel);
            }
        }
//        imRelMemRepository.saveAll(relMemList);
    }

    int count(Long relId, Long memId) {
        return imRelMemRepository.countByIDS(relId, memId);
    }

    public Optional<IMRelMem> findByImRelIdMemId(Long relId, Long memId) {
        return imRelMemRepository.findByImRelIdMemId(relId, memId);
    }

    public List<Long> findMemIdListByImId(Long imId) {
        return imRelMemRepository.findMemIdListByImId(imId);
    }
}
