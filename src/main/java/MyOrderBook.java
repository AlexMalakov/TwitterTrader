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
  public HeapNode<T> goTo(int index, int currentPos);
  public BinHeap<T> heapify(); //ugly, need to have parent and children potentially swap
  public BinHeap<T> heapifyNode(HeapNode<T> node);
  public BinHeap<T> heapifyEmpty(EmptyNode<T> node);
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
    HeapNode<T> newStart = this.goTo(index, 1);
    newStart.assignChildren(leftSave,rightSave);
    newStart.heapify();
    return null;
  }

  private void assignChildren(BinHeap<T> newLeft, BinHeap<T> newRight) {
    this.left = newLeft;
    this.right = newRight;
  }

  @Override
  public HeapNode<T> goTo(int index, int currentPos) {
    if(index == currentPos) {
      return this;
    }
    int math = index;
    while(!(math == currentPos*2 || math == currentPos*2 + 1)) {
      math = math/2;
    }
    if(math%2 == 0) {
      return this.left.goTo(index,math);
    }else {
      return this.right.goTo(index,math);
    }
  }

  @Override
  public BinHeap<T> heapify() {
    this.left.heapifyNode(this);
    this.right.heapifyNode(this);
    return this;///////////////////////////////
  }

  @Override
  public BinHeap<T> heapifyNode(HeapNode<T> node) {
    return null;
  }

  @Override
  public BinHeap<T> heapifyEmpty(EmptyNode<T> node) {
    return null;
  }

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
  public HeapNode<T> goTo(int index, int currentPos) {
    throw new IndexOutOfBoundsException("cannot access an element not in the heap");
  }

  @Override
  public BinHeap<T> heapify() {
    return null;
  }

  @Override
  public BinHeap<T> heapifyNode(HeapNode<T> node) {
    return null;
  }

  @Override
  public BinHeap<T> heapifyEmpty(EmptyNode<T> node) {
    return null;
  }


}
