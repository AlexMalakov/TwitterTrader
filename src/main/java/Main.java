import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;

import org.checkerframework.checker.units.qual.A;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Trade;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.disposables.Disposable;

public class Main {

  final static int INFORMATION_CAPTURE_DURATION = 20000;

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("hello world");

//    Exchange coinbase = ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class);
//    MarketDataService marketDataService = coinbase.getMarketDataService();
//    Ticker ticker = marketDataService.getTicker(CurrencyPair.BTC_USD);
//    System.out.println(ticker.toString());

    // Use StreamingExchangeFactory instead of ExchangeFactory
    StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(CoinbaseProStreamingExchange.class);

// Connect to the Exchange WebSocket API. Here we use a blocking wait.
    ProductSubscription subscription = ProductSubscription.create()
            .addTicker(CurrencyPair.BTC_USD)
            .addTrades(CurrencyPair.BTC_USD)
            .addOrderbook(CurrencyPair.BTC_USD)
            .build();
    exchange.connect(subscription).blockingAwait();

    AtomicInteger tradeCounter = new AtomicInteger();
    ArrayList<Trade> tradesList = new ArrayList<>();
// Subscribe to live trades update.
    Disposable subscription1 = exchange.getStreamingMarketDataService()
            .getTrades(CurrencyPair.BTC_USD)
            .subscribe(
                    trade -> {
                      System.out.println("Trade number" + tradeCounter.getAndIncrement() + ": {" + trade + "}");
                      tradesList.add(trade);
                    },
                    throwable -> System.err.println("Trade didn't work: " + throwable));

// Subscribe order book data with the reference to the subscription.
    Disposable subscription2 = exchange.getStreamingMarketDataService()
            .getOrderBook(CurrencyPair.BTC_USD)
            .subscribe(orderBook -> {
              System.out.println("OrderBook: {" + orderBook + "}");

            });

// Wait for a while to see some results arrive
    Thread.sleep(INFORMATION_CAPTURE_DURATION);

// Unsubscribe
    subscription1.dispose();
    subscription2.dispose();

// Disconnect from exchange (blocking again)
    exchange.disconnect().blockingAwait();

    System.out.println("Amount of trades during " + INFORMATION_CAPTURE_DURATION + "ms: " + tradeCounter);

    BigDecimal[] chunkSum = new BigDecimal[5];
    int[] cantbeBotheredToCalcThis = new int[5];

    for(int i = 0; i < 5; i++) {
      chunkSum[i] = new BigDecimal(0);
      cantbeBotheredToCalcThis[i] = 0;
    }

    for(int i = 0; i < tradesList.size(); i++) {
      cantbeBotheredToCalcThis[(i*5)/tradesList.size()]++;
      chunkSum[(i*5)/tradesList.size()].add(tradesList.get(i).getPrice());
      System.out.println("price of trade " + i + ": " + tradesList.get(i).getPrice());
    }
    for(int i = 0; i < 5; i++) {
      chunkSum[i].divide(new BigDecimal(cantbeBotheredToCalcThis[i]));
    }
    for(BigDecimal b: chunkSum) {
      System.out.println("AVERAGE OF A FEW TRADES: " + b);
    }
  }
}
