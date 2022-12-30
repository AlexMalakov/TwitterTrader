package malakov.tradingbot.tradestate;

public class EnteringPositionState extends TradeState {


  public EnteringPositionState(TradeStateContext context) {
    super(context);
  }

  @Override
  //make purchase at current market price
  public void onEnter() {

  }

  @Override
  //monitor price to see when a buy is needed
  public void update() {
    //track to see how filled
  }

  @Override
  public void checkSwapState() {
    //once our order is full, swap to sell
  }

  @Override
  public void onExit() {
    //set average price
  }
}
