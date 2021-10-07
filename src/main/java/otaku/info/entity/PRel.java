package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "p_rel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "p_rel")
public class PRel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long p_rel_id;

    @Column(nullable = false)
    private Long program_id;

    @Column(nullable = false)
    private Long team_id;

    @Column(nullable = true)
    private Long member_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
