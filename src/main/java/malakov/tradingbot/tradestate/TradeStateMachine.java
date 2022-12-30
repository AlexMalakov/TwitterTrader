package malakov.tradingbot.tradestate;

public interface TradeStateMachine {

  public void onEnter();

  public void update();

  public void swapTo(TradeStateMachine newState);

  public void checkSwapState();

  public void onExit();
}
