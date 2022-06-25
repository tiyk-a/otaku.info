package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@AllArgsConstructor
public class FrontController {

    @GetMapping("/tmp")
    public void profile() {
        System.out.println("koko");
    }
}
