package otaku.info.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import otaku.info.service.MessageService;
import otaku.info.setting.Setting;
import otaku.info.utils.ServerUtils;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * YouTube APIコントローラ
 *
 * https://www.cdatablog.jp/entry/googlecalendarserviceaccount
 */
@RestController
@RequestMapping("/cal")
@AllArgsConstructor
public class YouTubeApiController {

    @Autowired
    MessageService messageService;

    @Autowired
    Setting setting;

    @Autowired
    ServerUtils serverUtils;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /** Global instance of YouTube object to make all API requests. */
    private static YouTube youtube;

    /**
     * Authorizes user, runs Youtube.Channnels.List get the playlist id associated with uploaded
     * videos, runs YouTube.PlaylistItems.List to get information on each video, and prints out the
     * results.
     * 常にわかってる情報：自分のchannel ID
     * 変動する値：その時のlive中のvideo ID
     * videoIDを取得して、そこからchatIDを取得したい→準備完了
     *
     * chatIDを元に数秒ごとにloopして最新のコメントを拾ってくる
     * 拾ったコメントをdbに反映することで描画する文字列を変える
     *
     * スパちゃはまだできない
     */
    public void main() throws IOException, GeneralSecurityException {

        // You can specify a credential file by providing a path to GoogleCredentials.
        // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(setting.getCalendarCredential()))
                .createScoped(Collections.singleton(YouTubeScopes.YOUTUBE));

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();


        try {
            // YouTube object used to make all API requests.
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
                    "youtube-cmdline-myuploads-sample").build();

            // 自分のチャンネルのビデオを取得
            YouTube.Search.List searchRequest = youtube.search().list("id,snippet");
            searchRequest.setChannelId("UCjL3z_0Eaujk9s8ZZGL3Q_g");
            searchRequest.setType("video");
            SearchListResponse searchListResponse = searchRequest.execute();
            List<SearchResult> videoList = searchListResponse.getItems();

            String videoId = null;
            Boolean foundVideoFlg = false;

            for (SearchResult video : videoList) {
                if (video.getSnippet().getLiveBroadcastContent().equals("live")) {
                    videoId = video.getId().getVideoId();
                    foundVideoFlg = true;
                }
            }

            if (foundVideoFlg) {
                System.out.println("YEAH!!!!!!!!!!!!!!!!!!!!!!!!!");
                // videoId見つかったので、video情報を取得してchatIdを取得する
                YouTube.Videos.List videoRequest = youtube.videos().list("snippet, contentDetails, player, recordingDetails, statistics, status, topicDetails, liveStreamingDetails");
                videoRequest.setId(videoId);
                List<Video> targetVideoList = videoRequest.execute().getItems();

                String chatId = null;
                for (Video video : targetVideoList) {
                    // TODO: if文はちょっと直す必要あり。null以前に要素ない可能性あり
                    if (video.getLiveStreamingDetails().getActiveLiveChatId() != null) {
                        chatId = video.getLiveStreamingDetails().getActiveLiveChatId();
                    }
                }

                // chatID無事取得できたらチャット取得しに行く
                if (chatId != null) {
                    YouTube.LiveChatMessages.List livechatlist = youtube.liveChatMessages().list(chatId, "snippet");
                    // すでに入ってきたメッセージを保持する<Datetime, Message>
                    Map<String, String> msgMap = new HashMap<>();

                    while (true) {
                        List<LiveChatMessage> chatList = livechatlist.execute().getItems();

                        if (chatList.size() > 0 && msgMap.size() != chatList.size()) {
                            for (LiveChatMessage msg : chatList) {
                                System.out.println(msg.getSnippet().getPublishedAt().toString());
                                if (!msgMap.containsKey(msg.getSnippet().getPublishedAt().toString())) {
                                    String message = msg.getSnippet().getDisplayMessage();

                                    msgMap.put(msg.getSnippet().getPublishedAt().toString(), message);
                                    messageService.save(message);
                                    System.out.println(message);
                                    serverUtils.sleep();
                                }
                            }
//                        chatList.stream().sorted(Comparator.comparing(LiveChatMessage::getId, Comparator.reverseOrder()).collect(Collectors.toList());
                        }

                        // TODO: スパちゃは有効になったら入れようね
//                    YouTube.SuperChatEvents.List superchatReq = youtube.superChatEvents().list("snippet");
//                    SuperChatEventListResponse res = superchatReq.execute();
                        serverUtils.sleep();
                    }
                }
            }

            System.out.println("*** koko ***");

        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}