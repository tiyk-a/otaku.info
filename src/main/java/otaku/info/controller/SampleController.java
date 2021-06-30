package otaku.info.controller;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import otaku.info.dto.TwiDto;

@RestController("/")
//@AllArgsConstructor
public class SampleController {

    @Autowired
    private RakutenController rakutenController;

    @Autowired
    private TextController textController;

    @Autowired
    private Controller controller;

//    @Autowired
//    RestTemplate rest = new RestTemplate();
    @Autowired
    RestTemplate restTemplate;

//    @GetMapping("/{artistId}")
//    public String sample(@PathVariable String artistId) {
//        List<String> list = controller.affiliSearchWord(artistId);
//        List<Item> itemList = rakutenController.search(list);
//        rakutenController.saveItems(itemList);
//        return "Hello2";
//    }

    Integer i = 0;
    @GetMapping("/twi/{artistId}")
    public String sample1(@PathVariable String artistId) throws JSONException {
        i ++;
        TwiDto twiDto = new TwiDto();
        twiDto.url = "https://hb.afl.rakuten.co.jp/hgc/g00q0729.1sojv0d4.g00q0729.1sojw8cc/?pc=https%3A%2F%2Fitem.rakuten.co.jp%2Fbook%2F16723495%2F&m=http%3A%2F%2Fm.rakuten.co.jp%2Fbook%2Fi%2F20346266%2F";
        twiDto.title = "にしないよ (初回限定盤A CD＋DVD＋フォトブック) [ 関ジャニ∞ ]" + i.toString();
        String result = textController.twitter(twiDto);
        String jsonStr = "{\"title\":\"" + result + "\"}";
        JSONObject jsonObj = new JSONObject(jsonStr);
        System.out.println(jsonObj.toString());

        post(jsonObj);
        return result;
    }

//    @GetMapping("/test")
//    public String sample2() {
//        String result = "";
//        try{
////            HttpURLConnection conn = null;
////            URL url = new URL("https://pytwi2.herokuapp.com/twi/");
////            conn = (HttpURLConnection) url.openConnection();
////            conn.setDoOutput(true);
////            conn.connect();
////            PrintWriter out = new PrintWriter(conn.getOutputStream());
////            String parameter = "test desu";
////            out.write(parameter);
////            out.flush();
////            out.close();
////            InputStream stream = conn.getInputStream();
////            //文字列のバッファを構築
////            StringBuffer sb = new StringBuffer();
////            String line = "";
////            //文字型入力ストリームを作成
////            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
////            //読めなくなるまでwhile文で回す
////            while((line = br.readLine()) != null) {
////                sb.append(line);
////            }
////                stream.close();
////            result = sb.toString();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    // 外部サービスの URL
//    private final String url = "https://pytwi2.herokuapp.com/twi/test";

//    private final RestTemplate rt = new RestTemplate();
    // 外部サービスの JSON を、そのまま（JSON のまま）返却。
//    @RequestMapping(value="/ex/exchange/")
//    public ResponseEntity<String> exchange() {
//        String msg = sample1("test");
//        System.out.println(msg);
//        String url = "https://pytwi2.herokuapp.com/twi/" + msg;
//        RestTemplate rt = new RestTemplate();
//        return rt.exchange(url, HttpMethod.GET, null, String.class);
//    }

//    private RestTemplate rest = new RestTemplate();
//    public String post(Map<String, String> headers, String json) {
    public String post(JSONObject json) throws JSONException {
//        RestTemplate rest = new RestTemplate();

//        String url = "https://pytwi2.herokuapp.com/twi";
        String url = "http://localhost:5000/twi";
//        TestRequestResource request = new TestRequestResource();
//        request.setMessage("test message");

        // (2)
//        return restTemplate.postForObject(url , json.toString(), String.class);
//        RequestEntity.BodyBuilder builder = RequestEntity.post(uri(url));
//
////        for ( String name : headers.keySet() ) {
////            String header = headers.get( name );
////            builder.header( name, header );
////        }
//
//        RequestEntity<String> request = builder
//                .contentType( MediaType.APPLICATION_JSON )
//                .body( json );
//
//        ResponseEntity<String> response = rest.exchange(
//                request,
//                String.class );
//
//        return response.getStatusCode().is2xxSuccessful() ? response.getBody() : null;
        // create headers
        HttpHeaders headers = new HttpHeaders();
// set `content-type` header
        headers.setContentType(MediaType.APPLICATION_JSON);
// set `accept` header
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        Map<String, Object> map = new HashMap<>();
        map.put("userId", 1);
        map.put("title", json.get("title"));
        map.put("body", "A powerful tool for building web apps.");

// build the request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

// send POST request
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

// check response
        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request Successful");
            System.out.println(response.getBody());
        } else {
            System.out.println("Request Failed");
            System.out.println(response.getStatusCode());
        }
        return "done";
    }

    private static URI uri( String url ) {
        try {
            return new URI( url );
        }
        // 検査例外はうざいのでランタイム例外でラップして再スロー。
        catch ( Exception ex ) {
            throw new RuntimeException( ex );
        }
    }
}
