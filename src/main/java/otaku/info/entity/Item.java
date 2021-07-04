package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity(name = "Item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Item")
public class Item {

    @Id
    @GeneratedValue
    private Long item_id;

    @Column(nullable = false)
    private int site_id;

    @Column(nullable = false)
    private String item_code;

    @Column(nullable = true)
    private String url;

    @Column(nullable = true)
    private int price;

    @Column(nullable = false)
    private int team_id;

    @Column(nullable = true)
    private int artist_id;

    @Column(nullable = true)
    private String title;

    @Column(nullable = false)
    private String item_caption;

    @Column(nullable = true)
    private Date publication_date;

    @Column(nullable = true)
    private Timestamp created_at;

    @Column(nullable = true)
    private Timestamp updated_at;
}
