//package otaku.info.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import otaku.info.dto.MemberSearchDto;
//
//import java.sql.Timestamp;
//import java.util.Date;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Member {
//
//    private Long member_id;
//
//    private Long team_id;
//
//    private String member_name;
//
//    private String kana;
//
//    private String mnemonic;
//
//    private Date birthday;
//
//    private Timestamp created_at;
//
//    private Timestamp updated_at;
//
//    public MemberSearchDto convertToDto() {
//        MemberSearchDto dto = new MemberSearchDto();
//        dto.setMember_id(member_id);
//        dto.setTeam_id(team_id);
//        dto.setMember_name(member_name);
//        return dto;
//    }
//}
