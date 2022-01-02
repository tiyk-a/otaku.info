package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import otaku.info.form.IMForm;

import javax.persistence.*;
import java.util.Date;

/**
 * 商品の概要情報だけを持つ第２世代IM
 *
 */
@Entity(name = "im")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "im")
public class IM implements Comparable<IM> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long im_id;

    @Column(nullable = false)
    private String title;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private Date publication_date;

    /** htmlで入れる */
    @Column(nullable = true, columnDefinition="TEXT")
    private String amazon_image;

    @Column(nullable = false)
    private boolean del_flg;

    /** trueだとブログが更新されていないので更新する必要がある */
    @Column(columnDefinition = "Boolean default false")
    private boolean blogNotUpdated;

    public IM absorb(IMForm imForm) {
        if (imForm.getTitle() != null && !imForm.getTitle().isEmpty()) {
            this.setTitle(imForm.getTitle());
        }
        if (imForm.getPublication_date() != null) {
            this.setPublication_date(imForm.getPublication_date());
        }
        return this;
    }

    @Override
    public int compareTo(IM target) {
        if (this.getIm_id() < target.getIm_id()) {
            return -1;
        } else {
            return 1;
        }
    }
}
