package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.RoomSampleData;

import java.util.List;

public interface RoomSampleDataRepository extends JpaRepository<RoomSampleData, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM room_sample_data WHERE data_id = ?1 and user_id = ?2")
    RoomSampleData findByDataId(String dataId, String userId);

    @Query(nativeQuery = true, value = "SELECT user_id FROM room_sample_data")
    List<String> findUserIdList();
}
