package com.butent.bee.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.html.Autocomplete;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

public final class AutocompleteProvider {
  
  private static final class AcWidget extends Widget {
    private AcWidget(Element element) {
      super();
      setElement(element);
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(AutocompleteProvider.class);

  private static final String NAME_PREFIX = "ac-";
  private static final String NAME_SEPARATOR = "-";
  
  public static void enableDefault(HasAutocomplete obj, String name) {
    Assert.notNull(obj);
    Assert.notEmpty(name);
    
    obj.setAutocomplete(Autocomplete.ON);
    obj.setName(name);
  }
  
  public static void maybeSetAutocomplete(HasAutocomplete obj, Map<String, String> attributes,
      String formName, String widgetName, String viewName, String columnId) {
    
    if (obj == null || BeeUtils.isEmpty(attributes)) {
      return;
    }
    
    String ac = attributes.get(HasAutocomplete.ATTR_AUTOCOMPLETE);
    if (BeeConst.isFalse(ac)) {
      return;
    }

    String name = attributes.get(HasAutocomplete.ATTR_AUTOCOMPLETE_NAME);
    String field = attributes.get(HasAutocomplete.ATTR_AUTOCOMPLETE_FIELD);

    if (BeeUtils.allEmpty(name, field)) {
      if (BeeUtils.isEmpty(ac)) {
        return;
      }
      
      name = generateName(viewName, columnId);
      if (BeeUtils.isEmpty(name)) {
        name = generateName(formName, widgetName);

        if (BeeUtils.isEmpty(name)) {
          logger.warning("autocomplete cannot generate name:", formName, viewName, attributes);
          return;
        }
      }
    }

    if (BeeUtils.isEmpty(field)) {
      obj.setAutocomplete(Autocomplete.ON);
      obj.setName(name);
      return;
    }

    Autocomplete autocomplete = new Autocomplete(); 
    
    String section = attributes.get(HasAutocomplete.ATTR_AUTOCOMPLETE_SECTION);
    if (!BeeUtils.isEmpty(section)) {
      autocomplete.setSection(section);
    }

    String hint = attributes.get(HasAutocomplete.ATTR_AUTOCOMPLETE_HINT);
    if (!BeeUtils.isEmpty(hint)) {
      autocomplete.setHint(hint);
    }

    String contact = attributes.get(HasAutocomplete.ATTR_AUTOCOMPLETE_CONTACT);
    if (!BeeUtils.isEmpty(contact)) {
      autocomplete.setContact(contact);
    }

    autocomplete.setField(field);
    obj.setAutocomplete(autocomplete);
    
    if (BeeUtils.isEmpty(name)) {
      obj.setName(NAME_PREFIX + BeeUtils.join(NAME_SEPARATOR, section, hint, contact, field));
    } else {
      obj.setName(name);
    }
  }
  
  public static void saveValues(Collection<HasAutocomplete> fields) {
    Flow panel = new Flow();
    
    for (HasAutocomplete field : fields) {
      Element element = field.cloneAutocomplete();
      AcWidget widget = new AcWidget(element);
      
      panel.add(widget);
    }
    
    FormPanel form = new FormPanel();
    form.setWidget(panel);
    
    form.setAction("about:blank");
    form.setMethod(FormPanel.METHOD_POST);
    
    form.submit();
  }
  
  private static String generateName(String first, String second) {
    if (BeeUtils.anyEmpty(first, second)) {
      return null;
    } else {
      return NAME_PREFIX + first.trim() + NAME_SEPARATOR + second.trim();
    }
  }
  
  private AutocompleteProvider() {
  }
}
