package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import otaku.info.dto.TeamIdMemberNameDto;
import otaku.info.entity.Member;
import otaku.info.repository.MemberRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public List<Member> findAllMember() {
        return memberRepository.findAll();
    }

    /**
     * 引数のStringの中に含まれているメンバー名をリストにして返却します。
     *
     * @param text
     * @return
     */
    public List<Long> findMemberIdByText(String text) {
        List<String> memberNameList = memberRepository.getAllMemberNameList();
        List<Long> resultList = new ArrayList<>();
        for (String memberName : memberNameList) {
            if (text.contains(memberName) || text.contains(memberName.replace(" ", ""))) {
                resultList.add(memberRepository.findMemberIdByMemberName(memberName));
            }
        }
        return resultList;
    }

    /**
     * TeamIdとMemberNameだけのDTOリストを返却します。
     *
     * @param memberId
     * @return
     */
    public TeamIdMemberNameDto getMapTeamIdMemberName(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(new Member());
        TeamIdMemberNameDto dto = new TeamIdMemberNameDto();
        BeanUtils.copyProperties(member, dto);
        return dto;
    }

    public String getMnemonic(String memberName) {
        return memberRepository.getMnemonic(memberName);
    }

    public String getMemberName(Long memberId) {
        return memberRepository.getMemberName(memberId);
    }

    public List<String> getMemberNameList(List<Long> memberIdList) {
        return memberRepository.getMemberNameList(memberIdList);
    }

    public List<Long> getDupl() {
        return memberRepository.getDupl();
    }
}
