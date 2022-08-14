//package otaku.info.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import javax.persistence.*;
//import java.io.Serializable;
//import java.sql.Timestamp;
//
//@Entity(name = "pm_rel_mem")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@IdClass(value=PMRelMemKey.class)
//@Table(name = "pm_rel_mem")
//public class PMRelMem implements Serializable {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long pm_rel_mem_id;
//
//    @Id
//    @Column(nullable = false)
//    private Long pm_rel_id;
//
//    @Id
//    @Column(nullable = false)
//    private Long member_id;
//
//    @CreationTimestamp
//    @Column(nullable = true)
//    private Timestamp created_at;
//
//    @UpdateTimestamp
//    @Column(nullable = true)
//    private Timestamp updated_at;
//
//    @Column(columnDefinition = "Boolean default false")
//    private Boolean del_flg;
//}
