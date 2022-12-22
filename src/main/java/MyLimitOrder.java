import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.util.Objects;

public class MyLimitOrder {
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

  public boolean sameAs(MyLimitOrder other) {
    return this.amount.compareTo(other.getAmount()) == 0
            && this.price.compareTo(other.getPrice()) == 0;
  }
}
