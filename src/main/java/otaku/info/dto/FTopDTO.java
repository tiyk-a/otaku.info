package otaku.info.dto;

import lombok.Getter;
import lombok.Setter;
import otaku.info.entity.Item;

import java.util.List;

@Setter
@Getter
public class FTopDTO {

    private List<Item> i;

    private List<FIMDto> im;

    private List<Item> iim;
}