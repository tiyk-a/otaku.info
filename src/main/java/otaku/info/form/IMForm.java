package otaku.info.form;

import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商品画像以外の商品データを登録するフォーム
 *
 * @author hasegawachiharu
 */
@Data
public class IMForm {

    private String url;

    private String title;

    private Long wp_id;

    private String item_caption;

    private Date publication_date;

    private Integer price;

//    private String image1;
//
//    private String image2;
//
//    private String image3;

    private boolean fct_chk;

    private boolean del_flg;
}