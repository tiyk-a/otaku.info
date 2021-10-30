package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import otaku.info.entity.ImVer;
import otaku.info.repository.ImVerRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ImVerService {

    @Autowired
    ImVerRepository imVerRepository;

    public ImVer save(ImVer ver) {
        return imVerRepository.save(ver);
    }

    public boolean existtVerNameImId(String verName, Long teamId) {
        return imVerRepository.existtVerNameImId(verName, teamId) > 0;
    }

    public List<ImVer> findByImId(Long imId) {
        return imVerRepository.findByImId(imId);
    }
}
