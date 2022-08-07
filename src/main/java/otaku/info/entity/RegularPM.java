package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 定期的な番組のデータ。連ドラ名とか毎週あるTV番組の名前
 *
 */
@Entity(name = "regular_pm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "regular_pm")
public class RegularPM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long regular_pm_id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private LocalDateTime start_date;

    @Column(nullable = true)
    private LocalDateTime end_date;

    @Column(nullable = true)
    private String teamArr;

    @Column(nullable = true)
    private String memArr;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
