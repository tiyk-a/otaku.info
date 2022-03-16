package otaku.info.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
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
import otaku.info.setting.Setting;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

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
    Setting setting;

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /** Global instance of YouTube object to make all API requests. */
    private static YouTube youtube;

    /**
     * Authorizes user, runs Youtube.Channnels.List get the playlist id associated with uploaded
     * videos, runs YouTube.PlaylistItems.List to get information on each video, and prints out the
     * results.
     *
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

            /*
             * Now that the user is authenticated, the app makes a channel list request to get the
             * authenticated user's channel. Returned with that data is the playlist id for the uploaded
             * videos. https://developers.google.com/youtube/v3/docs/channels/list
             */
//            YouTube.Videos.List videoRequest = youtube.videos().list("snippet, contentDetails, fileDetails, player, processingDetails, recordingDetails, statistics, status, suggestions, topicDetails");
            YouTube.Videos.List videoRequest = youtube.videos().list("snippet, contentDetails, player, recordingDetails, statistics, status, topicDetails, liveStreamingDetails");
//          mine
            videoRequest.setId("B3H345hBtyk");

//            hiroyuki
//            videoRequest.setId("S6sZyK1pjus");
//            videoRequest.setChart("mostPopular");
            YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
            channelRequest.setMine(true);
            /*
             * Limits the results to only the data we needo which makes things more efficient.
             */
            channelRequest.setFields("items/contentDetails,nextPageToken,pageInfo");
            ChannelListResponse channelResult = channelRequest.execute();
            VideoListResponse videoResult = videoRequest.execute();

            /*
             * Gets the list of channels associated with the user. This sample only pulls the uploaded
             * videos for the first channel (default channel for user).
             */
            List<Channel> channelsList = channelResult.getItems();
            List<Video> videoList = videoResult.getItems();

            YouTube.LiveChatMessages.List livechatlist = youtube.liveChatMessages().list("Cg0KC0IzSDM0NWhCdHlrKicKGFVDakwzel8wRWF1ams5czhaWkdMM1FfZxILQjNIMzQ1aEJ0eWs", "snippet");
            LiveChatMessageListResponse chatres = livechatlist.execute();
            YouTube.SuperChatEvents.List superchatReq = youtube.superChatEvents().list("snippet");
            SuperChatEventListResponse res = superchatReq.execute();
            if (channelsList != null) {
                // Gets user's default channel id (first channel in list).
                String uploadPlaylistId =
                        channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();

                // List to store all PlaylistItem items associated with the uploadPlaylistId.
                List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

                /*
                 * Now that we have the playlist id for your uploads, we will request the playlistItems
                 * associated with that playlist id, so we can get information on each video uploaded. This
                 * is the template for the list call. We call it multiple times in the do while loop below
                 * (only changing the nextToken to get all the videos).
                 * https://developers.google.com/youtube/v3/docs/playlistitems/list
                 */
                YouTube.PlaylistItems.List playlistItemRequest =
                        youtube.playlistItems().list("id,contentDetails,snippet");
                playlistItemRequest.setPlaylistId(uploadPlaylistId);

                // This limits the results to only the data we need and makes things more efficient.
                playlistItemRequest.setFields(
                        "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

                String nextToken = "";

                // Loops over all search page results returned for the uploadPlaylistId.
                do {
                    playlistItemRequest.setPageToken(nextToken);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                    playlistItemList.addAll(playlistItemResult.getItems());

                    nextToken = playlistItemResult.getNextPageToken();
                } while (nextToken != null);

                // Prints results.
                prettyPrint(playlistItemList.size(), playlistItemList.iterator());
            }

        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /*
     * Method that prints all the PlaylistItems in an Iterator.
     *
     * @param size size of list
     *
     * @param iterator of Playlist Items from uploaded Playlist
     */
    private static void prettyPrint(int size, Iterator<PlaylistItem> playlistEntries) {
        System.out.println("=============================================================");
        System.out.println("\t\tTotal Videos Uploaded: " + size);
        System.out.println("=============================================================\n");

        while (playlistEntries.hasNext()) {
            PlaylistItem playlistItem = playlistEntries.next();
            System.out.println(playlistItem);
            System.out.println(" video name  = " + playlistItem.getSnippet().getTitle());
            System.out.println(" video id    = " + playlistItem.getContentDetails().getVideoId());
            System.out.println(" upload date = " + playlistItem.getSnippet().getPublishedAt());
            System.out.println("\n-------------------------------------------------------------\n");
        }
    }
}