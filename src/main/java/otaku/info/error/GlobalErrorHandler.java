package otaku.info.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import otaku.info.controller.LineController;
import otaku.info.setting.Setting;

import java.util.Arrays;

@ControllerAdvice
@Component
public class GlobalErrorHandler {

    @Autowired
    Setting setting;

    @Autowired
    LineController lineController;

    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception e) {
        e.printStackTrace();
        if (setting.getTest() != null && !setting.getTest().equals("dev")) {
            lineController.post("😨Error:" + System.currentTimeMillis() + ":" + Arrays.toString(e.getStackTrace()).substring(0,200));
        }
    }
}
