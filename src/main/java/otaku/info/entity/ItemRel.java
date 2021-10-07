package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "item_rel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_rel")
public class ItemRel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long i_rel_id;

    @Column(nullable = false)
    private Long item_id;

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
