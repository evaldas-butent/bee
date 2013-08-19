package com.butent.bee.shared.modules.ec;

public interface LocalizableEcMessages {

  String ecCategoryMigrate(String source, String destination);

  String ecOrderId(String id);

  String ecOrderTotal(String amount, String currency);

  String ecSearchDidNotMatch(String query);
}
