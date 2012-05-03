package com.butent.bee.client.ui;

import com.google.gwt.xml.client.Element;

import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

/**
 * Manages xml descriptions of forms.
 */

public class FormDescription implements HasViewName {
  
  private static final String TAG_ROW_MESSAGE = "rowMessage";
  private static final String TAG_ROW_EDITABLE = "rowEditable";
  private static final String TAG_ROW_VALIDATION = "rowValidation";

  private static final String ATTR_ASYNC_THRESHOLD = "asyncThreshold";
  private static final String ATTR_SEARCH_THRESHOLD = "searchThreshold";

  private static final String ATTR_ENABLED_ACTIONS = "enabledActions";
  private static final String ATTR_DISABLED_ACTIONS = "disabledActions";

  public static String getName(Element element) {
    Assert.notNull(element);
    return element.getAttribute(UiConstants.ATTR_NAME);
  }
  
  private final Element formElement;
  private String viewName = null;
  
  public FormDescription(Element formElement) {
    Assert.notNull(formElement);
    this.formElement = formElement;
  }

  public int getAsyncThreshold() {
    Integer asyncThreshold = XmlUtils.getAttributeInteger(getFormElement(), ATTR_ASYNC_THRESHOLD);
    if (asyncThreshold == null) {
      asyncThreshold = DataUtils.getDefaultAsyncThreshold();
    }
    return asyncThreshold;
  }

  public String getCaption() {
    String caption = getFormElement().getAttribute(UiConstants.ATTR_CAPTION);
    if (BeeUtils.isEmpty(caption)) {
      caption = getFormElement().getAttribute(UiConstants.ATTR_NAME);
    }
    return BeeUtils.trim(caption);
  }

  public Dimensions getDimensions() {
    return XmlUtils.getDimensions(getFormElement());
  }

  public Set<Action> getDisabledActions() {
    String actions = getFormElement().getAttribute(ATTR_DISABLED_ACTIONS);
    if (BeeUtils.isEmpty(actions)) {
      return null;
    } else {
      return Action.parse(actions);
    }
  }
  
  public Set<Action> getEnabledActions() {
    String actions = getFormElement().getAttribute(ATTR_ENABLED_ACTIONS);
    if (BeeUtils.isEmpty(actions)) {
      return null;
    } else {
      return Action.parse(actions);
    }
  }

  public String getName() {
    return getFormElement().getAttribute(UiConstants.ATTR_NAME);
  }
  
  public Calculation getRowEditable() {
    return XmlUtils.getCalculation(getFormElement(), TAG_ROW_EDITABLE);
  }

  public Calculation getRowMessage() {
    return XmlUtils.getCalculation(getFormElement(), TAG_ROW_MESSAGE);
  }

  public Calculation getRowValidation() {
    return XmlUtils.getCalculation(getFormElement(), TAG_ROW_VALIDATION);
  }

  public int getSearchThreshold() {
    Integer searchThreshold = XmlUtils.getAttributeInteger(getFormElement(), ATTR_SEARCH_THRESHOLD);
    if (searchThreshold == null) {
      searchThreshold = DataUtils.getDefaultSearchThreshold();
    }
    return searchThreshold;
  }

  public String getViewName() {
    if (BeeUtils.isEmpty(viewName)) {
      return getFormElement().getAttribute(UiConstants.ATTR_VIEW_NAME);
    } else {
      return viewName;
    }
  }

  public boolean isReadOnly() {
    Boolean readOnly = XmlUtils.getAttributeBoolean(getFormElement(), UiConstants.ATTR_READ_ONLY);
    if (readOnly == null) {
      return false;
    }
    return readOnly;
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  Element getFormElement() {
    return formElement;
  }
}
