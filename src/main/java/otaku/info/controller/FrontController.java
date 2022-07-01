package otaku.info.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import otaku.info.dto.*;
import otaku.info.entity.*;
import otaku.info.setting.Log4jUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Front pageを表示するためのコントローラです
 *
 */
@Controller
@RequestMapping("/")
@AllArgsConstructor
public class FrontController {

    final Logger logger = Log4jUtils.newConsoleCsvAllLogger("FrontController");

    @Autowired
    ApiController apiController;

    @GetMapping("/item/{id}")
    public String getItemTeam(@PathVariable("id") Long teamId, Model model) {
        ResponseEntity<List<Item>> res = apiController.getItemTeam(teamId);
        model.addAttribute("test", "testdayo");
        model.addAttribute("itemList", res.getBody());
        return "item";
    }

    @GetMapping("/tv/{id}")
    public String tv(@PathVariable("id") Long teamId, Model model) {
        model.addAttribute("test", "testdayo");
        ResponseEntity<PAllDto> res = apiController.tvAll(teamId);
        model.addAttribute("pAllDto", res.getBody());
        return "tv";
    }

    /**
     * Twitter一覧ページ
     */
    @GetMapping("/tw")
    public void getTwitter() {
    }
}
