package malakov.tradingbot.tradestate;

import org.knowm.xchange.dto.Order;

import java.util.ArrayList;

public class TradeStateContext {
  private final ArrayList<Order> positions;

  private TradeStateMachine state;

  public TradeStateContext() {
    positions = new ArrayList<>();
  }

  public void init() {
    this.state = new InitializingState(this);
  }

  public void setCurrentState(TradeStateMachine state) {
    this.state = state;
  }
}
