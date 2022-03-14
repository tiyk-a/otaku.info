package otaku.info.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import otaku.info.entity.Message;
import otaku.info.repository.MessageRepository;

import javax.transaction.Transactional;

@Service
@Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn = Throwable.class)
@AllArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message save(String title) {
        Message m = new Message(null, title, null);
        return messageRepository.save(m);
    }

    public Message latest() {
        return messageRepository.getLatest().orElse(null);
    }

    /**
     * titleのアップデート描画が必要か確認する
     * 1なら必要、0は不要
     * @param title
     * @return
     */
    public Boolean checkLatestMessage(String title) {
        // 引数が最新1件のtitleと一緒（更新不要）だったら1が返ってくる
        return messageRepository.checkLatestMessage(title) != 1;
    }
}
