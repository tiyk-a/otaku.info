package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import otaku.info.dto.TeamIdMemberNameDto;
import otaku.info.entity.Member;
import otaku.info.repository.MemberRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public List<Member> findAllMember() {
        return memberRepository.findAll();
    }

    public List<Long> findMemberIdByText(String text) {
        return memberRepository.findMemberIdByText(text);
    }

    public TeamIdMemberNameDto getMapTeamIdMemberName(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(new Member());
        TeamIdMemberNameDto dto = new TeamIdMemberNameDto();
        BeanUtils.copyProperties(member, dto);
        return dto;
    }

}
