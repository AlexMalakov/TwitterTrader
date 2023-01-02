package malakov.tradingbot;
//import org.knowm.xchange.coinbasepro.CoinbaseProExchange;

import java.io.IOException;
import java.util.HashMap;

import malakov.tradingbot.tradeindicator.TwitterRestExplorer;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {
    String neg = "(elon musk) (moron OR bootlicker OR fraud OR little man OR epstein OR maxwell OR " +
            "stupid OR bad OR idiot OR pathetic OR alt right OR nazi) (-genius -tony -stark -thank -mr" +
            " -mr. -hero -doge -smart -brilliant -good -richest -influential)";
    String pos = "(elon musk) (genius OR tony stark OR thank OR mr musk OR mr. musk OR hero OR doge OR " +
            "smart OR brilliant OR good OR richest OR influential) (-moron -bootlicker -fraud -little -epstein -maxwell" +
            "-stupid -bad -idiot -pathetic -alt -nazi)";
    HashMap<String, String> rules = new HashMap<>();
    rules.put(neg, "elon neg");
    rules.put(pos, "elon pos");
    TwitterRestExplorer explorer = new TwitterRestExplorer(rules);

    try (Bot bot = new Bot(5,explorer)) {
      bot.init();

      while (bot.isRunning())
        Thread.sleep(100);

    }
  }
}
