package otaku.info.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 楽天ROOM、参考にしたいユーザーのデータ
 * 汎用的にしようか。
 */
@Entity(name = "room_sample_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "room_sample_data")
public class RoomSampleData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 何に関するデータなのか、ENUMで用意し文字列で入れようと思ってる
     */
    @Column(nullable = false)
    private String data_id;

    @Column(nullable = false)
    private String user_id;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String data1;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String data2;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String data3;

    @CreationTimestamp
    @Column(nullable = true)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column(nullable = true)
    private Timestamp updated_at;
}
