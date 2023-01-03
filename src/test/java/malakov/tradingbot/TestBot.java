package malakov.tradingbot;

import malakov.tradingbot.orderbook.Exchange;
import malakov.tradingbot.tradeindicator.Indicator;
import malakov.tradingbot.tradeindicator.IndicatorHandler;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.instrument.Instrument;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestBot {

  private static final Instrument BTCUSD = CurrencyPair.BTC_USD;

  @Test
  public void simple() throws IOException {
    Indicator indicator = new MockIndicator();
    Exchange exchange = new MockExchange();

    Bot bot = new Bot (1, indicator, exchange);
    assertEquals (Bot.State.INIT, bot.getState());

    bot.init();
    assertEquals (Bot.State.FIND_INDICATOR, bot.getState());

    bot.onPositiveTrend();
    assertEquals (Bot.State.BUY, bot.getState());

    bot.onOrderBookChanged(makeOrderBook(15000, 16000));
    assertEquals (Bot.State.WAIT_FOR_FULFILLED_BID, bot.getState());


    bot.onOrderChanged(makeOrder(Order.OrderStatus.NEW));
    bot.onOrderChanged(makeOrder(Order.OrderStatus.FILLED));
    bot.onTradeUpdated(makeTrade());
    assertEquals (Bot.State.SELL, bot.getState());

    bot.onOrderBookChanged(makeOrderBook(15000, 16000));
    assertEquals(Bot.State.WAIT_FOR_FULFILLED_ASK, bot.getState());

    bot.onOrderChanged(makeOrder(Order.OrderStatus.NEW));
    bot.onOrderChanged(makeOrder(Order.OrderStatus.FILLED));
    bot.onTradeUpdated(makeTrade());
    assertEquals(Bot.State.EXIT, bot.getState());


    bot.close();
  }

  @NotNull
  private Order makeOrder(Order.OrderStatus orderStatus) {
    Order filled = new LimitOrder(
            Order.OrderType.BID, BigDecimal.TEN, BTCUSD, "hello",
            new Date(),BigDecimal.ONE, BigDecimal.ONE,BigDecimal.TEN,BigDecimal.ZERO, orderStatus);
    return filled;
  }

  private static OrderBook makeOrderBook(double bestBid, double bestAsk) {
    List<LimitOrder> asks = Arrays.asList( makeOrder (Order.OrderType.ASK, bestAsk) );
    List<LimitOrder> bids = Arrays.asList( makeOrder (Order.OrderType.BID, bestBid) );
    return new OrderBook(new Date(), asks, bids);
  }

  private static LimitOrder makeOrder (Order.OrderType type, double price) {
    return new LimitOrder(type, new BigDecimal(1), BTCUSD, "dummyorder" + System.nanoTime(), new Date(), new BigDecimal(price));
  }

  private static Trade makeTrade() {
    return new Trade(Order.OrderType.BID,BigDecimal.TEN,BTCUSD,BigDecimal.ONE,new Date(),"hello","id","also id");
  }

}


class MockIndicator implements Indicator {
  @Override
  public void init(IndicatorHandler event) {
    // do nothing
  }

  @Override
  public void close() throws IOException {
    // do nothing
  }


}

class MockExchange implements Exchange {

  @Override
  public void createSubscriptions(Bot bot) {

  }

  @Override
  public void attemptBuy(BigDecimal amount, OrderBook book) throws IOException {

  }

  @Override
  public void attemptSell(BigDecimal amount, OrderBook book) throws IOException {

  }

  @Override
  public void close() {
    //does nothing
  }
}