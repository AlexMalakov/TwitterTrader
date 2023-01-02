package malakov.tradingbot.tradeindicator;

import java.io.IOException;

public interface Indicator {

  void init(IndicatorHandler event);

  void close() throws IOException;

  //public boolean searchForIndicator();
}
