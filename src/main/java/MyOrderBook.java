import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

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

  private TreeSet<MyLimitOrder> bids;
  private TreeSet<MyLimitOrder> asks;

  public MyOrderBook() {

  }

  //calculates the price of an agressive sale of a certain size
  public BigDecimal calculateSellPriceSize(float amountBTC) {
    BigDecimal sum = new BigDecimal(0);
    BigDecimal btcSize = new BigDecimal(amountBTC);


    for(MyLimitOrder order: asks) {
      if(btcSize.subtract(order.getAmount()).compareTo(new BigDecimal(0)) < 0) {
        sum = sum.add(btcSize.multiply(order.getPrice()));
        break;
      }else {
        btcSize = btcSize.subtract(order.getAmount());
        sum = sum.add(order.getAmount().multiply(order.getPrice()));
      }
    }
    return sum;
  }
  //TODO: update
  //calculates the price of an agressive purchase of a certain size
  /*
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
  }*/

  //given an XChange orderbook, maps price to amount for bid and ask
  //TODO: CLEAN UP
  /*
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
  }*/
}

class BinaryHeap<T> {
  BinHeap<T> min;
  private int nextInsert;

  public BinaryHeap() {
    min = new EmptyNode<>();
    nextInsert = 1; //yes its a sin but it makes my life easier
  }

  public void insert(T element) {
    this.min = this.min.append(element,nextInsert,1);
    nextInsert++;
  }

  public T extractMin() {
    T result = this.min.getMin();
    this.nextInsert--;
    this.min = this.min.removeAndSwap(this.nextInsert);
    return result;
  }
}

interface BinHeap<T> {
  public BinHeap<T> append(T elem, int index, int current);
  public T getMin();
  public BinHeap<T> removeAndSwap(int index);
  public BinHeap<T> locateIndex(int index);
}

class HeapNode<T> implements BinHeap<T>{
  T value;
  BinHeap<T> left;
  BinHeap<T> right;

  public HeapNode(T element) {
    value = element;
    left = new EmptyNode<T>();
    right = new EmptyNode<T>();
  }

  public BinHeap<T> append(T element, int index, int current) {
    int math = index;
    while(!(math == current*2 || math == current*2 + 1)) {
      math = math/2;
    }
    if(math%2 == 0) {
      this.left = this.left.append(element,index,math);
    }else {
      this.right = this.right.append(element,index,math);
    }
    return this;
  }

  @Override
  public T getMin() {
    return value;
  }

  @Override
  public BinHeap<T> removeAndSwap(int index) {
    BinHeap<T> leftSave = left;
    BinHeap<T> rightSave = right;
    return null;
  }

  @Override
  public BinHeap<T> locateIndex(int index) {
    return null;
  }

  //TODO: CLEAN UP
  /*
  public BinHeap<T> LocateAtIndex(int index) {
    int math = index;
    while(!(math == current*2 || math == current*2 + 1)) {
      math = math/2;
    }
    if(math%2 == 0) {
      this.left = this.left.append(element,index,math);
    }else {
      this.right = this.right.append(element,index,math);
    }
  }*/
}

class EmptyNode<T> implements BinHeap<T>{



  public BinHeap<T> append(T element, int index, int current) {
    return new HeapNode<T>(element);
  }

  public T getMin() {
    throw new IllegalArgumentException("empty heap cannot have a minimum");
  }

  @Override
  public BinHeap<T> removeAndSwap(int index) {
    throw new IllegalArgumentException("does not have min to remove");
  }

  @Override
  public BinHeap<T> locateIndex(int index) {
    return this;
  }
}
