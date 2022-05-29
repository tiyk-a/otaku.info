package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity(name = "pm_ver")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pm_ver")
public class PMVer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pm_v_id;

    @Column(nullable = false)
    private Long pm_id;

    @Column(nullable = true)
    private LocalDateTime on_air_date;

    @Column(nullable = false)
    private Long station_id;

    /** 論理抹消 */
    @Column(columnDefinition = "Boolean default false")
    private Boolean del_flg;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
