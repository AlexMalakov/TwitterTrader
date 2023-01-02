package malakov.tradingbot;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;

import malakov.tradingbot.orderbook.XchangeExchange;
import malakov.tradingbot.tradeindicator.Indicator;

public class Bot implements Closeable {

  private enum State {
          INIT, FIND_INDICATOR,BUY, WAIT_FOR_FULFILLED_BID, SELL, WAIT_FOR_FULFILLED_ASK, LEAVE
  }

  private State state;
  private final XchangeExchange exchange;
  private final BigDecimal tradePrice;
  private final Indicator indicatorFinder;


  public Bot(double tradePrice, Indicator indicator) {

    this.tradePrice = new BigDecimal(tradePrice);
    this.exchange = new XchangeExchange(CurrencyPair.BTC_USD);
    this.indicatorFinder = indicator;
    this.state = State.INIT;
  }

  public void init() {
    this.exchange.createSubscriptions(this);
    this.state = State.FIND_INDICATOR;
    this.indicatorFinder.init();
  }

  public void start() {
    if(this.indicatorFinder.searchForIndicator()) {
      state = State.BUY;
    }else {
      state = State.LEAVE;
    }
  }

  public synchronized void shouldTrade(OrderBook book) {
    if(state == State.BUY) {
      System.out.println("BUYING");
      try{
        this.exchange.attemptBuy(this.tradePrice, book);
        state = State.WAIT_FOR_FULFILLED_BID;
      }catch(IOException ex) {
        System.err.println("ATTEMPTED BUY FAILED");
        state = State.LEAVE;
      }
    } else if(state == State.SELL) {
      System.out.println("SELLING");
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
      System.out.println("LEAVING");
      state = State.LEAVE;
    }

    if(state == State.WAIT_FOR_FULFILLED_ASK && order.getStatus().equals(Order.OrderStatus.FILLED)) {
      System.out.println("GOING TO SELL");
      this.state = State.SELL;
    }
  }

  public void tradeUpdates(Trade trade) {
    System.out.println("COMPLETED TRADE: " + trade);
  }

  @Override
  public void close() throws IOException{
    System.out.println("CLOSING");
    this.exchange.close();
    this.indicatorFinder.close();
  }

//  public static void main(String[] args) {
//    Bot bot = new Bot(5, new TwitterExplorer("a","b", "c", "d", false,"bearer"));
//    bot.init();
//  }
}
