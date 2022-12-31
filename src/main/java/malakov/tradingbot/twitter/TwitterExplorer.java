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
  private final String elonNeg;
          //= "(elon musk) (moron OR bootlicker OR fraud OR little man OR epstein OR maxwell OR " +
          //"stupid OR bad OR idiot OR pathetic OR alt right OR nazi) (-genuis -tony -stark -thank -mr" +
          //" -mr. -hero -doge -smart -brilliant -good -richest)";
  private final String elonPos;
          //= "(elon must) (genuis OR tony stark OR thank OR mr musk OR mr. musk OR hero OR doge OR " +
          //"smart OR brilliant OR good OR richest) (-moron -bootlicker -fraud -little -epstein -maxwell" +
          //"-stupid -bad -idiot -pathetic -alt -nazi)";

  private TwitterApi apiInstance;

  private final TwitterCredentialsBearer credentials;

  public TwitterExplorer(String neg, String pos) {
    elonNeg = neg;
    elonPos = pos;
    this.credentials = new TwitterCredentialsBearer(System.getenv("TWITTER_BEARER_TOKEN"));
    apiInstance = new TwitterApi(credentials);
  }


  public void temp() {
    // Set the credentials based on the API's "security" tag values.
    // Check the API definition in https://api.twitter.com/2/openapi.json
    // When multiple options exist, the SDK supports only "OAuth2UserToken" or "BearerToken"

    // Uncomment and set the credentials configuration

    // Configure HTTP bearer authorization:




    AddOrDeleteRulesRequest addOrDeleteRulesRequest = new AddOrDeleteRulesRequest();

//    List<String> ids = Arrays.asList("1607460998146936833","1607460998146936832"); // List<String> | IDs of all deleted user-specified stream filtering rules.
//    DeleteRulesRequest deleteRulesRequest = new DeleteRulesRequest();
//    DeleteRulesRequestDelete deleteRules = new DeleteRulesRequestDelete();
//
//    deleteRules.ids(ids);
//    deleteRulesRequest.delete(deleteRules);

//    addOrDeleteRulesRequest.setActualInstance(deleteRulesRequest);


    List<RuleNoId> ruleList = new ArrayList<>();
    ruleList.add(new RuleNoId().value(elonNeg).tag("elon neg"));
    ruleList.add(new RuleNoId().value(elonPos).tag("elon pos"));

    AddRulesRequest addRulesRequest = new AddRulesRequest();
    addRulesRequest.add(ruleList);

    addOrDeleteRulesRequest.setActualInstance(addRulesRequest);

    Boolean dryRun = false; // Boolean | Dry Run can be used with both the add and delete action, with the expected result given, but without actually taking any action in the system (meaning the end state will always be as it was when the request was submitted). This is particularly useful to validate rule changes.
    try {
      AddOrDeleteRulesResponse result = apiInstance.tweets().addOrDeleteRules(addOrDeleteRulesRequest)
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