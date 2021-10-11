package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.ItemRel;
import otaku.info.repository.ItemRelRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ItemRelService {

    @Autowired
    ItemRelRepository itemRelRepository;

    public List<ItemRel> findByItemId(Long itemId) {
        return itemRelRepository.findByItemId(itemId);
    }

    public List<Long> getTeamIdListByItemId(Long itemId) {
        return itemRelRepository.getTeamIdListByItemId(itemId);
    }

    public ItemRel save(ItemRel itemRel) {
        return itemRelRepository.save(itemRel);
    }

    public List<ItemRel> saveAll(List<ItemRel> itemRelList) {
        List<ItemRel> tmpList = removeExistRecord(itemRelList);
        return itemRelRepository.saveAll(tmpList);
    }

    public List<ItemRel> removeExistRecord(List<ItemRel> itemRelList) {
        List<ItemRel> returnList = new ArrayList<>();
        for (ItemRel ir : itemRelList) {
            if (ir.getMember_id() == null) {
                boolean exists = itemRelRepository.existsByElem(ir.getItem_id(), ir.getTeam_id()) > 0;
                if (!exists) {
                    returnList.add(ir);
                }
            } else {
                boolean exists = itemRelRepository.existsByElem(ir.getItem_id(), ir.getTeam_id(), ir.getMember_id()) > 0;
                if (!exists) {
                    returnList.add(ir);
                }
            }
        }
        return returnList;
    }

    public List<ItemRel> findByItemIdList(List<Long> itemIdList) {
        return itemRelRepository.findByItemIdList(itemIdList);
    }

    public List<ItemRel> findAll() {
        return itemRelRepository.findAll();
    }

    public ItemRel findByItemIdTeamIdMemberIdNull(Long itemId, Long teamId) {
        return itemRelRepository.findByItemIdTeamIdMemberIdNull(itemId, teamId).orElse(new ItemRel());
    }
}
