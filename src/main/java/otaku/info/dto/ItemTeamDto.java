package otaku.info.dto;

import lombok.Getter;
import lombok.Setter;
import otaku.info.entity.IRel;
import otaku.info.entity.IRelMem;
import otaku.info.entity.Item;

import java.util.List;

@Setter
@Getter
public class ItemTeamDto {

    private Item item;

    private List<IRel> relList;

    private List<Long> teamIdList;

    protected List<IRelMem> relMemList;

    private List<Long> memIdList;
}
