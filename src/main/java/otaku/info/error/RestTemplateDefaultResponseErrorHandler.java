package otaku.info.error;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;


import java.io.IOException;

@ControllerAdvice
public class RestTemplateDefaultResponseErrorHandler extends DefaultResponseErrorHandler {

    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class, RestClientException.class})
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        //Don't throw Exception.
    }
}
