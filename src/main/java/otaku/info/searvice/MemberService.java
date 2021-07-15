package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
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
}
