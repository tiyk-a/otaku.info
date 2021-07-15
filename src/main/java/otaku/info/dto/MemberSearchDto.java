package otaku.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSearchDto {

    private Long team_id;

    private Long member_id;

    private String member_name;

    private String team_name;
}
