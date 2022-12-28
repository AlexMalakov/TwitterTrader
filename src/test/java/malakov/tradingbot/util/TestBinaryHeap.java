package malakov.tradingbot.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestBinaryHeap {

  BinaryHeap<Item> heap = new BinaryHeap<>();

  public void reset() {
    while(!heap.isEmpty()) {
      heap.extractMin();
    }
  }

  @Test
  public void testIsEmpty() {
    this.reset();
    assertTrue("heap is empty", heap.isEmpty());
    heap.insert(new Item(0));
    assertFalse("heap is not empty", heap.isEmpty());
    heap.extractMin();
    assertTrue("heap is empty", heap.isEmpty());
  }

  @Test
  public void testExtractMin() {
    this.reset();
    heap.insert(new Item(0));
    assertEquals(0, heap.extractMin().value);
    heap.insert(new Item(2));
    heap.insert(new Item(1));
    heap.insert(new Item(3));
    assertEquals(1, heap.extractMin().value);
    assertEquals(2, heap.extractMin().value);
    heap.insert(new Item(0));
    heap.insert(new Item(40000));
    assertEquals(0, heap.extractMin().value);
    assertEquals(3, heap.extractMin().value);
    assertEquals(40000, heap.extractMin().value);

    heap.insert(new Item(1));
    heap.insert(new Item(2));
    heap.insert(new Item(4));
    heap.insert(new Item(3));
    heap.insert(new Item(1));
    heap.insert(new Item(2));
    heap.insert(new Item(1));

    assertEquals(1, heap.extractMin().value);
    assertEquals(1, heap.extractMin().value);
    assertEquals(1, heap.extractMin().value);
    assertEquals(2, heap.extractMin().value);
    assertEquals(2, heap.extractMin().value);
    assertEquals(3, heap.extractMin().value);
    assertEquals(4, heap.extractMin().value);
  }

  @Test
  public void testUpdate() {
    this.reset();
    Item testItem = new Item(0);
    heap.insert(testItem);
    testItem.meaningless = 3;
    heap.update(testItem);
    Item newTestItem = heap.extractMin();
    assertEquals(3,newTestItem.meaningless);
  }

  @Test
  public void testContains() {
    this.reset();
    Item doesNotExist = new Item(69);
    assertFalse(heap.contains(doesNotExist));
    Item doesExist = new Item(3);
    heap.insert(doesExist);
    heap.insert(new Item(12));
    heap.insert(new Item(3003));
    assertTrue(heap.contains(doesExist));
    assertFalse(heap.contains(doesNotExist));
    heap.extractMin();
    assertFalse(heap.contains(doesExist));
  }

  @Test
  public void testRemove() {
    this.reset();
    Item removeMe = new Item(4);

    assertTrue(heap.isEmpty());
    heap.insert(removeMe);
    assertFalse(heap.isEmpty());
    heap.remove(removeMe);
    assertTrue(heap.isEmpty());

    heap.insert(new Item(1));
    heap.insert(new Item(3));
    heap.insert(new Item(8));
    heap.insert(new Item(5));
    heap.insert(new Item(2));
    heap.insert(new Item(7));
    heap.insert(removeMe);

    heap.remove(removeMe);
    assertEquals(1, heap.extractMin().value);
    assertEquals(2, heap.extractMin().value);
    assertEquals(3, heap.extractMin().value);
    assertEquals(5, heap.extractMin().value);
    assertEquals(7, heap.extractMin().value);
    assertEquals(8, heap.extractMin().value);
    assertTrue(heap.isEmpty());


  }
}

class Item implements Comparable<Item>{

  public final int value;
  public int meaningless = 0;

  public Item(int value) {
    this.value = value;
  }

  @Override
  public int compareTo(Item o) {

    return this.value - o.value;
  }

}
