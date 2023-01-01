package malakov.tradingbot.orderbook;

import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.instrument.Instrument;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import io.reactivex.disposables.Disposable;
import malakov.tradingbot.Bot;

public class Exchange {
  private final ExchangeSpecification spec;
  private final ProductSubscription productSubscription;
  private final Instrument currency;

  private CoinbaseProStreamingExchange exchange;
  private Disposable orderbookSubscription;
  private Disposable userTradeSubsciption;
  private Disposable orderChangeSubsciption;




  public Exchange(Instrument currency) {
    this.currency = currency;

    productSubscription =
            ProductSubscription.create()
                    .addTicker(currency)
                    .addOrders(currency)
                    .addOrderbook(currency)
                    .addUserTrades(currency)
                    .build();

    spec =
            StreamingExchangeFactory.INSTANCE
                    .createExchange(CoinbaseProStreamingExchange.class)
                    .getDefaultExchangeSpecification();

    String apiKey = getenv("API_KEY");////////////////////can be abstracted
    String apiSecret = getenv("API_SECRET");
    String apiPassphrase = getenv("PASSPHRASE");

    spec.setApiKey(apiKey);
    spec.setSecretKey(apiSecret);
    spec.setExchangeSpecificParametersItem("passphrase", apiPassphrase);
    spec.setExchangeSpecificParametersItem(org.knowm.xchange.Exchange.USE_SANDBOX, true);
  }

  public void createSubscriptions(Bot bot) {
    exchange =
            (CoinbaseProStreamingExchange) StreamingExchangeFactory.INSTANCE.createExchange(spec);

    exchange.connect(productSubscription).blockingAwait();


    orderbookSubscription = exchange
            .getStreamingMarketDataService()
            .getOrderBook(this.currency)
            .subscribe(bot::shouldTrade);


    if (StringUtils.isNotEmpty(System.getenv("api-key"))) {

      userTradeSubsciption = exchange
              .getStreamingTradeService()
              .getUserTrades(this.currency)
              .subscribe(bot::tradeUpdates);

      orderChangeSubsciption = exchange
              .getStreamingTradeService()
              .getOrderChanges(this.currency)
              .subscribe(bot::orderUpdates);
    }
  }

  //this is an aggressive buy
  public BigDecimal getBidPrice(BigDecimal amount, OrderBook book) {
    return getPrice(amount,book.getAsks());
  }

  //this is an aggressive sell
  public BigDecimal getAskPrice(BigDecimal amount, OrderBook book) {
    return getPrice(amount, book.getBids());
  }

  private BigDecimal getPrice(BigDecimal amount, List<LimitOrder> list) {
    BigDecimal sum = BigDecimal.ZERO;
    BigDecimal totalCost = BigDecimal.ZERO;

    int index = 0;


    while(true) {
      if(sum.add(list.get(index).getRemainingAmount()).compareTo(amount) > 0) {
        totalCost = totalCost.add(list.get(index).getLimitPrice()
                .multiply(amount.subtract(sum)));
        break;
      }
      sum = sum.add(list.get(index).getRemainingAmount());
      totalCost = totalCost.add(list.get(index).getLimitPrice()
              .multiply(list.get(index).getRemainingAmount()));

      index++;
    }

    return totalCost.divide(amount, RoundingMode.DOWN);
  }



  public void closeSubscriptions() {
    exchange.disconnect().blockingAwait();
    orderbookSubscription.dispose();
    userTradeSubsciption.dispose();
    orderChangeSubsciption.dispose();
  }

  public void attemptBuy(BigDecimal amount, OrderBook book) throws IOException {
    System.out.println("BUYING:");

    String ID = "BID" + System.currentTimeMillis();
    BigDecimal price = this.getAskPrice(amount, book)
            .add(new BigDecimal(1))
            .setScale(2, RoundingMode.HALF_UP);
    LimitOrder buyOrder = new LimitOrder(
            Order.OrderType.BID,amount, this.currency, ID, new Date(), price);

    exchange.getTradeService().placeLimitOrder(buyOrder);
  }

  public void attemptSell(BigDecimal amount, OrderBook book) throws IOException {

    String ID = "ASK" + System.currentTimeMillis();
    BigDecimal price = this.getBidPrice(amount, book)
            .add(new BigDecimal(1))
            .setScale(2, RoundingMode.HALF_UP);
    LimitOrder sellOrder = new LimitOrder(
            Order.OrderType.ASK,amount, this.currency, ID, new Date(), price);


    exchange.getTradeService().placeLimitOrder(sellOrder);
  }

  private static String getenv(String key) {
    String value = System.getenv(key);
    if (value == null || value.equals(""))
      throw new NullPointerException("Env variable '" + key + "' is not defined");
    return value;
  }

}
