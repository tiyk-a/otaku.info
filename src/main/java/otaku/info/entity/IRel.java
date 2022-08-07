package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity(name = "i_rel")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(value=IRelKey.class)
@Table(name = "i_rel")
public class IRel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long i_rel_id;

    @Id
    @Column(nullable = false)
    private Long item_id;

    @Id
    @Column(nullable = false)
    private Long team_id;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
