package otaku.info.form;

import lombok.Data;
import otaku.info.entity.Item;

/**
 * ErrorJsonからItemを新規登録する場合に、ErrorJsonを一緒にsolvedにするためにItemとErrorJsonのIDを渡せるフォーム
 *
 */
@Data
public class ItemByJsonForm {

    private Item item;

    private Long jsonId;
}
