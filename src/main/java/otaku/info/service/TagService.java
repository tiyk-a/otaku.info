package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.repository.TagRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class TagService {

    @Autowired
    TagRepository tagRepository;

    public List<String> getTagByTeam(Long teamId) {
        return tagRepository.findbyTeamId(teamId);
    }

    // メンバー名リストから該当タグを取得します。
    public List<String> getTagByMemberNameList(List<String> memberNameList) {
        for (String memberName : memberNameList) {
            // もしメンバー名にスペースが入っていたらタグ検索用にスペースを切り取り、リストに入れ直す。
            if (memberName.contains(" ")){
                memberNameList.set(memberNameList.indexOf(memberName), memberName.replace(" ", ""));
            }
        }
        return tagRepository.findByMemberNameList(memberNameList);
    }
}
