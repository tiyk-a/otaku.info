//package otaku.info.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.*;
//
///**
// * prel/prelmから1つのテーブルに移管
// *
// */
//@Entity(name = "p_cast")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "p_cast")
//public class PCast {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long p_cast_id;
//
//    @Column(nullable = false)
//    private Long program_id;
//
//    /** TeamIDもMemberIDもここに入る */
//    @Column(nullable = false)
//    private Long tm_id;
//
//    @Column(nullable = false)
//    private boolean del_flg;
//}
