package otaku.info.searvice;

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
}
