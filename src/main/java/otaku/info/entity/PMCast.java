//package otaku.info.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.*;
//
///**
// * pmrel/pmrelmから1つのテーブルに移管
// *
// */
//@Entity(name = "pm_cast")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "pm_cast")
//public class PMCast {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long pm_cast_id;
//
//    @Column(nullable = false)
//    private Long pm_id;
//
//    /** TeamIDもMemberIDもここに入る */
//    @Column(nullable = false)
//    private Long tm_id;
//
//    @Column(nullable = false)
//    private boolean del_flg;
//}
