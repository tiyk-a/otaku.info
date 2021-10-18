package otaku.info.entity;

import javax.persistence.Column;
import java.io.Serializable;

public class PRelMemKey implements Serializable {

    @Column(name = "p_rel_mem_id")
    private Long p_rel_mem_id;

    @Column(name = "p_rel_id")
    private Long p_rel_id;

    @Column(name = "member_id")
    private Long member_id;
}
