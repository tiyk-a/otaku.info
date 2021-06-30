package otaku.info.searvice.db;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import otaku.info.entity.Item;
import otaku.info.repository.ItemRepository;

import javax.transaction.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public boolean saveItem(Item item) {
        if (!hasData(item.getItem_code())) {
            itemRepository.save(item);
        }
        return true;
    }

    public boolean hasData(String itemCode) {
        Long result = itemRepository.hasItemCode(itemCode);
        return result!=0;
    }
}
