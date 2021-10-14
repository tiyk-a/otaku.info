package otaku.info.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.io.Serializable;

@Setter
@Getter
@Data
public class IMRelKey implements Serializable {

    @Column(name = "im_rel_id")
    private Long im_rel_id;

    @Column(name = "item_m_id")
    private Long item_m_id;

    @Column(name = "team_id")
    private Long team_id;
}
