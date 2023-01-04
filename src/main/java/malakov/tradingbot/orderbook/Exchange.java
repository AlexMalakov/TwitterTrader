package malakov.tradingbot.orderbook;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;

import malakov.tradingbot.Bot;

public interface Exchange extends Closeable {
  public void createSubscriptions(Bot bot);
  public String attemptLimitOrder(Order.OrderType type, BigDecimal amount, OrderBook book, BigDecimal price) throws IOException;
  public BigDecimal getMarketPrice(Order.OrderType typeOfOrder, BigDecimal amountToBuy, OrderBook book);
  public void attemptMarketOrder(Order.OrderType type, BigDecimal amount, OrderBook book) throws IOException;
  public void cancelOrder(String id) throws IOException;
}
