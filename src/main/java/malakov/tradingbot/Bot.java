package malakov.tradingbot;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;

import malakov.tradingbot.orderbook.Exchange;
import malakov.tradingbot.tradeindicator.Indicator;
import malakov.tradingbot.tradeindicator.IndicatorHandler;

public class Bot implements IndicatorHandler, Closeable {

  public enum State {
      INIT, FIND_INDICATOR,BUY, WAIT_FOR_FULFILLED_BID, SELL, WAIT_FOR_FULFILLED_ASK, LEAVE
  }

  private State state;
  private final Exchange exchange;
  private final BigDecimal tradePrice;
  private final Indicator indicatorFinder;


  public Bot(double tradePrice, Indicator indicator, Exchange exchange) {

    this.tradePrice = new BigDecimal(tradePrice);
    this.exchange = exchange;
    this.indicatorFinder = indicator;
    this.state = State.INIT;
  }

  public State getState() {
    return state;
  }

  public void init() {
    this.exchange.createSubscriptions(this);

    // start twitter thread search thread
    this.state = State.FIND_INDICATOR;
    this.indicatorFinder.init(this);
  }

  public boolean isRunning () {
    return state != State.LEAVE;
  }

  @Override
  public void onPositiveTrend() {
    if (state == State.FIND_INDICATOR)
      state = State.BUY;
  }

  public synchronized void onOrderBookChanged(OrderBook book) {
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


  public void onOrderChanged(Order order) {
    if(order.getStatus().equals(Order.OrderStatus.FILLED)) {
      if(state == State.WAIT_FOR_FULFILLED_ASK) {
        System.out.println("LEAVING");
        state = State.LEAVE;
      } else if(state == State.WAIT_FOR_FULFILLED_BID) {
        System.out.println("GOING TO SELL");
        this.state = State.SELL;
      }
    }
  }

  public void onTradeUpdated(Trade trade) {
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
