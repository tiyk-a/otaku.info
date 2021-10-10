package otaku.info.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import otaku.info.controller.LineController;

import java.io.IOException;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Autowired
    LineController lineController;

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        System.out.println(httpResponse.getStatusCode());
        System.out.println(httpResponse.getHeaders());
        System.out.println(httpResponse.getBody());
        lineController.post("RestErrorHandlerMine:hasError:" + httpResponse.getStatusCode() + "\n" + httpResponse.getBody().toString().substring(0, 200));
        return (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR || httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {

//        if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
//            // handle SERVER_ERROR
//            System.out.println(httpResponse.getBody());
//        } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
//            // handle CLIENT_ERROR
//            if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
//                System.out.println(httpResponse.getBody());
//            }
//        }
        System.out.println(httpResponse.getStatusCode());
        System.out.println(httpResponse.getHeaders());
        System.out.println(httpResponse.getBody());
        lineController.post("RestErrorHandlerMine:handleError:" + httpResponse.getStatusCode() + "\n" + httpResponse.getBody().toString().substring(0, 200));
    }
}