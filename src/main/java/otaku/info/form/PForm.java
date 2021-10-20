package otaku.info.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PForm {

    /** 商品タイトル */
    @NotBlank(message = "商品タイトルを記入してください")
    @Size(max = 100, message = "商品タイトルは最大{max}文字までで記入してください")
    private String title;

    /** 価格 */
    @Min(value = 1, message = "{value}円以上の価格を数字で記入してください")
    private int price;

    /** 説明文 */
    @NotBlank(message = "説明文を記入してください")
    @Size(max = 500, message = "説明文は{max}文字以内で記入してください")
    private String description;
}
