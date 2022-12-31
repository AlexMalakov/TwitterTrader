package malakov.tradingbot;

import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import io.reactivex.disposables.Disposable;
import malakov.tradingbot.orderbook.Exchange;
import malakov.tradingbot.twitter.TwitterExplorer;

public class Bot implements Closeable {

  private enum State {
          INIT, FIND_INDICATOR,BUY, WAIT_FOR_FULFILLED_BID, SELL, WAIT_FOR_FULFILLED_ASK, LEAVE
  }

  private State state;
  private final Exchange exchange;
  private final BigDecimal tradePrice;
  private final TwitterExplorer indicatorFinder;


  public Bot(double tradePrice) {

    this.tradePrice = new BigDecimal(tradePrice);
    exchange = new Exchange(CurrencyPair.BTC_USD);
    this.indicatorFinder = new TwitterExplorer("a", "b");
    this.state = State.INIT;
  }

  public void init() {
    this.exchange.createSubscriptions(this);
    state = State.FIND_INDICATOR;
  }

  public synchronized void shouldTrade(OrderBook book) {
    if(state == State.BUY) {
      try{
        this.exchange.attemptBuy(this.tradePrice, book);
        state = State.WAIT_FOR_FULFILLED_BID;
      }catch(IOException ex) {
        System.err.println("ATTEMPTED BUY FAILED");
        state = State.LEAVE;
      }
    } else if(state == State.SELL) {
      try {
        this.exchange.attemptSell(this.tradePrice, book);
        state = State.WAIT_FOR_FULFILLED_ASK;
      }catch(IOException exception) {
        System.err.println("ATTEMPTED SELL FAILED");
        state = State.LEAVE;
      }
    }
  }


  public void orderUpdates(Order order) {
    if(state == State.WAIT_FOR_FULFILLED_BID && order.getStatus().equals(Order.OrderStatus.FILLED)) {
      state = State.LEAVE;
    }

    if(state == State.WAIT_FOR_FULFILLED_ASK && order.getStatus().equals(Order.OrderStatus.FILLED)) {
      this.state = State.SELL;
    }
  }

  public void tradeUpdates(Trade trade) {
    System.out.println("COMPLETED TRADE: " + trade);
  }

  @Override
  public void close() {
    this.exchange.closeSubscriptions();
  }

  public static void main(String[] args) {
    Bot bot = new Bot(5);
    bot.init();
  }
}
