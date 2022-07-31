package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Station;
import otaku.info.enums.StationEnum;
import otaku.info.repository.StationRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public Optional<Long> findStationId(String stationName) {
        Optional<Long> id = Arrays.stream(StationEnum.values()).filter(e -> e.getName().equals(stationName)).map(StationEnum::getId).findFirst();
        if (!id.isPresent()) {
            List<Long> idList = stationRepository.findStationId(stationName);
            if (idList.size() > 0) {
                id = Optional.ofNullable(idList.get(0));
            }
        }
        return id;
    }

    /**
     *
     * @param stationId
     * @return
     */
    public String getStationNameByEnumDB(Long stationId) {
        String result = "";
        result = Arrays.stream(StationEnum.values()).filter(e -> e.getId().equals(Math.toIntExact(stationId))).map(StationEnum::getName).findFirst().orElse("");
        if (result.equals("")) {
            Station station = stationRepository.findById(stationId).orElse(null);
            if (station != null) {
                result = station.getStation_name();
            }
        }
        return result;
    }

    /**
     * DBに登録します
     *
     * @param station
     * @return
     */
    public Station save(Station station) {
        return stationRepository.save(station);
    }

    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    /**
     * 引数stringからstationを検索します
     *
     * @param name
     * @return
     */
    public List<Station> findByName(String name) {
        List<Station> stationList = new ArrayList<>();

        // Enumから検索
        List<StationEnum> stationEnumList = Arrays.stream(StationEnum.values()).filter(e -> e.getName().contains(name) && e.getId() != null).collect(Collectors.toList());

        if (stationEnumList.size() > 0) {
            stationList.addAll(convertByEnum(stationEnumList));
            List<Long> idList = stationEnumList.stream().map(e -> e.getId()).collect(Collectors.toList());
            stationList.addAll(stationRepository.findByNameExceptIdList(name, idList));
        } else {
            stationList.addAll(stationRepository.findByName(name));
        }

        // 10件のみ返す
        return stationList.stream().filter(e -> e.getStation_id() != null).limit(10).collect(Collectors.toList());
    }

    public Station findById(Long stationId) {
        return stationRepository.findById(stationId).orElse(null);
    }

    private Station convertByEnum(StationEnum se) {
        Station s = new Station();
        s.setStation_name(se.getName());
        s.setStation_id(s.getStation_id());
        return s;
    }

    /**
     * リストで渡されてきたEnumをStationエンティティに変換します
     *
     * @param stationEnumList
     * @return
     */
    private List<Station> convertByEnum(List<StationEnum> stationEnumList) {
        return stationEnumList.stream().map(e -> convertByEnum(e)).collect(Collectors.toList());
    }
}
