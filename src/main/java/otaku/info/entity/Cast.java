package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Regular_PMにひもづくキャスト
 * 1テーブルでTeamもMemberも扱う
 *
 */
@Entity(name = "cast")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cast")
public class Cast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cast_id;

    @Column(nullable = false)
    private Long regular_pm_id;

    /** TeamIDもMemberIDもここに入る */
    @Column(nullable = false)
    private Long tm_id;

    @Column(nullable = false)
    private boolean del_flg;
}
