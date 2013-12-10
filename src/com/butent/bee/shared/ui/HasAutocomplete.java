package com.butent.bee.shared.ui;

import com.butent.bee.shared.html.Autocomplete;

public interface HasAutocomplete {
  
  String ATTR_AUTOCOMPLETE = "autocomplete";

  String ATTR_AUTOCOMPLETE_SECTION = "autocompleteSection";
  String ATTR_AUTOCOMPLETE_HINT = "autocompleteHint";
  String ATTR_AUTOCOMPLETE_CONTACT = "autocompleteContact";
  String ATTR_AUTOCOMPLETE_FIELD = "autocompleteField";

  String getAutocomplete();

  void setAutocomplete(Autocomplete autocomplete);
  
  void setAutocomplete(String ac);
}
