import org.checkerframework.checker.units.qual.A;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import sun.invoke.empty.Empty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;

//represents a dynamically updated order book
public class MyOrderBook {

  //private Map<BigDecimal,BigDecimal> bids;
  //private Map<BigDecimal, BigDecimal> asks;

  private BinaryHeap bids;
  private BinaryHeap asks;

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
  /*
  //calculates the price of an agressive purchase of a certain size
  public BigDecimal calculateBuyPriceSize(float amountBTC) {
    ArrayList<BigDecimal> sorted = new ArrayList<>(bids.keySet());
    Collections.sort(sorted);

    BigDecimal sum = new BigDecimal(0);
    BigDecimal btcSize = new BigDecimal(amountBTC);
    for(BigDecimal value: sorted) {
      if(btcSize.subtract(bids.get(value)).compareTo(new BigDecimal(0)) < 0) {
        sum = sum.add(btcSize.multiply(value));
        break;
      }else {
        btcSize = btcSize.subtract(bids.get(value));
        sum = sum.add(bids.get(value).multiply(value));
      }

    }
    return sum;
  }

  //given an XChange orderbook, maps price to amount for bid and ask
  public void update(OrderBook book) {
    for(LimitOrder order: book.getBids()) {
      if(bids.contains(new MyLimitOrder(order))) {
        bids.
      }
      bids.put(order.getLimitPrice(), order.getRemainingAmount());//not sure if this is correct
    }
    for(LimitOrder order: book.getAsks()) {
      asks.put(order.getLimitPrice(), order.getOriginalAmount());//not sure if this is correct
    }
  } */
}


class BinaryHeap {
  ArrayList<MyLimitOrder> list;

  public BinaryHeap() {
    list = new ArrayList<>();
  }

  public void insert(MyLimitOrder element) {
    list.add(element);
    this.heapifyUp(this.list.size()-1);
  }

  public MyLimitOrder extractMin() {
    MyLimitOrder returnMe = this.list.get(0);
    this.list.set(0, list.get(list.size()-1));
    list.remove(list.size()-1);
    this.heapifyDown(0);
    return returnMe;
  }

  public void updateN(int index, MyLimitOrder newValue) {
    this.list.set(index,newValue);
    this.heapifyUp(index);
    this.heapifyDown(index);
  }

  public boolean contains(MyLimitOrder order) {
    for(MyLimitOrder ord: this.list) {
      if(ord.sameAs(order)) {
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return this.list.size() == 0;
  }

  private void heapifyDown(int startIndex) {
    int position = startIndex;
    while(position < list.size()) {
      int smallest = this.findSmallest(position,position*2 +1, position*2+2);
      if(smallest != position) {
        MyLimitOrder swapper = this.list.get(position);
        this.list.set(position, this.list.get(smallest));
        this.list.set(smallest, swapper);
      }else {
        break;
      }
      position = smallest;
    }
  }

  private int findSmallest(int parent, int child1, int child2) {
    if(child2 < this.list.size()
            && this.list.get(child2).getPrice().compareTo(this.list.get(parent).getPrice()) < 0
            && this.list.get(child2).getPrice().compareTo(this.list.get(child1).getPrice()) < 0) {
      return child2;
    }

    if(child1 < this.list.size()
            && this.list.get(child1).getPrice().compareTo(this.list.get(parent).getPrice()) < 0) {
      return child1;
    }
    return parent;
  }

  private void heapifyUp(int startIndex) {
    int position = startIndex;
    while(true) {
      if(list.get(position).getPrice().compareTo(list.get((position-1)/2).getPrice()) < 0) {
        MyLimitOrder swapper = list.get(position);
        list.set(position, list.get((position-1)/2));
        list.set((position-1)/2,swapper);
      }else {
        break;
      }
      position = (position-1)/2;
    }
  }
}
