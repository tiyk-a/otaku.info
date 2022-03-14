package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import otaku.info.dto.YTDto;
import otaku.info.entity.Message;
import otaku.info.service.MessageService;

import javax.validation.Valid;

/**
 * otakuinfoと関係なし
 * Youtube実験用のエンティティ
 *
 */
@RestController
@RequestMapping("/youtube")
@AllArgsConstructor
public class YoutubeController {

    @Autowired
    MessageService messageService;

    /**
     *
     * @param title
     * @return
     */
    @GetMapping("/add")
    public ResponseEntity<Boolean> insertMessage(@Valid @RequestParam String title) {
        messageService.save(title);

        return ResponseEntity.ok(true);
    }

    @GetMapping("/")
    public ResponseEntity<String> latestOne() {
        Message m = messageService.latest();
        return ResponseEntity.ok(m.getTitle());
    }

    @GetMapping("/check")
    public ResponseEntity<YTDto> updateMessage(@Valid @RequestParam String title) {
        Boolean updateFlg = messageService.checkLatestMessage(title);
        YTDto dto = new YTDto();

        if(updateFlg) {
            dto.setUpdateFlg(true);
            String msg = messageService.latest().getTitle();
            dto.setTitle(msg);
        } else {
            dto.setUpdateFlg(false);
        }

        return ResponseEntity.ok(dto);
    }
}
