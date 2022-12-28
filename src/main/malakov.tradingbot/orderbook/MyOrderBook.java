package orderbook;

import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;


import java.math.BigDecimal;
import java.util.ArrayList;
//represents a dynamically updated order book
public class MyOrderBook {

  //private Map<BigDecimal,BigDecimal> bids;
  //private Map<BigDecimal, BigDecimal> asks;

  private BinaryHeap<MyLimitOrder> bids;
  private BinaryHeap<MyLimitOrder> asks;

  public MyOrderBook() {

  }

  //calculates the price of an agressive sale of a certain size
  public BigDecimal calculateSellPriceSize(float amountBTC) {
    BigDecimal sum = new BigDecimal(0);
    BigDecimal btcSize = new BigDecimal(amountBTC);
    ArrayList<MyLimitOrder> reinsertMe = new ArrayList<>();
    while(!this.asks.isEmpty()) {
      MyLimitOrder order = asks.extractMin();
      reinsertMe.add(order);
      if(btcSize.subtract(order.getAmount()).compareTo(new BigDecimal(0)) < 0) {
        sum = sum.add(btcSize.multiply(order.getPrice()));
        break;
      }else {
        btcSize = btcSize.subtract(order.getAmount());
        sum = sum.add(order.getAmount().multiply(order.getPrice()));
      }
    }
    for(MyLimitOrder order:reinsertMe) {
      this.asks.insert(order);
    }
    return sum;
  }
  //TODO: update

  //calculates the price of an agressive purchase of a certain size
  public BigDecimal calculateBuyPriceSize(float amountBTC) {
    BigDecimal sum = new BigDecimal(0);
    BigDecimal btcSize = new BigDecimal(amountBTC);
    ArrayList<MyLimitOrder> reinsertMe = new ArrayList<>();
    while(!this.bids.isEmpty()) {
      MyLimitOrder order = bids.extractMin();
      reinsertMe.add(order);
      if(btcSize.subtract(order.getAmount()).compareTo(new BigDecimal(0)) < 0) {
        sum = sum.add(btcSize.multiply(order.getPrice()));
        break;
      }else {
        btcSize = btcSize.subtract(order.getAmount());
        sum = sum.add(order.getAmount().multiply(order.getPrice()));
      }
    }
    for(MyLimitOrder order:reinsertMe) {
      this.bids.insert(order);
    }
    return sum;
  }

  //given an XChange orderbook, maps price to amount for bid and ask
  public void update(OrderBook book) {
    for(LimitOrder order: book.getBids()) {
      MyLimitOrder ord = new MyLimitOrder(order);
      if(bids.contains(ord)) {
        bids.update(ord);
      }else {
        bids.insert(ord);
      }
    }
    for(LimitOrder order: book.getAsks()) {
      MyLimitOrder ord = new MyLimitOrder(order);
      if(asks.contains(ord)) {
        asks.update(ord);
      }else {
        asks.insert(ord);
      }
    }
  }

  @Override
  public String toString() {
    return "bid info: " + this.bids.toString() + " asks info: " + this.asks.toString();
  }
}


