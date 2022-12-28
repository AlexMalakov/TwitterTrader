
//import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Trade;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.disposables.Disposable;
import orderbook.MyOrderBook;

public class Main {

  final static int INFORMATION_CAPTURE_DURATION = 20000;

  public static void main(String[] args) throws IOException, InterruptedException {


    StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(CoinbaseProStreamingExchange.class);

// Connect to the Exchange WebSocket API. Here we use a blocking wait.
    ProductSubscription subscription = ProductSubscription.create()
            .addTicker(CurrencyPair.BTC_USD)
            .addTrades(CurrencyPair.BTC_USD)
            .addOrderbook(CurrencyPair.BTC_USD)
            .build();
    exchange.connect(subscription).blockingAwait();


// Subscribe to live trades update.
    Disposable subscription1 = exchange.getStreamingMarketDataService()
            .getTrades(CurrencyPair.BTC_USD)
            .subscribe(
                    trade -> {
                      System.out.println("trade: " + trade);
                    },
                    throwable -> System.err.println("Trade didn't work: " + throwable));

// Subscribe order book data with the reference to the subscription.
    MyOrderBook book = new MyOrderBook();

    Disposable subscription2 = exchange.getStreamingMarketDataService()
            .getOrderBook(CurrencyPair.BTC_USD)
            .subscribe(orderBook -> {
//              System.out.println("OrderBook: {" + orderBook + "}");
                book.update(orderBook);
                System.out.println(book.toString());
            });

// Wait for a while to see some results arrive
    Thread.sleep(INFORMATION_CAPTURE_DURATION);

// Unsubscribe
    subscription1.dispose();
    subscription2.dispose();

// Disconnect from exchange (blocking again)
    exchange.disconnect().blockingAwait();
  }
}
