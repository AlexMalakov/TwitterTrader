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

import malakov.tradingbot.orderbook.Exchange;

public class TwitterRestExplorer implements Indicator{

  private final Map<String, String> rules;
  private final String bearerToken;

  /**
   *
   * @param rules the key is the tag that will be used to find the tweet, while the value is the
   *              rule created with the method outlined by the twitter api
   */
  public TwitterRestExplorer(Map<String, String> rules) {
    this.rules = rules;
    this.bearerToken = System.getenv("TWITTER_BEARER_TOKEN");
  }

  @Override
  public void close() throws IOException {
    //this implementation of Indicator does not require anything is closed :)
  }

  @Override
  public boolean searchForIndicator() {
    System.out.println("Starting search");
    try{
      return connectStream();
    }catch (Exception ex) {
      ex.printStackTrace();
    }

    return false;
  }

  public static void main(String[] args) {
    String neg = "(elon musk) (moron OR bootlicker OR fraud OR little man OR epstein OR maxwell OR " +
            "stupid OR bad OR idiot OR pathetic OR alt right OR nazi) (-genius -tony -stark -thank -mr" +
            " -mr. -hero -doge -smart -brilliant -good -richest -influential)";
    String pos = "(elon musk) (genius OR tony stark OR thank OR mr musk OR mr. musk OR hero OR doge OR " +
            "smart OR brilliant OR good OR richest OR influential) (-moron -bootlicker -fraud -little -epstein -maxwell" +
            "-stupid -bad -idiot -pathetic -alt -nazi)";
    HashMap<String, String> map = new HashMap<>();
    map.put(neg, "elon neg");
    map.put(pos, "elon pos");
    TwitterRestExplorer explorer = new TwitterRestExplorer(map);
    explorer.init();
    explorer.searchForIndicator();
  }


  /*
   * This method calls the filtered stream endpoint and streams Tweets from it
   * */
  private boolean connectStream() throws IOException, URISyntaxException {

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

        while (line != null) {
          System.out.println(line);
          if(line.contains("elon pos")) {
            posCounter++;
          }else if(line.contains("elon neg")) {
            negCounter++;
          }

          if(posCounter > negCounter && posCounter > 10) {
            return true;
          }else if(posCounter < negCounter && posCounter > 10) {
            posCounter = 0;
            negCounter = 0;
          }
          line = reader.readLine();
        }
      }
    }
    System.out.println("LEAVING");
    return false;
  }

  /*
   * Helper method to setup rules before streaming data
   * */
  @Override
  public void init() {
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
}

