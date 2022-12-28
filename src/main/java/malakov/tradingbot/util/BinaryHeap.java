package malakov.tradingbot.util;

import java.util.ArrayList;

public class BinaryHeap<T extends Comparable<T>> {
  ArrayList<T> list;

  public BinaryHeap() {
    list = new ArrayList<>();
  }

  public void insert(T element) {
    list.add(element);
    this.heapifyUp(this.list.size() - 1);
  }

  public T extractMin() throws IndexOutOfBoundsException{
    if(this.isEmpty()) {
      throw new IndexOutOfBoundsException("cannot extract an element from an empty heap");
    }

    T returnMe = this.list.get(0);
    this.list.set(0, list.get(list.size() - 1));
    list.remove(list.size() - 1);
    this.heapifyDown(0);
    return returnMe;
  }

  public void update(T newValue) {
    int index = this.find(newValue);
    if (index == -1) {
      throw new IllegalArgumentException("doesn't have this element");
    }
    this.list.set(index, newValue);
    this.heapifyUp(index);
    this.heapifyDown(index);
  }

  public void remove(T target) {
    int index = this.find(target);
    if(index == -1) {
      throw new IllegalArgumentException("doesn't have this element to remove");
    }
    this.list.set(index, this.list.get(this.list.size()-1));
    this.list.remove(this.list.size()-1);
    this.heapifyDown(index);
  }

  private int find(T value) {
    int index = 0;
    while (index < this.list.size()) {
      if (this.list.get(index).compareTo(value) == 0) {
        return index;
      }
      index++;
    }
    return -1;
  }

  public boolean contains(T order) {
    for (T ord : this.list) {
      if (ord.compareTo(order) == 0) {
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
    while (position < list.size()) {
      int smallest = this.findSmallest(position, position * 2 + 1, position * 2 + 2);
      if (smallest != position) {
        T swapper = this.list.get(position);
        this.list.set(position, this.list.get(smallest));
        this.list.set(smallest, swapper);
      } else {
        break;
      }
      position = smallest;
    }
  }

  private int findSmallest(int parent, int child1, int child2) {
    if (child2 < this.list.size()
            && this.list.get(child2).compareTo(this.list.get(parent)) < 0
            && this.list.get(child2).compareTo(this.list.get(child1)) < 0) {
      return child2;
    }

    if (child1 < this.list.size()
            && this.list.get(child1).compareTo(this.list.get(parent)) < 0) {
      return child1;
    }
    return parent;
  }

  private void heapifyUp(int startIndex) {
    int position = startIndex;
    while (true) {
      if (list.get(position).compareTo(list.get((position - 1) / 2)) < 0) {
        T swapper = list.get(position);
        list.set(position, list.get((position - 1) / 2));
        list.set((position - 1) / 2, swapper);
      } else {
        break;
      }
      position = (position - 1) / 2;
    }
  }

  @Override
  public String toString() {
    String smallestInfo = "";
    if(!isEmpty()) {
      smallestInfo = " and with smallest element " + this.list.get(0).toString();
    }
    return "heap of size: " + this.list.size() + smallestInfo;
  }
}
