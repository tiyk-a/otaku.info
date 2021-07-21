package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT a.member_id FROM Member a WHERE ?1 LIKE CONCAT('%', a.member_name, '%')")
    List<Long> findMemberIdByText(String text);
}
