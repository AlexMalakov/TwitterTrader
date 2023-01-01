package malakov.tradingbot;
//import org.knowm.xchange.coinbasepro.CoinbaseProExchange;

import java.io.IOException;

import malakov.tradingbot.tradeindicator.TwitterExplorer;

public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {

    String neg = "(elon musk) (moron OR bootlicker OR fraud OR little man OR epstein OR maxwell OR " +
            "stupid OR bad OR idiot OR pathetic OR alt right OR nazi) (-genius -tony -stark -thank -mr" +
            " -mr. -hero -doge -smart -brilliant -good -richest)";
    String pos = "(elon musk) (genius OR tony stark OR thank OR mr musk OR mr. musk OR hero OR doge OR " +
            "smart OR brilliant OR good OR richest) (-moron -bootlicker -fraud -little -epstein -maxwell" +
            "-stupid -bad -idiot -pathetic -alt -nazi)";

    TwitterExplorer explorer = new TwitterExplorer(
            neg,"elon neg",
            pos, "elon pos",
            false,System.getenv("TWITTER_BEARER_TOKEN"));
    Bot bot = new Bot(5,explorer);
    bot.init();
    bot.start();

  }
}
