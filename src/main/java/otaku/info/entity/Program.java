package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    /** ","区切り、複数可 */
    @Column(nullable = true)
    private String team_id;

    /** ","区切り、複数可 */
    @Column(nullable = true)
    private String member_id;

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

    /**
     * チームID("n,n,n,n,n")をLongListにして返します。
     *
     * @return
     */
    public List<Long> getTeamIdList() {
        if (this.getTeam_id() != null && this.getTeam_id().contains(",")) {
            return List.of(this.getTeam_id().split(",")).stream().map(Integer::parseInt).collect(Collectors.toList()).stream().map(Integer::longValue).collect(Collectors.toList());
        } else if (StringUtils.hasText(this.getTeam_id())) {
            List<Long> teamIdList = new ArrayList<>();
            teamIdList.add((long)Integer.parseInt(this.getTeam_id()));
            return teamIdList;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * チームID("n,n,n,n,n")をLongListにして返します。
     *
     * @return
     */
    public List<Long> getMemberIdList() {
        if (this.getMember_id() != null && this.getMember_id().contains(",")) {
            return List.of(this.getMember_id().split(",")).stream().map(Integer::parseInt).collect(Collectors.toList()).stream().map(Integer::longValue).collect(Collectors.toList());
        } else if (StringUtils.hasText(this.getMember_id())) {
            List<Long> memberIdList = new ArrayList<>();
            memberIdList.add((long)Integer.parseInt(this.getMember_id()));
            return memberIdList;
        } else {
            return new ArrayList<>();
        }
    }
}
