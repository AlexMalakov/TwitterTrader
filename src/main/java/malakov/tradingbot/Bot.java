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
    INIT,
    /** Waiting for positive trend on twitter */
    FIND_INDICATOR,
    /** Positive trend found, waiting for order book to determine price of BUY order */
    BUY,

    /** Waiting for BUY order to fill */
    WAIT_FOR_FULFILLED_BID,

    /** Wait for opportunity to make profit by SELLING what we bought */
    SELL,

    /** Waiting for SELL order to fill */
    WAIT_FOR_FULFILLED_ASK,


    EXIT,

    ABORT
  }

  private State state;
  private final Exchange exchange;
  private final BigDecimal tradeAmount;
  private final Indicator indicatorFinder;


  public Bot(double tradeAmount, Indicator indicator, Exchange exchange) {

    this.tradeAmount = new BigDecimal(tradeAmount);
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

  public boolean isRunning() {
    return state != State.EXIT && state != State.ABORT;
  }

  @Override
  public void onPositiveTrend() {
    if (state == State.FIND_INDICATOR)
      state = State.BUY;
  }

  public synchronized void onOrderBookChanged(OrderBook book) {
    if (state == State.BUY) {
      try {
        this.exchange.attemptBuy(this.tradeAmount, book);
        state = State.WAIT_FOR_FULFILLED_BID;
      } catch (IOException ex) {
        System.err.println("ATTEMPTED BUY FAILED: " + ex.getMessage());
        state = State.ABORT;
      }
    } else if (state == State.SELL) {
      System.out.println("SELLING");
      try {
        this.exchange.attemptSell(this.tradeAmount, book);
        state = State.WAIT_FOR_FULFILLED_ASK;
      } catch (IOException ex) {
        System.err.println("ATTEMPTED SELL FAILED: " + ex.getMessage());
        state = State.ABORT;
      }
    }
  }


  public void onOrderChanged(Order order) {
    System.out.println("Bot.onOrderChanged(" + order + ")");
    if (order.getStatus().equals(Order.OrderStatus.FILLED)) {
      if (state == State.WAIT_FOR_FULFILLED_ASK) {
        System.out.println("FINISHING");
        state = State.EXIT;
      } else if (state == State.WAIT_FOR_FULFILLED_BID) {
        System.out.println("GOING TO SELL");
        this.state = State.SELL;
      }
    }
  }

  public void onTradeUpdated(Trade trade) {
    System.out.println("Bot.onTradeUpdated(" + trade + ")");
  }

  @Override
  public void close() throws IOException {
    System.out.println("CLOSING");
    this.exchange.close();
    this.indicatorFinder.close();
  }

//  public static void main(String[] args) {
//    Bot bot = new Bot(5, new TwitterExplorer("a","b", "c", "d", false,"bearer"));
//    bot.init();
//  }
}
