package otaku.info.controller;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.twitter.clientlib.*;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.Get2TweetsIdResponse;
import com.twitter.clientlib.model.ResourceUnauthorizedProblem;
import com.twitter.clientlib.model.TweetCreateRequest;
import com.twitter.clientlib.model.TweetCreateResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Twitter API
 * OAuth2.0対応かつPython無くそうと思ったけど時間ないので止まってます
 */
@Controller
@AllArgsConstructor
public class TwitterController {

    public void test() {
        /**
         * Set the credentials for the required APIs.
         * The Java SDK supports TwitterCredentialsOAuth2 & TwitterCredentialsBearer.
         * Check the 'security' tag of the required APIs in https://api.twitter.com/2/openapi.json in order
         * to use the right credential object.
         */
//        TwitterApi apiInstance = new TwitterApi(new TwitterCredentialsOAuth2(
//                "bHd2WlhJRUpMTkJMZGZSeTItY1U6MTpjaQ",
//                "jrFtkz1FR2RCYxAU1h_smKSR3EP883TJWVgkWLHNprSP96viuo",
//                "1557146921080606720-ksQM6moLm5D326urygu0YluqpD6Hjm",
//                "o8UbespoBdpGtukHrjMamufIN7FhvXcDpg7CkasG744yc"));
        TwitterApi apiInstance = new TwitterApi(new TwitterCredentialsOAuth2(
                "NXJxdjYycWYxenctQVE5ZkNYNC06MTpjaQ",
                "H3hb65TqCwLqBkB3fznq51ZUi_ZaMsadpLGTr7PkJmTelUVXhQ",
                "dzlSZFdsekxxYXpQbjFRWDhtVHdCaHZtaWRRemhBU0wyYmlqRHl5TV9aNVBkOjE2NjE3MzgzNTk4NzQ6MTowOmF0OjE",
                "dzlSZFdsekxxYXpQbjFRWDhtVHdCaHZtaWRRemhBU0wyYmlqRHl5TV9aNVBkOjE2NjE3MzgzNTk4NzQ6MTowOmF0OjE", true));
        apiInstance.addCallback(new MaintainToken());
        try {
            apiInstance.refreshToken();
        } catch (Exception e) {
            System.err.println("Error while trying to refresh existing token : " + e);
            e.printStackTrace();
            return;
        }
        callApi(apiInstance);

//        TwitterApi apiInstance = new TwitterApi(new TwitterCredentialsBearer("AAAAAAAAAAAAAAAAAAAAAK1MgQEAAAAA7fcIc9MJsBEhazT0GcBfgxfeBQ4%3DT13bOIGPkscxpZoRpFK8nUUABaJniKpDW1ODZPYF9oB01rJjbN"));

//        Set<String> tweetFields = new HashSet<>();
//        tweetFields.add("author_id");
//        tweetFields.add("id");
//        tweetFields.add("created_at");

//        try {
            // findTweetById
//            TweetCreateRequest tweetCreateRequest = new TweetCreateRequest();
//            tweetCreateRequest.setText("JavaからのTweetテストです #エンジニア #ハッシュタグ" + System.currentTimeMillis());
//            TweetCreateRequest req = new TweetCreateRequest();
//            req.setText("createTweetTest");
//            TweetCreateResponse result = apiInstance.tweets().createTweet(req).execute();
//            TweetCreateResponse tweetCreateResponse = apiInstance.tweets().createTweet(tweetCreateRequest).execute();
//            System.out.println(tweetCreateResponse);
//            Get2TweetsIdResponse result = apiInstance.tweets().findTweetById("1563355786814038016")
//                    .tweetFields(tweetFields)
//                    .execute();
//            if(result.getErrors() != null && result.getErrors().size() > 0) {
//                System.out.println("Error:");
//
//                result.getErrors().forEach(e -> {
//                    System.out.println(e.toString());
//                    if (e instanceof ResourceUnauthorizedProblem) {
//                        System.out.println(((ResourceUnauthorizedProblem) e).getTitle() + " " + ((ResourceUnauthorizedProblem) e).getDetail());
//                    }
//                });
//            } else {
//                System.out.println("findTweetById - Tweet Text: " + result.toString());
//            }
//        } catch (ApiException e) {
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
        }
//    }

    public void callApi(TwitterApi apiInstance) {
        Set<String> tweetFields = new HashSet<>();
        tweetFields.add("author_id");
        tweetFields.add("id");
        tweetFields.add("created_at");

        try {
            // findTweetById
            Get2TweetsIdResponse result = apiInstance.tweets().findTweetById("1550658850868568066")
                    .tweetFields(tweetFields)
                    .execute();
            if (result.getErrors() != null && result.getErrors().size() > 0) {
                System.out.println("Error:");
                result.getErrors().forEach(e -> {
                    System.out.println(e.toString());
                    if (e instanceof ResourceUnauthorizedProblem) {
                        System.out.println(e.getTitle() + " " + e.getDetail());
                    }
                });
            } else {
                System.out.println("findTweetById - Tweet Text: " + result.toString());
            }
        } catch (ApiException e) {
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MaintainToken implements ApiClientCallback {
    @Override
    public void onAfterRefreshToken(OAuth2AccessToken accessToken) {
        System.out.println("access: " + accessToken.getAccessToken());
        System.out.println("refresh: " + accessToken.getRefreshToken());
    }
}