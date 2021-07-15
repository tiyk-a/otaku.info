package otaku.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import otaku.info.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
