package otaku.info.searvice.db;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import otaku.info.entity.Item;
import otaku.info.repository.ItemRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<String> tmpMethod() {
        return itemRepository.tmpMethod();
    }

    public Long findItemId(String code) {
        return itemRepository.findItemIdByItemCode(code);
    }

    public void updateItem(Item item) {
        itemRepository.save(item);
    }
    public Item saveItem(Item item) {
        if (!hasData(item.getItem_code())) {
            return itemRepository.save(item);
        }
        return new Item();
    }

    public boolean hasData(String itemCode) {
        Long result = itemRepository.hasItemCode(itemCode);
        return result!=0;
    }

    public Optional<Item> findByItemId(Long itemId) {
        return itemRepository.findById(itemId);
    }
}
