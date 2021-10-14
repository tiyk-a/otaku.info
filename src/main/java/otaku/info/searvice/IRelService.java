package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.IRel;
import otaku.info.repository.IRelRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class IRelService {

    @Autowired
    IRelRepository iRelRepository;

    public List<IRel> findByItemId(Long itemId) {
        return iRelRepository.findByItemId(itemId);
    }

    public List<Long> getTeamIdListByItemId(Long itemId) {
        return iRelRepository.getTeamIdListByItemId(itemId);
    }

    public IRel save(IRel iRel) {
        return iRelRepository.save(iRel);
    }

    public List<IRel> saveAll(List<IRel> iRelList) {
        List<IRel> tmpList = removeExistRecord(iRelList);
        return iRelRepository.saveAll(tmpList);
    }

    public List<IRel> removeExistRecord(List<IRel> iRelList) {
        List<IRel> returnList = new ArrayList<>();
        for (IRel ir : iRelList) {
//            if (ir.getMember_id() == null) {
                boolean exists = iRelRepository.existsByElem(ir.getItem_id(), ir.getTeam_id()) > 0;
                if (!exists) {
                    returnList.add(ir);
                }
//            } else {
//                boolean exists = iRelRepository.existsByElem(ir.getItem_id(), ir.getTeam_id(), ir.getMember_id()) > 0;
//                if (!exists) {
//                    returnList.add(ir);
//                }
//            }
        }
        return returnList;
    }

    public List<IRel> findByItemIdList(List<Long> itemIdList) {
        return iRelRepository.findByItemIdList(itemIdList);
    }

    public List<IRel> findAll() {
        return iRelRepository.findAll();
    }

    public IRel findByItemIdTeamIdMemberIdNull(Long itemId, Long teamId) {
        return iRelRepository.findByItemIdTeamIdMemberIdNull(itemId, teamId).orElse(new IRel());
    }

    public void removeAll(List<IRel> relList) {
        iRelRepository.deleteAll(relList);
    }

    public List<IRel> findByItemIdTeamIdNotNull(Long itemId) {
        return iRelRepository.findByItemIdTeamIdNotNull(itemId);
    }
}
