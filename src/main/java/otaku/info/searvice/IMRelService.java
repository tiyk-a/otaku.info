package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.IMRel;
import otaku.info.repository.IMRelRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class IMRelService {

    @Autowired
    IMRelRepository iMRelRepository;

    public List<IMRel> findByItemMId(Long itemMId) {
        return iMRelRepository.findByItemMId(itemMId);
    }

    public List<IMRel> saveAll(List<IMRel> iMRelList) {
        List<IMRel> tmpList = removeExistRecord(iMRelList);
        return iMRelRepository.saveAll(tmpList);
    }

    public List<Long> findTeamIdListByItemMId(Long itemMId) {
        return iMRelRepository.findTeamIdListByItemMId(itemMId);
    }

    public List<Long> findMemberIdListByItemMId(Long itemMId) {
        return  iMRelRepository.findMemberIdListByItemMId(itemMId);
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
            if (rel.getMember_id() == null) {
                boolean exists = iMRelRepository.existsByElem(rel.getItem_m_id(), rel.getTeam_id()) > 0;
                if (!exists) {
                    returnList.add(rel);
                }
            } else {
                boolean exists = iMRelRepository.existsByElem(rel.getItem_m_id(), rel.getTeam_id(), rel.getMember_id()) > 0;
                if (!exists) {
                    returnList.add(rel);
                }
            }
        }
        return returnList;
    }

    public IMRel findByItemIdTeamIdMemberIdNull(Long iMId, Long teamId) {
        return iMRelRepository.findByItemIdTeamIdMemberIdNull(iMId, teamId).orElse(new IMRel());
    }
}
