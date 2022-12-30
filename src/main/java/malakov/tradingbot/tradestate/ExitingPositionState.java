package malakov.tradingbot.tradestate;

public class ExitingPositionState extends TradeState {

  public ExitingPositionState(TradeStateContext context) {
    super(context);
  }

  @Override
  //set limits, put sale up
  public void onEnter() {

  }

  @Override
  public void update() {
    //track state of sale
  }

  @Override
  public void checkSwapState() {

  }


  @Override
  public void onExit() {
    //close subscriptions
  }
}
