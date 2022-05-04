package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * グループのテーブル、商品検索に使う
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    private Long team_id;

    private String team_name;

    private String kana;

    private String mnemonic;

    private String anniversary;

    private String tw_id;

    private Timestamp created_at;

    private Timestamp updated_at;
}
