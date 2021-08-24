package otaku.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TvDto {

    public String station;

    public String title;

    public String description;

    public String on_air_date;

    public String keyword;
}
