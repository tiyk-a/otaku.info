package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.ItemRelation;
import otaku.info.repository.ItemRelationRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ItemRelationService {

    @Autowired
    ItemRelationRepository itemRelationRepository;

    public List<ItemRelation> findByItemId(Long itemId) {
        return itemRelationRepository.findByItemId(itemId);
    }

    public List<Long> getTeamIdListByItemId(Long itemId) {
        return itemRelationRepository.getTeamIdListByItemId(itemId);
    }

    public ItemRelation save(ItemRelation itemRelation) {
        return itemRelationRepository.save(itemRelation);
    }

    public List<ItemRelation> saveAll(List<ItemRelation> itemRelationList) {
        return itemRelationRepository.saveAll(itemRelationList);
    }

    public List<ItemRelation> removeExistRecord(List<ItemRelation> itemRelationList) {
        List<ItemRelation> returnList = new ArrayList<>();
        for (ItemRelation ir : itemRelationList) {
            boolean exists = itemRelationRepository.existsByElem(ir.getItem_id(), ir.getTeam_id(), ir.getMember_id()) > 0;
            if (!exists) {
                returnList.add(ir);
            }
        }
        return returnList;
    }

    public List<ItemRelation> findByItemIdList(List<Long> itemIdList) {
        return itemRelationRepository.findByItemIdList(itemIdList);
    }
}
