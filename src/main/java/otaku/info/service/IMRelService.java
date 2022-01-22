package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.IMRel;
import otaku.info.repository.IMRelRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class IMRelService {

    @Autowired
    IMRelRepository iMRelRepository;

    public IMRel findByImRelId(Long imRelId) {
        return iMRelRepository.findByImRelId(imRelId);
    }
    public List<IMRel> findByItemMId(Long itemMId) {
        return iMRelRepository.findByItemMId(itemMId);
    }

    public List<Long> findTeamIdByItemMId(Long itemMId) {
        return iMRelRepository.findTeamIdByItemMId(itemMId);
    }

    public List<IMRel> saveAll(List<IMRel> iMRelList) {
        List<IMRel> tmpList = removeExistRecord(iMRelList);
        return iMRelRepository.saveAll(tmpList);
    }

    public List<Long> findTeamIdListByItemMId(Long itemMId) {
        return iMRelRepository.findTeamIdListByItemMId(itemMId);
    }

    public Long getWpIdByItemMId(Long itemMId) {
        return iMRelRepository.getWpIdByItemMId(itemMId);
    }

    public List<IMRel> findAll() {
        return iMRelRepository.findAll();
    }

    public List<IMRel> removeExistRecord(List<IMRel> imRelList) {
        List<IMRel> returnList = new ArrayList<>();
        for (IMRel rel : imRelList) {
            boolean exists = iMRelRepository.existsByElem(rel.getIm_id(), rel.getTeam_id()) > 0;
            if (!exists) {
                returnList.add(rel);
            }
        }
        return returnList;
    }

    public IMRel save(IMRel rel) {
        return iMRelRepository.save(rel);
    }

    public Optional<IMRel> findByImIdTeamId(Long imId, Long teamId) {
        return iMRelRepository.findByImIdTeamId(imId, teamId);
    }

    public void removeAll(List<IMRel> relList) {
        iMRelRepository.deleteAll(relList);
    }

    public List<IMRel> findByItemIdTeamIdNotNull(Long imId) {
        return iMRelRepository.findByItemIdTeamIdNotNull(imId);
    }

    public List<IMRel> findAllWpIdNotNull() {
        return iMRelRepository.findAllWpIdNotNull();
    }

    public List<IMRel> findbyWpIdTeamId(Long wpId, Long teamId) {
        return iMRelRepository.findbyWpIdTeamId(wpId, teamId);
    }

    public List<IMRel> findByWpIdNullPublicationDateFuture(Date today) {
        return iMRelRepository.findByWpIdNullPublicationDateFuture(today);
    }

    public IMRel findByItemMIdTeamId(Long imId, Long teamId) {
        return iMRelRepository.findByItemMIdTeamId(imId, teamId);
    }
}
