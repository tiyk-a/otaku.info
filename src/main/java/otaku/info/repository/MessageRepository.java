package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Message;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(nativeQuery = true, value = "select a.* from message a order by created_at desc limit 1")
    Optional<Message> getLatest();

    /**
     * 引数と最新1件のtitleが同じ値だったら1、違ったら0が返る
     * @param title
     * @return
     */
    @Query(nativeQuery = true, value = "select a.title = ?1 from message a order by created_at desc limit 1")
    int checkLatestMessage(String title);
}
