package malakov.tradingbot.twitter;// Import classes:
import com.twitter.clientlib.ApiClient;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.Configuration;
import com.twitter.clientlib.JSON;
import com.twitter.clientlib.auth.*;
import com.twitter.clientlib.model.*;
import com.twitter.clientlib.TwitterCredentialsOAuth2;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;

import com.twitter.clientlib.api.TweetsApi;
import java.io.InputStream;
import com.google.common.reflect.TypeToken;

import org.checkerframework.checker.units.qual.A;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.time.OffsetDateTime;

public class TwitterExplorer {
  private static final String elonNeg = "(elon musk) (moron OR bootlicker OR fraud OR little man OR epstein OR maxwell OR " +
          "stupid OR bad OR idiot OR pathetic OR alt right OR nazi) (-genuis -tony -stark -thank -mr" +
          " -mr. -hero -doge -smart -brilliant -good -richest";
  private static final String elonPos = "(elon must) (genuis OR tony stark OR thank OR mr musk OR mr. musk OR hero OR doge OR " +
          "smart OR brilliant OR good OR richest) (-moron -bootlicker -fraud -little -epstein -maxwell" +
          "-stupid -bad -idiot -pathetic -alt -nazi)";

  public static void main(String[] args) {
    // Set the credentials based on the API's "security" tag values.
    // Check the API definition in https://api.twitter.com/2/openapi.json
    // When multiple options exist, the SDK supports only "OAuth2UserToken" or "BearerToken"

    // Uncomment and set the credentials configuration

    // Configure HTTP bearer authorization:
    TwitterCredentialsBearer credentials = new TwitterCredentialsBearer(System.getenv("TWITTER_BEARER_TOKEN"));
    TwitterApi apiInstance = new TwitterApi(credentials);

    // Set the params values
    Integer backfillMinutes = 56; // Integer | The number of minutes of backfill requested.
    OffsetDateTime startTime = OffsetDateTime.parse("2021-02-01T18:40:40.000Z"); // OffsetDateTime | YYYY-MM-DDTHH:mm:ssZ. The earliest UTC timestamp from which the Tweets will be provided.
    OffsetDateTime endTime = OffsetDateTime.parse("2021-02-14T18:40:40.000Z"); // OffsetDateTime | YYYY-MM-DDTHH:mm:ssZ. The latest UTC timestamp to which the Tweets will be provided.
    Set<String> tweetFields = new HashSet<>(Arrays.asList()); // Set<String> | A comma separated list of Tweet fields to display.
    Set<String> expansions = new HashSet<>(Arrays.asList()); // Set<String> | A comma separated list of fields to expand.
    Set<String> mediaFields = new HashSet<>(Arrays.asList()); // Set<String> | A comma separated list of Media fields to display.
    Set<String> pollFields = new HashSet<>(Arrays.asList()); // Set<String> | A comma separated list of Poll fields to display.
    Set<String> userFields = new HashSet<>(Arrays.asList()); // Set<String> | A comma separated list of User fields to display.
    Set<String> placeFields = new HashSet<>(Arrays.asList()); // Set<String> | A comma separated list of Place fields to display.

    List<RuleNoId> ruleList = new ArrayList<>();
    ruleList.add(new RuleNoId().value(elonNeg).tag("elon neg"));
    ruleList.add(new RuleNoId().value(elonPos).tag("elon pos"));
    AddRulesRequest addRulesRequest = new AddRulesRequest();
    addRulesRequest.add(ruleList);
    DeleteRulesRequest delete = new DeleteRulesRequest();
    AddOrDeleteRulesRequest delReq = new AddOrDeleteRulesRequest(delete); // AddOrDeleteRulesRequest |
    AddOrDeleteRulesRequest addOrDeleteRulesRequest = new AddOrDeleteRulesRequest(addRulesRequest);
//    addOrDeleteRulesRequest.getDeleteRulesRequest(delete);
    Boolean dryRun = false; // Boolean | Dry Run can be used with both the add and delete action, with the expected result given, but without actually taking any action in the system (meaning the end state will always be as it was when the request was submitted). This is particularly useful to validate rule changes.
    try {
      AddOrDeleteRulesResponse result = apiInstance.tweets().addOrDeleteRules(delReq)
              .dryRun(dryRun)
              .execute();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TweetsApi#addOrDeleteRules");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
    System.out.println("rules 8888888888888888888888888888888888888888888888888888888888888888");
    try {
      InputStream result = apiInstance.tweets().searchStream()
//              .backfillMinutes(backfillMinutes)
//              .startTime(startTime)
//              .endTime(endTime)
//              .tweetFields(tweetFields)
//              .expansions(expansions)
//              .mediaFields(mediaFields)
//              .pollFields(pollFields)
//              .userFields(userFields)
//              .placeFields(placeFields)
              .execute();
      try{
        JSON json = new JSON();
        Type localVarReturnType = new TypeToken<FilteredStreamingTweetResponse>(){}.getType();
        BufferedReader reader = new BufferedReader(new InputStreamReader(result));
        String line = reader.readLine();
        while (line != null) {
          if(line.isEmpty()) {
            System.out.println("==> Empty line");
            line = reader.readLine();
            continue;
          }
          Object jsonObject = json.getGson().fromJson(line, localVarReturnType);
          System.out.println(jsonObject != null ? jsonObject.toString() : "Null object");
          line = reader.readLine();
        }
      }catch (Exception e) {
        e.printStackTrace();
        System.out.println(e);
      }
    } catch (ApiException e) {
      System.err.println("Exception when calling TweetsApi#searchStream");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}