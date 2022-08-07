package otaku.info.dto;

import lombok.Data;
import otaku.info.entity.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * Program Masterをフロントに持っていくためのDTO
 *
 */
@Data
public class PMDto {

    private PM pm;

    private List<PMVerDto> verList;

    private Timestamp created_at;

    private Timestamp updated_at;
}
