package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue
    private Long tag_id;

    @Column(nullable = false)
    private String tag;

    @Column(nullable = true)
    private Long team_id;

    @Column(nullable = true)
    private Long member_id;

    @Column(nullable = true)
    private Long item_id;

    @Column(nullable = true)
    private Timestamp created_at;

    @Column(nullable = true)
    private Timestamp updated_at;

}
