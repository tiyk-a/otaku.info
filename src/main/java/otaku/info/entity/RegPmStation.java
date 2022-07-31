package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * レギュラーPM放送局を管理する
 *
 */
@Entity(name = "reg_pm_station")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reg_pm_station")
public class RegPmStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reg_pm_station_id;

    @Column(nullable = false)
    private Long regular_pm_id;

    @Column(nullable = false)
    private Long station_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
