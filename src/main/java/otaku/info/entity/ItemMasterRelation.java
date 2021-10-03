package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "item_master_relation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_master_relation")
public class ItemMasterRelation {

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
