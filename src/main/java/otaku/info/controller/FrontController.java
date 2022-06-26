package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Front pageを表示するためのコントローラです
 *
 */
@Controller
@RequestMapping("/")
@AllArgsConstructor
public class FrontController {

    @GetMapping("/tmp")
    public void profile() {
        System.out.println("koko");
    }

    /**
     * Twitter一覧ページ
     */
    @GetMapping("/tw")
    public void getTwitter() {
//        System.out.println("koko");
    }
}
