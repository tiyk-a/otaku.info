package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.apache.lucene.analysis.miscellaneous.PrefixAndSuffixAwareTokenFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import otaku.info.dto.TeamIdMemberNameDto;
import otaku.info.entity.Member;
import otaku.info.enums.MemberEnum;
import otaku.info.enums.TeamEnum;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class MemberService {

    /**
     * 全メンバーのデータを返します。
     *
     * @return
     */
    public List<Member> findAllMember() {
        return Arrays.stream(MemberEnum.values()).map(MemberEnum::convertToEntity).collect(Collectors.toList());
    }

    /**
     * 全チームデータのチーム名のみを返します。
     *
     * @return
     */
    public List<String> findAllMemberName() {
        return Arrays.stream(MemberEnum.values()).map(MemberEnum::getName).collect(Collectors.toList());
    }

    /**
     * 引数のStringの中に含まれているメンバー名をリストにして返却します。
     *
     * @param text
     * @return
     */
    public List<Long> findMemberIdByText(String text) {
        if (!StringUtils.hasText(text)) {
            return new ArrayList<>();
        }
        List<String> memberNameList = findAllMemberName();
        return memberNameList.stream().filter(e -> text.contains(e) || text.contains(e.replace(" ", ""))).map(e -> (long) MemberEnum.get(e).getId()).collect(Collectors.toList());
    }

    /**
     * TeamIdとMemberNameだけのDTOリストを返却します。
     *
     * @param memberId
     * @return
     */
    public TeamIdMemberNameDto getMapTeamIdMemberName(Long memberId) {
        Member member = MemberEnum.get(Math.toIntExact(memberId)).convertToEntity();
        TeamIdMemberNameDto dto = new TeamIdMemberNameDto();
        BeanUtils.copyProperties(member, dto);
        return dto;
    }

    /**
     * メンバー名からニックネームを返します。
     *
     * @param memberName
     * @return
     */
    public String getMnemonic(String memberName) {
        return MemberEnum.get(memberName).getMnemonic();
    }

    /**
     * メンバーIDからメンバー名を返します。
     *
     * @param memberId
     * @return
     */
    public String getMemberName(Long memberId) {
        return MemberEnum.get(Math.toIntExact(memberId)).getName();
    }

    /**
     * 引数のIDリストからメンバー名リストを返します。
     *
     * @param memberIdList
     * @return
     */
    public List<String> getMemberNameList(List<Long> memberIdList) {
        return Arrays.stream(MemberEnum.values()).filter(e -> memberIdList.stream().anyMatch(f -> f.equals((long) e.getId()))).map(MemberEnum::getName).collect(Collectors.toList());
    }

    /**
     * 引数のメンバーIDから、そのチームIDを返却します。重複あり。
     * @param memberIdList
     * @return
     */
    public List<Long> findTeamIdListByMemberIdList(List<Long> memberIdList) {
        return Arrays.stream(MemberEnum.values()).filter(e -> memberIdList.stream().anyMatch(f -> f.equals((long) e.getId()))).map(e -> (long) e.getId()).collect(Collectors.toList());
    }
}
