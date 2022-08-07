package otaku.info.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.io.Serializable;

@Setter
@Getter
@Data
public class PMRelMemKey implements Serializable {

    @Column(name = "pm_rel_mem_id")
    private Long pm_rel_mem_id;

    @Column(name = "pm_rel_id")
    private Long pm_rel_id;

    @Column(name = "member_id")
    private Long member_id;
}
