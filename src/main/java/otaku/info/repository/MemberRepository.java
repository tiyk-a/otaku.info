package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import otaku.info.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT member_name FROM Member")
    List<String> getAllMemberNameList();

    @Query("SELECT member_id FROM Member WHERE member_name = ?1")
    Long findMemberIdByMemberName(String memberName);

    @Query("SELECT mnemonic FROM Member WHERE member_name = ?1")
    String getMnemonic(String member);

    @Query("SELECT member_name FROM Member WHERE member_id = ?1")
    String getMemberName(Long memberId);

    @Query("SELECT member_name FROM Member WHERE member_id in ?1")
    List<String> getMemberNameList(List<Long> memberIdList);
}
