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

    private List<PRel> pRelList;

    private List<PRelMem> pRelMList;

    private List<Long> teamIdList;
}
