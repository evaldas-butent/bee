package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TradeDocumentSums {

  private static final class Item {

    private double quantity;
    private double price;

    private double discount;
    private boolean discountIsPercent;

    private double vat;
    private boolean vatIsPercent;

    private Item() {
    }

    private boolean isValid() {
      return !isZero(quantity) && !isZero(price);
    }

    private boolean setQuantity(Double value) {
      double x = normalize(value);

      if (eq(quantity, x)) {
        return false;
      } else {
        this.quantity = x;
        return true;
      }
    }

    private boolean setPrice(Double value) {
      double x = normalize(value);

      if (eq(price, x)) {
        return false;
      } else {
        this.price = x;
        return true;
      }
    }

    private boolean setDiscount(Double value) {
      double x = normalize(value);

      if (eq(discount, x)) {
        return false;
      } else {
        this.discount = x;
        return true;
      }
    }

    private boolean setDiscountIsPercent(Boolean value) {
      boolean b = normalize(value);

      if (discountIsPercent == b) {
        return false;
      } else {
        this.discountIsPercent = b;
        return true;
      }
    }

    private boolean setVat(Double value) {
      double x = normalize(value);

      if (eq(vat, x)) {
        return false;
      } else {
        this.vat = x;
        return true;
      }
    }

    private boolean setVatIsPercent(Boolean value) {
      boolean b = normalize(value);

      if (vatIsPercent == b) {
        return false;
      } else {
        this.vatIsPercent = b;
        return true;
      }
    }
  }

  private static BeeLogger logger = LogUtils.getLogger(TradeDocumentSums.class);

  private static boolean eq(double x1, double x2) {
    return x1 == x2;
  }

  private static boolean isZero(double x) {
    return x == BeeConst.DOUBLE_ZERO;
  }

  private static boolean normalize(Boolean value) {
    return BeeUtils.isTrue(value);
  }

  private static double normalize(Double value) {
    return BeeUtils.isDouble(value) ? value : BeeConst.DOUBLE_ZERO;
  }

  private double documentDiscount;

  private TradeDiscountMode discountMode;
  private TradeVatMode vatMode;

  private final Map<Long, Item> items = new HashMap<>();

  public TradeDocumentSums() {
  }

  public TradeDocumentSums(Double discount, TradeDiscountMode discountMode, TradeVatMode vatMode) {
    this.documentDiscount = normalize(discount);

    this.discountMode = discountMode;
    this.vatMode = vatMode;
  }

  public void add(long id, Double quantity, Double price,
      Double discount, Boolean discountIsPercent, Double vat, Boolean vatIsPercent) {

    Item item = new Item();

    item.setQuantity(quantity);
    item.setPrice(price);

    item.setDiscount(discount);
    item.setDiscountIsPercent(discountIsPercent);

    item.setVat(vat);
    item.setVatIsPercent(vatIsPercent);

    items.put(id, item);
  }

  public void clearItems() {
    items.clear();
  }

  public boolean delete(Long id) {
    return items.remove(id) != null;
  }

  public boolean delete(Collection<Long> ids) {
    boolean changed = false;

    if (ids != null) {
      for (Long id : ids) {
        changed |= delete(id);
      }
    }

    return changed;
  }

  public boolean updateDocumentDiscount(Double value) {
    double x = normalize(value);

    if (eq(documentDiscount, x)) {
      return false;
    } else {
      this.documentDiscount = x;
      return true;
    }
  }

  public boolean updateDiscountMode(TradeDiscountMode value) {
    if (discountMode == value) {
      return false;
    } else {
      this.discountMode = value;
      return true;
    }
  }

  public boolean updateVatMode(TradeVatMode value) {
    if (vatMode == value) {
      return false;
    } else {
      this.vatMode = value;
      return true;
    }
  }

  public boolean updateQuantity(long id, Double value) {
    Item item = getItem(id);
    return item != null && item.setQuantity(value);
  }

  public boolean updatePrice(long id, Double value) {
    Item item = getItem(id);
    return item != null && item.setPrice(value);
  }

  public boolean updateDiscount(long id, Double value) {
    Item item = getItem(id);
    return item != null && item.setDiscount(value);
  }

  public boolean updateDiscountIsPercent(long id, Boolean value) {
    Item item = getItem(id);
    return item != null && item.setDiscountIsPercent(value);
  }

  public boolean updateVat(long id, Double value) {
    Item item = getItem(id);
    return item != null && item.setVat(value);
  }

  public boolean updateVatIsPercent(long id, Boolean value) {
    Item item = getItem(id);
    return item != null && item.setVatIsPercent(value);
  }

  private Item getItem(long id) {
    Item item = items.get(id);
    if (item == null) {
      logger.warning(NameUtils.getName(this), "item", id, "not found");
    }

    return item;
  }
}
