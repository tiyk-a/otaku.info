package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 第２世代IM用
 * 商品versionを管理します
 *
 */
@Entity(name = "im_ver")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "im_ver")
public class ImVer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long im_v_id;

    @Column(nullable = false)
    private Long im_id;

    @Column(nullable = false)
    private String ver_name;

    @Column(nullable = true)
    private Integer sort_order;
}
