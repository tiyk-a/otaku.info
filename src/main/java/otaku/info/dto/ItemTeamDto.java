package otaku.info.dto;

import lombok.Getter;
import lombok.Setter;
import otaku.info.entity.Item;

import java.util.List;

@Setter
@Getter
public class ItemTeamDto {

    private Item item;

    private List<Long> teamIdList;

}
