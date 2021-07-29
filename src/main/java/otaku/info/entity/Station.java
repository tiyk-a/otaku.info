package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.security.Timestamp;

@Entity(name = "station")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "station")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long station_id;

    @Column(nullable = true)
    public String station_name;

    @Column(nullable = false)
    public String keyword;

    @Column(nullable = true)
    public Timestamp created_at;

    @Column(nullable = true)
    public Timestamp updated_at;

}
