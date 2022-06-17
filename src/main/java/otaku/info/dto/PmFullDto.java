package otaku.info.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * PMのデータをフルで取得する
 */
@Data
@AllArgsConstructor
public class PmFullDto {

    private BigInteger pmId;

    private String title;

    private String description;

    /** pmVerから */
    private LocalDateTime onAirDate;

    /** pmVerから */
    private BigInteger stationId;

    public PmFullDto(Object[] o) {

        Timestamp timestamp = (Timestamp) o[3];

        this.pmId = (BigInteger) o[0];
        this.title = (String) o[1];
        this.description = (String) o[2];
        this.onAirDate = timestamp.toLocalDateTime();
        this.stationId = (BigInteger) o[4];
    }
}
