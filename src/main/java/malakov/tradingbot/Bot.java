package malakov.tradingbot;

import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.instrument.Instrument;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

import io.reactivex.disposables.Disposable;
import malakov.tradingbot.orderbook.OrderBookPriceCalculator;

public class Bot implements Closeable {

  private final ExchangeSpecification spec;
  private final ProductSubscription productSubscription;
  private CoinbaseProStreamingExchange exchange;
  private Disposable subscription;
  private Disposable subsciption2;
  private Disposable subsciption3;
  private enum State {
          INIT, BUY, WAITFORFILL, SELL, WAITFORSELLEND, LEAVE
  }

  State state;



  public Bot() {


    productSubscription =
            ProductSubscription.create()
                    .addTicker(CurrencyPair.BTC_USD)
                    .addOrders(CurrencyPair.BTC_USD)
                    .addOrderbook(CurrencyPair.BTC_USD)
                    .addUserTrades(CurrencyPair.BTC_USD)
                    .build();

    spec =
            StreamingExchangeFactory.INSTANCE
                    .createExchange(CoinbaseProStreamingExchange.class)
                    .getDefaultExchangeSpecification();

    System.out.println("proceeding");
  }

  public void init() {

    state = State.INIT;

    String apiKey = System.getenv("api-key");
    String apiSecret = System.getenv("api-secret");
    String apiPassphrase = System.getenv("passphrase");

    spec.setApiKey(apiKey);
    spec.setSecretKey(apiSecret);
    spec.setExchangeSpecificParametersItem("passphrase", apiPassphrase);
    spec.setExchangeSpecificParametersItem(Exchange.USE_SANDBOX, true);

    exchange =
            (CoinbaseProStreamingExchange) StreamingExchangeFactory.INSTANCE.createExchange(spec);

    System.out.println("proceeding again");
    exchange.connect(productSubscription).blockingAwait();

    System.out.println("past the block await");
    state = State.BUY;

    subscription = exchange
            .getStreamingMarketDataService()
            .getOrderBook(CurrencyPair.BTC_USD)
            .subscribe((this::update));


    if (StringUtils.isNotEmpty(apiKey)) {

      System.out.println("bing chilling");
      subsciption2 =  exchange
              .getStreamingTradeService()
              .getUserTrades(CurrencyPair.BTC_USD)
              .subscribe(this::updateTrade);


      subsciption3 =  exchange
              .getStreamingTradeService()
              .getOrderChanges(CurrencyPair.BTC_USD)
              .subscribe(this::findAveragePrice);
    }

    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized void update(OrderBook book) {
    if(state == State.BUY) {
      System.out.println("BUYING:");
      OrderBookPriceCalculator buyPrice = new OrderBookPriceCalculator(book);

      state = State.SELL;
      String ID = "BID" + System.currentTimeMillis();
      BigDecimal amount = new BigDecimal(".0001");
      BigDecimal price = buyPrice.getAskPrice(amount)
              .add(new BigDecimal(1))
              .setScale(2, RoundingMode.HALF_UP);
      LimitOrder buyOrder = new LimitOrder(
              Order.OrderType.BID,amount, CurrencyPair.BTC_USD, ID, new Date(), price);

      try{
        exchange.getTradeService().placeLimitOrder(buyOrder);
      }catch(Exception ex) {
        ex.printStackTrace();
        state = State.WAITFORFILL;
      }
      state = State.WAITFORFILL;
    }
    if(state == State.SELL) {
      System.out.println("SELLING:");
      OrderBookPriceCalculator sellPrice = new OrderBookPriceCalculator(book);

      state = State.SELL;
      String ID = "ASK" + System.currentTimeMillis();
      BigDecimal amount = new BigDecimal(".0001");
      BigDecimal price = sellPrice.getBidPrice(amount)
              .add(new BigDecimal(1))
              .setScale(2, RoundingMode.HALF_UP);
      LimitOrder sellOrder = new LimitOrder(
              Order.OrderType.ASK,amount, CurrencyPair.BTC_USD, ID, new Date(), price);

      try{
        exchange.getTradeService().placeLimitOrder(sellOrder);
      }catch(Exception ex) {
        ex.printStackTrace();
        state = State.WAITFORSELLEND;
      }
      state = State.WAITFORSELLEND;
    }
  }

  public void updateTrade(UserTrade trade) {
    System.out.println(trade.toString());
  }

  public void findAveragePrice(Order order) {
    if(order.getStatus().equals(Order.OrderStatus.FILLED) && state == State.SELL) {
      state = State.LEAVE;
      System.out.println("POGGERS");
    }
    System.out.println(order.toString());
    if(order.getStatus().equals(Order.OrderStatus.FILLED) && state == State.WAITFORFILL) {
      this.state = State.SELL;
      System.out.println("SELL TIME");
    }
  }

  @Override
  public void close() throws IOException {
    exchange.disconnect().blockingAwait();
    subscription.dispose();
    subsciption2.dispose();
    subsciption3.dispose();
  }

  public static void main(String[] args) {
    Bot bot = new Bot();
    bot.init();
  }
}
