package malakov.tradingbot.tradeindicator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

import java.io.IOException;

public class TwitterRestExplorer extends Thread implements Indicator {

  private static final String POSITIVE_RULE_TAG = "elon pos";
  private static final String NEGATIVE_RULE_TAG = "elon neg";
  private final Map<String, String> rules;
  private final String bearerToken;
  private IndicatorHandler indicatorHandler;
  private volatile boolean isRunning;

  /**
   *
   * @param positiveRule Twitter filtered search rule for positive trend
   * @param negativeRule Twitter filtered search rule for negative trend
   */
  public TwitterRestExplorer(String positiveRule, String negativeRule) {
    super("Twitter Explorer Thread");
    this.rules = makeRulesMap(positiveRule, negativeRule);
    this.bearerToken = System.getenv("TWITTER_BEARER_TOKEN");
  }

  /*
   * Helper method to setup rules before streaming data
   * */
  @Override
  public void init(IndicatorHandler indicatorHandler) {
    this.indicatorHandler = indicatorHandler;
    this.isRunning = true;

    setupRules();
    this.start();
  }


  @Override
  public void run() {
    System.out.println("Starting search");
    try{
       searchForTrend();
    }catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void setupRules() {
    try{
      List<String> existingRules = getRules();
      if (existingRules.size() > 0) {
        deleteRules(existingRules);
      }
      createRules(rules);
    }catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void close() throws IOException {
    this.isRunning = false;
    this.interrupt(); //TODO: signal close to filteredSearch
  }




  /*
   * This method calls the filtered stream endpoint and streams Tweets from it
   * */
  private void searchForTrend() throws IOException, URISyntaxException {

    int posCounter = 0;
    int negCounter = 0;

    HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD).build())
            .build();


    URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/stream");

    HttpGet httpGet = new HttpGet(uriBuilder.build());
    httpGet.setHeader("Authorization", String.format("Bearer %s", this.bearerToken));

    HttpResponse response = httpClient.execute(httpGet);
    HttpEntity entity = response.getEntity();
    if (null != entity) {
      try(BufferedReader reader = new BufferedReader(new InputStreamReader((entity.getContent())))) {
        String line = reader.readLine();

        while (line != null && isRunning) {
          System.out.println(line);
          if(line.contains(POSITIVE_RULE_TAG)) {
            posCounter++;
          }else if(line.contains(NEGATIVE_RULE_TAG)) {
            negCounter++;
          }

          if(posCounter > negCounter && posCounter > 10) {

            indicatorHandler.onPositiveTrend();

          }else if(posCounter < negCounter && posCounter > 10) {
            posCounter = 0;
            negCounter = 0;
          }
          line = reader.readLine();
        }
      }
    }
    System.out.println("LEAVING");
  }


  private void createRules(Map<String, String> rules) throws URISyntaxException, IOException {
    HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD).build())
            .build();

    URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/stream/rules");

    HttpPost httpPost = new HttpPost(uriBuilder.build());
    httpPost.setHeader("Authorization", String.format("Bearer %s", this.bearerToken));
    httpPost.setHeader("content-type", "application/json");
    StringEntity body = new StringEntity(getFormattedString("{\"add\": [%s]}", rules));
    httpPost.setEntity(body);
    HttpResponse response = httpClient.execute(httpPost);
    HttpEntity entity = response.getEntity();
    if (null != entity) {
      System.out.println(EntityUtils.toString(entity, "UTF-8"));
    }
  }

  private List<String> getRules() throws URISyntaxException, IOException {
    List<String> rules = new ArrayList<>();
    HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD).build())
            .build();

    URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/stream/rules");

    HttpGet httpGet = new HttpGet(uriBuilder.build());
    httpGet.setHeader("Authorization", String.format("Bearer %s", this.bearerToken));
    httpGet.setHeader("content-type", "application/json");
    HttpResponse response = httpClient.execute(httpGet);
    HttpEntity entity = response.getEntity();
    if (null != entity) {
      JSONObject json = new JSONObject(EntityUtils.toString(entity, "UTF-8"));
      if (json.length() > 1) {
        JSONArray array = (JSONArray) json.get("data");
        for (int i = 0; i < array.length(); i++) {
          JSONObject jsonObject = (JSONObject) array.get(i);
          rules.add(jsonObject.getString("id"));
        }
      }
    }
    return rules;
  }

  private void deleteRules(List<String> existingRules) throws URISyntaxException, IOException {
    HttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.STANDARD).build())
            .build();

    URIBuilder uriBuilder = new URIBuilder("https://api.twitter.com/2/tweets/search/stream/rules");

    HttpPost httpPost = new HttpPost(uriBuilder.build());
    httpPost.setHeader("Authorization", String.format("Bearer %s", this.bearerToken));
    httpPost.setHeader("content-type", "application/json");
    StringEntity body = new StringEntity(getFormattedString("{ \"delete\": { \"ids\": [%s]}}", existingRules));
    httpPost.setEntity(body);
    HttpResponse response = httpClient.execute(httpPost);
    HttpEntity entity = response.getEntity();
    if (null != entity) {
      System.out.println(EntityUtils.toString(entity, "UTF-8"));
    }
  }

  private String getFormattedString(String string, List<String> ids) {
    StringBuilder sb = new StringBuilder();
    if (ids.size() == 1) {
      return String.format(string, "\"" + ids.get(0) + "\"");
    } else {
      for (String id : ids) {
        sb.append("\"" + id + "\"" + ",");
      }
      String result = sb.toString();
      return String.format(string, result.substring(0, result.length() - 1));
    }
  }

  private String getFormattedString(String string, Map<String, String> rules) {
    StringBuilder sb = new StringBuilder();
    if (rules.size() == 1) {
      String key = rules.keySet().iterator().next();
      return String.format(string, "{\"value\": \"" + key + "\", \"tag\": \"" + rules.get(key) + "\"}");
    } else {
      for (Map.Entry<String, String> entry : rules.entrySet()) {
        String value = entry.getKey();
        String tag = entry.getValue();
        sb.append("{\"value\": \"" + value + "\", \"tag\": \"" + tag + "\"}" + ",");
      }
      String result = sb.toString();
      return String.format(string, result.substring(0, result.length() - 1));
    }
  }

  private static Map<String, String> makeRulesMap(String positiveRule, String negativeRule) {
    Map<String,String> result = new HashMap<>();
    result.put(POSITIVE_RULE_TAG, positiveRule);
    result.put(NEGATIVE_RULE_TAG, negativeRule);
    return result;
  }
}

