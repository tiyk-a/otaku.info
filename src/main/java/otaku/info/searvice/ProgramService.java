package otaku.info.searvice;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Program;
import otaku.info.repository.ProgramRepository;

import javax.transaction.Transactional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class ProgramService  {

    private final ProgramRepository programRepository;

    public Program save(Program program) {
        return programRepository.save(program);
    }

    public boolean hasProgramCode(String code) {
        Long result = programRepository.hasProgramCode(code);
        return result!=0;
    }
}
