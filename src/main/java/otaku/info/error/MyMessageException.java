package otaku.info.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyMessageException extends Exception {

    private String keyName;
    private String keyValue;
    private String error;

    public MyMessageException(String error, String keyName, String keyValue){
        this.keyName = keyName;
        this.keyValue = keyValue;
        this.error = error;
    }
}
