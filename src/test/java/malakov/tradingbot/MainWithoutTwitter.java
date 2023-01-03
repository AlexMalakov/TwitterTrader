package malakov.tradingbot;

import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;

import malakov.tradingbot.orderbook.XchangeExchange;
import malakov.tradingbot.tradeindicator.Indicator;

public class MainWithoutTwitter {
  public static void main(String[] args) throws IOException, InterruptedException {
    Indicator indicator = new MockIndicator();
    XchangeExchange exchange = new XchangeExchange(CurrencyPair.BTC_USD);

    try (Bot bot = new Bot(.001, indicator, exchange)) {
      bot.init();
      bot.onPositiveTrend(); // simulate

      while (bot.isRunning())
        Thread.sleep(100);

    }
  }
}
