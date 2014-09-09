package com.butent.bee.client.modules.service;

import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.timeboard.TimeBoardHelper;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ServiceObjectWrapper {

  private static final String idColumn = Data.getIdColumn(VIEW_SERVICE_OBJECTS);

  private static final String categoryLabel = Data.getColumnLabel(VIEW_SERVICE_OBJECTS,
      COL_SERVICE_CATEGORY);
  private static final String addressLabel = Data.getColumnLabel(VIEW_SERVICE_OBJECTS,
      COL_SERVICE_ADDRESS);
  private static final String customerLabel = Data.getColumnLabel(VIEW_SERVICE_OBJECTS,
      COL_SERVICE_CUSTOMER);
  private static final String contractorLabel = Data.getColumnLabel(VIEW_SERVICE_OBJECTS,
      COL_SERVICE_CONTRACTOR);

  private static final int MAX_TITLE_CRITERIA = 20;

  private static String buildTitle(SimpleRow row) {
    String t1 = TimeBoardHelper.buildTitle(
        categoryLabel, row.getValue(ALS_SERVICE_CATEGORY_NAME),
        addressLabel, row.getValue(COL_SERVICE_ADDRESS),
        customerLabel, row.getValue(ALS_SERVICE_CUSTOMER_NAME),
        contractorLabel, row.getValue(ALS_SERVICE_CONTRACTOR_NAME));

    String t2 = null;

    String criteria = row.getValue(PROP_CRITERIA);
    if (!BeeUtils.isEmpty(criteria)) {
      String[] arr = Codec.beeDeserializeCollection(criteria);

      if (arr != null) {
        List<Property> properties = new ArrayList<>();

        for (int i = 0; i < Math.min(arr.length, MAX_TITLE_CRITERIA); i++) {
          Property property = Property.restore(arr[i]);
          if (property != null) {
            properties.add(Property.restore(arr[i]));
          }
        }

        if (arr.length > MAX_TITLE_CRITERIA) {
          properties.add(new Property(BeeConst.ELLIPSIS,
              BeeUtils.bracket(arr.length - MAX_TITLE_CRITERIA)));
        }

        if (!properties.isEmpty()) {
          Object[] labelsAndValues = new Object[properties.size() * 2];

          for (int i = 0; i < properties.size(); i++) {
            Property property = properties.get(i);

            labelsAndValues[i * 2] = property.getName();
            labelsAndValues[i * 2 + 1] = property.getValue();
          }

          t2 = TimeBoardHelper.buildTitle(labelsAndValues);
        }
      }
    }

    if (BeeUtils.isEmpty(t2)) {
      return t1;
    } else {
      return BeeUtils.buildLines(t1, BeeConst.STRING_EMPTY, t2);
    }
  }

  private final Long id;

  private final String address;
  private final String contractor;
  private final String category;
  private final String customer;

  private final Long contractorId;
  private final Long categoryId;
  private final Long customerId;

  private final String title;

  private Map<SvcCalendarFilterHelper.DataType, Boolean> selected = Maps.newHashMap();
  private Map<SvcCalendarFilterHelper.DataType, Boolean> enabled = Maps.newHashMap();

  private Map<SvcCalendarFilterHelper.DataType, Boolean> wasSelected = Maps.newHashMap();
  private Map<SvcCalendarFilterHelper.DataType, Boolean> wasEnabled = Maps.newHashMap();

  private String getContractor() {
    return contractor;
  }

  private String getCategory() {
    return category;
  }

  private String getCustomer() {
    return customer;
  }

  ServiceObjectWrapper(SimpleRow row) {
    this.id = row.getLong(idColumn);
    this.address = row.getValue(COL_SERVICE_ADDRESS);
    this.contractor = row.getValue(ALS_SERVICE_CONTRACTOR_NAME);
    this.contractorId = row.getLong(COL_SERVICE_CONTRACTOR);
    this.category = row.getValue(ALS_SERVICE_CATEGORY_NAME);
    this.categoryId = row.getLong(COL_SERVICE_CATEGORY);
    this.customer = row.getValue(ALS_SERVICE_CUSTOMER_NAME);
    this.customerId = row.getLong(COL_SERVICE_CUSTOMER);

    this.title = buildTitle(row);

    for (SvcCalendarFilterHelper.DataType type : SvcCalendarFilterHelper.DataType.values()) {
      enabled.put(type, Boolean.TRUE);
    }
  }

  String getAddress() {
    return address;
  }

  Long getCategoryId() {
    return categoryId;
  }

  Long getContractorId() {
    return contractorId;
  }

  Long getCustomerId() {
    return customerId;
  }

  Long getId() {
    return id;
  }

  String getFilterListName(SvcCalendarFilterHelper.DataType type) {
    switch (type) {
      case ADDRESS:
        return getAddress();
      case CONTRACTOR:
        return getContractor();
      case CATEGORY:
        return getCategory();
      case CUSTOMER:
        return getCustomer();
      default:
        return getAddress();
    }
  }

  String getTitle() {
    return title;
  }

  boolean isEnabled(SvcCalendarFilterHelper.DataType type) {
    return BeeUtils.unbox(enabled.get(type));
  }

  boolean isSelected(SvcCalendarFilterHelper.DataType type) {
    return BeeUtils.unbox(selected.get(type));
  }

  void restoreState(SvcCalendarFilterHelper.DataType type) {
    selected.put(type, wasSelected.get(type));
    enabled.put(type, wasEnabled.get(type));
  }

  void saveState(SvcCalendarFilterHelper.DataType type) {
    wasSelected.put(type, selected.get(type));
    wasEnabled.put(type, enabled.get(type));
  }

  void setEnabled(SvcCalendarFilterHelper.DataType type, boolean value) {
    this.enabled.put(type, Boolean.valueOf(value));
  }

  void setSelected(SvcCalendarFilterHelper.DataType type, boolean value) {
    this.selected.put(type, Boolean.valueOf(value));
  }
}
