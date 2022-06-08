package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * PMカレンダー
 * 各PM_VERが各pm_relにカレンダーイベントを持たないといけないため
 * N:Nの関係をもつテーブル
 *
 */
@Entity(name = "pm_cal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pm_cal")
public class PMCal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pm_cal_id;

    @Column(nullable = false)
    private Long pm_ver_id;

    @Column(nullable = false)
    private Long pm_rel_id;

    @Column(nullable = false)
    private boolean del_flg;

    /** Google Calendar Idって名前だけどeventIdが入ってるよ */
    @Column(nullable = true)
    private String calendar_id;

    @Column(nullable = false)
    private boolean cal_active_flg;
}
