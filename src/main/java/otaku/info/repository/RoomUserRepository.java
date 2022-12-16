package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.RoomUser;

import java.util.List;
import java.util.Optional;

public interface RoomUserRepository extends JpaRepository<RoomUser, Long> {

    @Query(nativeQuery = true, value = "SELECT username FROM room_user where user_id = ?1")
    Optional<String> findUserNameByUserId(String userId);

    @Query(nativeQuery = true, value = "select user_id from room_user where user_id in ?1")
    List<String> findUserIdListByUserId(List<String> userIdList);
}
