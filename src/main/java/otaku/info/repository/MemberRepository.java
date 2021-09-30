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

    @Query("select a.member_id from Member as a where 1 < (select count(*) from Member as b where a.member_name = b.member_name and a.member_id <= b.member_id ) and a.member_name = ?1")
    Long getFstMemberId(String memberName);

    @Query("select member_id from Member as a where 1 < (select count(*) from Member as b where a.member_name = b.member_name and a.member_id >= b.member_id)")
    List<Long> getDupl();

    @Query("SELECT member_name FROM Member WHERE member_id in ?1")
    List<String> findMemberNameByIdList(List<Long> memberIdList);

    @Query("select team_id from Member where member_id in ?1")
    List<Long> findTeamIdListByMemberIdList(List<Long> memberIdList);
}
