package malakov.tradingbot.tradestate;

public abstract class TradeState implements TradeStateMachine{
  private final TradeStateContext context;

  public TradeState(TradeStateContext context) {
    this.context = context;
  }

  @Override
  public void swapTo(TradeStateMachine newState) {
    this.onExit();
    context.setCurrentState(newState);
    newState.onEnter();
  }
}
