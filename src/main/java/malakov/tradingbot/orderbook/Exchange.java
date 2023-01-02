package malakov.tradingbot.orderbook;

import org.knowm.xchange.dto.marketdata.OrderBook;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;

import malakov.tradingbot.Bot;

public interface Exchange extends Closeable {

  public void createSubscriptions(Bot bot);
  public void attemptBuy(BigDecimal amount, OrderBook book) throws IOException;
  public void attemptSell(BigDecimal amount, OrderBook book) throws IOException;
  public void close();
}
