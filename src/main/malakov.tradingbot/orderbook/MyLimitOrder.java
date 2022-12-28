package orderbook;

import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;

public class MyLimitOrder implements Comparable<MyLimitOrder>{
  private BigDecimal amount;
  private BigDecimal price;


  public MyLimitOrder(BigDecimal price, BigDecimal amount) {
    this.price = price;
    this.amount = amount;
  }

  public MyLimitOrder(LimitOrder order) {
    this.price = order.getLimitPrice();
    this.amount = order.getRemainingAmount();
  }


  public int compareTo(MyLimitOrder other) {
    return this.price.compareTo(other.getPrice());
  }

  public void setPrice(BigDecimal newPrice) {
    price = newPrice;
  }
  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal newAmount) {
    amount = newAmount;
  }

  @Override
  public String toString() {
    return "limit order with price of " + this.price.toString() + " for " + this.amount.toString() + "btc";
  }
}
