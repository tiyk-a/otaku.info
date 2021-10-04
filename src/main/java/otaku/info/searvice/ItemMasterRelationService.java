package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.ItemMasterRelation;
import otaku.info.repository.ItemMasterRelationRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ItemMasterRelationService {

    @Autowired
    ItemMasterRelationRepository itemMasterRelationRepository;

    public List<ItemMasterRelation> findByItemMId(Long itemMId) {
        return itemMasterRelationRepository.findByItemMId(itemMId);
    }

    public List<ItemMasterRelation> saveAll(List<ItemMasterRelation> itemMasterRelationList) {
        return itemMasterRelationRepository.saveAll(itemMasterRelationList);
    }

    public List<Long> findTeamIdListByItemMId(Long itemMId) {
        return itemMasterRelationRepository.findTeamIdListByItemMId(itemMId);
    }

    public List<Long> findMemberIdListByItemMId(Long itemMId) {
        return  itemMasterRelationRepository.findMemberIdListByItemMId(itemMId);
    }

    public Long getWpIdByItemMId(Long itemMId) {
        return itemMasterRelationRepository.getWpIdByItemMId(itemMId);
    }
}
