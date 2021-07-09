package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "Team")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Team")
public class Team {

    @Id
    @GeneratedValue
    private Long team_id;

    @Column(nullable = true)
    private String team_name;

    @Column(nullable = true)
    private String kana;

    @Column(nullable = true)
    private String mnemonic;

    @Column(nullable = true)
    private String anniversary;

    @Column(nullable = true)
    private Timestamp created_at;

    @Column(nullable = true)
    private Timestamp updated_at;
}
