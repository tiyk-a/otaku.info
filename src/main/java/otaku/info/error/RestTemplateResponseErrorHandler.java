package otaku.info.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        System.out.println(httpResponse.getStatusCode());
        System.out.println(httpResponse.getHeaders());
        System.out.println(httpResponse.getBody());
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
    }
}