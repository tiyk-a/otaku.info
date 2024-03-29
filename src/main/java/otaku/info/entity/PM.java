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
 * PM (Program Master)
 * Programをフロントで確認して、作る
 *
 */
@Entity(name = "pm")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pm")
public class PM implements Comparable<PM> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pm_id;

    @Column(nullable = true)
    private String title;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private boolean del_flg;

    @Column(nullable = true)
    private String teamArr;

    @Column(nullable = true)
    private String memArr;

    @Column(nullable = true)
    private LocalDateTime on_air_date;

    @Column(nullable = true)
    private String stationArr;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    @Override
    public int compareTo(PM o) {
        return 0;
    }
}
