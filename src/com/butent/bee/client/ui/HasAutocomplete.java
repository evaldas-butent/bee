package com.butent.bee.client.ui;

import com.google.gwt.dom.client.Element;

import com.butent.bee.shared.HasName;
import com.butent.bee.shared.html.Autocomplete;
import com.butent.bee.shared.ui.HasStringValue;

public interface HasAutocomplete extends HasName, HasStringValue {
  
  String ATTR_AUTOCOMPLETE = "autocomplete";

  String ATTR_AUTOCOMPLETE_NAME = "autocompleteName";

  String ATTR_AUTOCOMPLETE_SECTION = "autocompleteSection";
  String ATTR_AUTOCOMPLETE_HINT = "autocompleteHint";
  String ATTR_AUTOCOMPLETE_CONTACT = "autocompleteContact";
  String ATTR_AUTOCOMPLETE_FIELD = "autocompleteField";
  
  Element cloneAutocomplete();

  String getAutocomplete();

  void setAutocomplete(Autocomplete autocomplete);
  
  void setAutocomplete(String ac);
}
