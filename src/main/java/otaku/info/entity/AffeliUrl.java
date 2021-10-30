package otaku.info.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "affeli_url")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "affeli_url")
public class AffeliUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long affeli_url_id;

    @Column(nullable = false)
    @NonNull
    private Long im_id;

    @Column(nullable = false)
    @NonNull
    private String url;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
