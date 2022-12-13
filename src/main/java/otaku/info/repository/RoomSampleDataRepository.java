package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.RoomSampleData;

public interface RoomSampleDataRepository extends JpaRepository<RoomSampleData, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM room_sample_data WHERE data_id = ?1 and user_id = ?2")
    RoomSampleData findByDataId(String dataId, String userId);
}
