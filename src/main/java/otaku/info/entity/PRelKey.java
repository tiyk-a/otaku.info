package otaku.info.entity;

import javax.persistence.Column;
import java.io.Serializable;

public class PRelKey implements Serializable {

    @Column(name = "p_rel_id")
    private Long p_rel_id;

    @Column(name = "program_id")
    private Long program_id;

    @Column(name = "team_id")
    private Long team_id;
}
