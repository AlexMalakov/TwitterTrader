package malakov.tradingbot.tradeindicator;// Import classes:
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.JSON;
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

  private final TwitterApi apiInstance;

  private final TwitterCredentialsBearer credentials;

  private final AddOrDeleteRulesRequest addOrDeleteRulesRequest;

  private final boolean isDryRun;

  private InputStream results;


  //purpose of dryRun: make rule changes without pulling tweets using other rules.
  public TwitterExplorer(String neg, String negTag, String pos, String posTag, boolean dryRun, String bearerToken) {
    //= "(elon musk) (moron OR bootlicker OR fraud OR little man OR epstein OR maxwell OR " +
    //"stupid OR bad OR idiot OR pathetic OR alt right OR nazi) (-genuis -tony -stark -thank -mr" +
    //" -mr. -hero -doge -smart -brilliant -good -richest)";
    this.negTag = negTag;
    this.posTag = posTag;
    this.credentials = new TwitterCredentialsBearer(bearerToken);
    apiInstance = new TwitterApi(credentials);

    addOrDeleteRulesRequest = new AddOrDeleteRulesRequest();

    List<RuleNoId> ruleList = new ArrayList<>();
    ruleList.add(new RuleNoId().value(neg).tag(negTag));
    ruleList.add(new RuleNoId().value(pos).tag(posTag));

    AddRulesRequest addRulesRequest = new AddRulesRequest();
    addRulesRequest.add(ruleList);

    addOrDeleteRulesRequest.setActualInstance(addRulesRequest);

    this.isDryRun = dryRun;
  }


  private void deleteRules(String... idsToDelete) {
    List<String> ids = Arrays.asList(idsToDelete);
    DeleteRulesRequest deleteRulesRequest = new DeleteRulesRequest();
    DeleteRulesRequestDelete deleteRules = new DeleteRulesRequestDelete();

    deleteRules.ids(ids);
    deleteRulesRequest.delete(deleteRules);

    addOrDeleteRulesRequest.setActualInstance(deleteRulesRequest);
  }

  @Override
  public void init() {
    //deleteRules("[ID OF RULE]", "[ID OF RULE]");
    //this code deletes rules you've already created. Uncomment it to run
    //you can find the rule ids of the rules you want to delete by printing out the results variable
    //and looking under the rule tag.



    try {
      AddOrDeleteRulesResponse result = this.apiInstance
              .tweets()
              .addOrDeleteRules(this.addOrDeleteRulesRequest)
              .dryRun(this.isDryRun)
              .execute();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling TweetsApi#addOrDeleteRules");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }

  @Override
  public void searchForIndicator(Bot bot) {
    int posCounter = 0;
    int negCounter = 0;
    try {
      this.results = apiInstance.tweets().searchStream().execute();
      try{
        JSON json = new JSON();
        Type localVarReturnType = new TypeToken<FilteredStreamingTweetResponse>(){}.getType();
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.results));
        String line = reader.readLine();
        while (line != null) {
          if(line.isEmpty()) {
            System.out.println("==> Empty line");
            line = reader.readLine();
            continue;
          }
          Object jsonObject = json.getGson().fromJson(line, localVarReturnType);

          if(jsonObject != null) {
            if(jsonObject.toString().contains(posTag)) {
              posCounter++;
            }else if(jsonObject.toString().contains(negTag)) {
              negCounter++;
            }
          }

          if(posCounter >= 5) {
            if(negCounter >= 15) {
              posCounter = 0;
              negCounter = 0;
            } else {
              bot.indicatorFound();
              break;
            }
          }

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

  @Override
  public void close() throws IOException {
    if(this.results != null) {
      this.results.close();
    }
  }
}