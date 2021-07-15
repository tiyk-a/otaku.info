package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import otaku.info.dto.MemberSearchDto;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity(name = "Member")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long member_id;

    @Column(nullable = false)
    private Long team_id;

    @Column(nullable = true)
    private String member_name;

    @Column(nullable = true)
    private String kana;

    @Column(nullable = true)
    private String mnemonic;

    @Column(nullable = true)
    private Date birthday;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;

    public MemberSearchDto convertToDto() {
        MemberSearchDto dto = new MemberSearchDto();
        dto.setMember_id(member_id);
        dto.setTeam_id(team_id);
        dto.setMember_name(member_name);
        return dto;
    }
}
