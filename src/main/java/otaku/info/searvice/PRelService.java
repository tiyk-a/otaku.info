package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.PRel;
import otaku.info.repository.PRelRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class PRelService {

    @Autowired
    PRelRepository pRelRepository;

    public List<PRel> getListByProgramId(Long programId) {
        List<PRel> relList = pRelRepository.findAllByProgramId(programId);
        if (relList == null) {
            relList = new ArrayList<>();
        }
        return relList;
    }

    /**
     * チームID("n,n,n,n,n")をLongListにして返します。
     *
     * @return
     */
    public List<Long> getTeamIdList(Long programId) {
        List<Long> teamIdList = pRelRepository.findTeamIdListByProgramId(programId);
        if (teamIdList == null) {
            teamIdList = new ArrayList<>();
        }
        return teamIdList;
    }

    /**
     * チームID("n,n,n,n,n")をLongListにして返します。
     *
     * @return
     */
    public List<Long> getMemberIdList(Long programId) {
        List<Long> teamIdList = pRelRepository.findMemberIdListByProgramId(programId);
        if (teamIdList == null) {
            teamIdList = new ArrayList<>();
        }
        return teamIdList;
    }

    public PRel save(PRel rel) {
        return pRelRepository.save(rel);
    }

    public List<PRel> saveAll(List<PRel> relList) {
        List<PRel> tmpList = removeExistRecord(relList);
        return pRelRepository.saveAll(tmpList);
    }

    public List<PRel> findAll() {
        return pRelRepository.findAll();
    }

    public List<PRel> removeExistRecord(List<PRel> imRelList) {
        List<PRel> returnList = new ArrayList<>();
        for (PRel rel : imRelList) {
            if (rel.getMember_id() == null) {
                boolean exists = pRelRepository.existsByElem(rel.getProgram_id(), rel.getTeam_id()) > 0;
                if (!exists) {
                    returnList.add(rel);
                }
            } else {
                boolean exists = pRelRepository.existsByElem(rel.getProgram_id(), rel.getTeam_id(), rel.getMember_id()) > 0;
                if (!exists) {
                    returnList.add(rel);
                }
            }
        }
        return returnList;
    }

    public PRel findByItemIdTeamIdMemberIdNull(Long programId, Long teamId) {
        return pRelRepository.findByItemIdTeamIdMemberIdNull(programId, teamId).orElse(null);
    }
}
