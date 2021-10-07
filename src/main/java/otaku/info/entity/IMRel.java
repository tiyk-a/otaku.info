package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "im_rel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "im_rel")
public class IMRel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long im_rel_id;

    @Column(nullable = false)
    private Long item_m_id;

    @Column(nullable = false)
    private Long team_id;

    @Column(nullable = true)
    private Long member_id;

    @Column(nullable = true)
    private Long wp_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
