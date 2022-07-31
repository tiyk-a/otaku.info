package otaku.info.dto;

import lombok.Data;
import otaku.info.entity.PRel;
import otaku.info.entity.PRelMem;
import otaku.info.entity.Program;

import java.util.List;

/**
 * プログラムのDTO
 * P_relを合わせて返します
 */
@Data
public class PDto {

    private Program program;

    private String station_name;

    private List<PRel> pRelList;

    private List<PRelMem> pRelMList;

    /** 関連ありそうなpmを取ってくる"on_air_date title description"を3件くらい */
    private List<String> relPmList;
}
