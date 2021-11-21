package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.ErrorJson;
import otaku.info.repository.ErrorJsonRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ErrorJsonService {

    @Autowired
    ErrorJsonRepository errorJsonRepository;

    public ErrorJson save(ErrorJson j) {
        return errorJsonRepository.save(j);
    }

    public boolean isExists(String json) {
        return errorJsonRepository.findByJson(json) != null;
    }

    public List<ErrorJson> isNotSolved() {
        return errorJsonRepository.isNotSolved();
    }

    public List<ErrorJson> findByTeamIdNotSolved(Long teamId) {
        return errorJsonRepository.findByTeamIdNotSolved(teamId);
    }
}
