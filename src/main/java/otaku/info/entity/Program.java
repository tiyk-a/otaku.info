package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import otaku.info.form.PForm;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity(name = "program")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "program")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long program_id;

    @Column(nullable = false)
    private Long station_id;

    @Column(nullable = true)
    private String title;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private LocalDateTime on_air_date;

    @Column(nullable = false)
    private boolean fct_chk;

    @Column(nullable = false)
    private boolean del_flg;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    @Column(nullable = true)
    private Long pm_id;

    public Program absorb(PForm form) {
        this.setTitle(form.getTitle());
        this.setDescription(form.getDescription());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(form.getOn_air_date(), formatter);
        this.setOn_air_date(dateTime);
        return this;
    }
}
