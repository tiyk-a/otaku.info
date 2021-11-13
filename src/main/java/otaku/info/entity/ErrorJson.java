package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "error_json")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "error_json")
public class ErrorJson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long errj_id;

    @Column(nullable = false)
    private String json;

    @Column(nullable = false)
    private boolean is_solved;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
