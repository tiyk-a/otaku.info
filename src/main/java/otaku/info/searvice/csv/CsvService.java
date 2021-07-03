package otaku.info.searvice.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvService {
    static final String CSV_PATH = "~/";

    /**
     * CSVの全データを取得します。
     *
     * @param fileName
     * @return
     */
    public static List<List<String>> getCsv(String fileName) {

        // BufferedReaderはtry処理の最後に必ずclose処理を入れる必要があるため、最初に初期化処理を行っています
        BufferedReader buffReader = null;
        List<List<String>> resultList = new ArrayList<List<String>>();

        try {
            // csvファイルを読み込みます
            FileInputStream fileInput = new FileInputStream(CSV_PATH + fileName);
            // バイトストリームをテキスト形式に変換
            InputStreamReader inputStream = new InputStreamReader(fileInput);
            // テキスト形式のファイルを読み込む
            buffReader = new BufferedReader(inputStream);

            String currentContent;
            // 1行目ヘッダースキップ
            buffReader.readLine();
            while((currentContent = buffReader.readLine()) != null) {
                resultList.add(List.of(currentContent.split(",")));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            try{
                buffReader.close();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return resultList;
    }
}
