package malakov.tradingbot.tradeindicator;

import java.io.IOException;

import malakov.tradingbot.Bot;

public interface Indicator {
  public void init();

  public void close() throws IOException;

  public void searchForIndicator(Bot bot);
}
