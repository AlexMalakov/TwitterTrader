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
  BinHeap min;
  private int nextInsert;

  public BinaryHeap() {
    min = new EmptyNode();
    nextInsert = 1; //yes its a sin but it makes my life easier
  }

  public void insert(MyLimitOrder element) {
    this.min = this.min.append(element,nextInsert,1);
    nextInsert++;
  }

  public MyLimitOrder extractMin() {
    MyLimitOrder result = this.min.getMin();
    this.nextInsert--;
    this.min = this.min.removeAndSwap(this.nextInsert);
    this.min = this.min.heapifyDown();
    return result;
  }

  public void updateN(int index, MyLimitOrder newValue) {
    this.min.goTo(index,1).setMin(newValue);
  }

  public boolean isEmpty() {
    return this.min.isEmpty();
  }
}

interface BinHeap {
  public BinHeap append(MyLimitOrder elem, int index, int current);
  public MyLimitOrder getMin();
  public BinHeap removeAndSwap(int index);
  public HeapNode goTo(int index, int currentPos);
  public BinHeap heapifyDown(); //ugly, need to have parent and children potentially swap
//  public BinHeap heapifyUp();
  public HeapNode returnSmallest(HeapNode node, BinHeap heap);
  public HeapNode returnSmallest(HeapNode node);
  public HeapNode getSmallest(HeapNode node1, HeapNode node2);
  public boolean isEmpty();
}

class HeapNode implements BinHeap{
  MyLimitOrder value;
  BinHeap left;
  BinHeap right;

  public HeapNode(MyLimitOrder element) {
    value = element;
    left = new EmptyNode();
    right = new EmptyNode();
  }

  public BinHeap append(MyLimitOrder element, int index, int current) {
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
  public MyLimitOrder getMin() {
    return this.value;
  }

  public void setMin(MyLimitOrder order) {
    this.value = order;
  }

  @Override
  public BinHeap removeAndSwap(int index) {
    BinHeap leftSave = left;
    BinHeap rightSave = right;
    HeapNode newStart = this.goTo(index, 1);
    newStart.assignChildren(leftSave,rightSave);
    newStart.heapifyDown();
    return null;
  }

  private void assignChildren(BinHeap newLeft, BinHeap newRight) {
    this.left = newLeft;
    this.right = newRight;
  }

  @Override
  public HeapNode goTo(int index, int currentPos) {
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
  public BinHeap heapifyDown() {
    HeapNode min = this.left.returnSmallest(this, this.right);
    if(min.getMin().sameAs(this.getMin())) {
      return this;
    }

    if(min.swapWithParent(this)) {
      min.left = min.left.heapifyDown();
    } else {
      min.right = min.right.heapifyDown();
    }

    return min;

  }

  public boolean swapWithParent(HeapNode parent) {
    BinHeap left = this.left;
    BinHeap right = this.right;

    if(parent.left.getMin().sameAs(this.getMin())) {
      this.left = parent;
      this.right = parent.right;

      parent.left = left;
      parent.right = right;

      return true;
    }else {
      this.right = parent;
      this.left = parent.left;

      parent.left = left;
      parent.right = right;

      return false;
    }



  }

  @Override
  public HeapNode returnSmallest(HeapNode node, BinHeap heap) {
    return heap.getSmallest(node,this);
  }

  @Override
  public HeapNode returnSmallest(HeapNode node) {
    if(this.getMin().compareTo(node.getMin()) < 0) {
      return this;
    }else {
      return node;
    }
  }

  @Override
  public HeapNode getSmallest(HeapNode node1, HeapNode node2) {
    HeapNode oneVsTwo = node1.returnSmallest(node2);
    if(this.getMin().compareTo(oneVsTwo.getMin()) < 0) {
      return this;
    }else {
      return oneVsTwo;
    }
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

}

class EmptyNode implements BinHeap{



  public BinHeap append(MyLimitOrder element, int index, int current) {
    return new HeapNode(element);
  }

  public MyLimitOrder getMin() {
    throw new IllegalArgumentException("empty heap cannot have a minimum");
  }

  @Override
  public BinHeap removeAndSwap(int index) {
    throw new IllegalArgumentException("does not have min to remove");
  }

  @Override
  public HeapNode goTo(int index, int currentPos) {
    throw new IndexOutOfBoundsException("cannot access an element not in the heap");
  }

  @Override
  public BinHeap heapifyDown() {
    return this;
  }


  @Override
  public HeapNode returnSmallest(HeapNode node, BinHeap heap) {
    return heap.returnSmallest(node);
  }

  @Override
  public HeapNode returnSmallest(HeapNode node) {
    return node;
  }

  @Override
  public HeapNode getSmallest(HeapNode node1, HeapNode node2) {
    return node1.returnSmallest(node2);
  }

  @Override
  public boolean isEmpty() {
    return true;
  }


}
