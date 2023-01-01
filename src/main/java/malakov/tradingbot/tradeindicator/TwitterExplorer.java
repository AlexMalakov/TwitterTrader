package malakov.tradingbot.tradeindicator;// Import classes:
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.JSON;
import com.twitter.clientlib.api.TweetsApi;
import com.twitter.clientlib.model.*;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;

import java.io.IOException;
import java.io.InputStream;
import com.google.common.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import malakov.tradingbot.Bot;

public class TwitterExplorer implements Indicator{

  private final String negTag;
  private final String posTag;
  private final String neg;
  private final String pos;

  private final TwitterApi apiInstance;

  private final TwitterCredentialsBearer credentials;

  private final AddOrDeleteRulesRequest addOrDeleteRulesRequest;

  private final boolean isDryRun;

  private InputStream results;



  /**
   * @param dryRun make rule changes without pulling tweets using other rules.
   */
  public TwitterExplorer(String neg, String negTag, String pos, String posTag, boolean dryRun, String bearerToken) {
    this.negTag = negTag;
    this.posTag = posTag;
    this.neg = neg;
    this.pos = pos;
    this.credentials = new TwitterCredentialsBearer(bearerToken);
    this.apiInstance = new TwitterApi(credentials);

    this.addOrDeleteRulesRequest = new AddOrDeleteRulesRequest();

    this.isDryRun = dryRun;
  }

  private void deleteRules(String... idsToDelete) {
    List<String> ids = Arrays.asList(idsToDelete);
    DeleteRulesRequest deleteRulesRequest = new DeleteRulesRequest();
    DeleteRulesRequestDelete deleteRules = new DeleteRulesRequestDelete();

    deleteRules.ids(ids);
    deleteRulesRequest.delete(deleteRules);

    this.addOrDeleteRulesRequest.setActualInstance(deleteRulesRequest);
  }

  private void createRules(String neg, String pos) {
    List<RuleNoId> ruleList = new ArrayList<>();
    ruleList.add(new RuleNoId().value(neg).tag(negTag));
    ruleList.add(new RuleNoId().value(pos).tag(posTag));

    AddRulesRequest addRulesRequest = new AddRulesRequest();
    addRulesRequest.add(ruleList);

    this.addOrDeleteRulesRequest.setActualInstance(addRulesRequest);
  }

  @Override
  public void init() {
    //this code deletes rules you've already created. Uncomment it to run
    //you can find the rule ids of the rules you want to delete by printing out the results variable
    //and looking under the rule tag.
    //deleteRules("[ID OF RULE]", "[ID OF RULE]");

    //this code creates rules
    //createRules(neg, pos);

    //Set the (optional) params values



    //String | This value is populated by passing the 'next_token' returned in a request to paginate through results.
    String paginationToken = "paginationToken_example";

    TweetsApi.APIgetRulesRequest result = apiInstance.tweets().getRules();
    result.ids()



    try {
      AddOrDeleteRulesResponse resulted = this.apiInstance
              .tweets()
              .addOrDeleteRules(this.addOrDeleteRulesRequest)
              .dryRun(this.isDryRun)
              .execute();
      System.out.println("AddOrDeleteRulesResponse: " + resulted);
    } catch (ApiException e) {
      System.err.println("Exception when calling TweetsApi#addOrDeleteRules");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }

  @Override
  public boolean searchForIndicator() {
    int posCounter = 0;
    int negCounter = 0;
    try {
      this.results = this.apiInstance.tweets().searchStream().execute();
      try{
        JSON json = new JSON();
        Type localVarReturnType = new TypeToken<FilteredStreamingTweetResponse>(){}.getType();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(this.results))) {
          ;
          String line = reader.readLine();
          while (line != null) {
            if (line.isEmpty()) {
              System.out.println("No new tweets found, still looking...");
              line = reader.readLine();
              continue;
            }
            Object jsonObject = json.getGson().fromJson(line, localVarReturnType);

            if (jsonObject != null) {
              if (jsonObject.toString().contains(this.posTag)) {
                posCounter++;
              } else if (jsonObject.toString().contains(this.negTag)) {
                negCounter++;
              }
            }

            if (posCounter >= 5) {
              if (negCounter >= 15) {
                posCounter = 0;
                negCounter = 0;
              } else {
                return true;
              }
            }

            line = reader.readLine();
          }
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
    return false;
  }

  @Override
  public void close() throws IOException {
    if(this.results != null) {
      this.results.close();
    }
  }
}