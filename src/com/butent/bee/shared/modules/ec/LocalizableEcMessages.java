package com.butent.bee.shared.modules.ec;

public interface LocalizableEcMessages {

  String ecCategoryMigrate(String source, String destination);

  String ecOrderId(String id);

  String ecOrderTotal(String amount, String currency);

  String ecRemoveCartItem(String cart, String item);

  String ecSearchDidNotMatch(String query);

  String ecUpdateCartItem(String cart, String item, int quantity);
}
