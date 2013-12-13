package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.IFrameElement;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.html.Autocomplete;
import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.HasAutocomplete;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AutocompleteProvider {

  private static final BeeLogger logger = LogUtils.getLogger(AutocompleteProvider.class);

  private static final String NAME_PREFIX = "ac-";
  private static final String NAME_SEPARATOR = "-";

  private static final int MIN_VALUE_LENGTH = 3;

  public static void enableAutocomplete(HasAutocomplete obj, String name) {
    Assert.notNull(obj);
    Assert.notEmpty(name);

    obj.setAutocomplete(Autocomplete.ON);
    obj.setName(name);
  }

  public static void maybeEnableAutocomplete(HasAutocomplete obj, Map<String, String> attributes,
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

  public static boolean retainValue(HasAutocomplete field) {
    if (isSubmittable(field)) {
      Set<HasAutocomplete> fields = Sets.newHashSet(field);
      return saveValues(fields);
    } else {
      return false;
    }
  }

  public static boolean retainValues(FormView form) {
    Assert.notNull(form);

    List<HasAutocomplete> fields = Lists.newArrayList();

    for (EditableWidget editableWidget : form.getEditableWidgets()) {
      if (editableWidget.getEditor() instanceof HasAutocomplete) {
        HasAutocomplete field = (HasAutocomplete) editableWidget.getEditor();

        if (isSubmittable(field)) {
          fields.add(field);
        }
      }
    }

    return fields.isEmpty() ? false : saveValues(fields);
  }

  private static String generateName(String first, String second) {
    if (BeeUtils.anyEmpty(first, second)) {
      return null;
    } else {
      return NAME_PREFIX + first.trim() + NAME_SEPARATOR + second.trim();
    }
  }

  private static boolean isSubmittable(HasAutocomplete field) {
    return field != null && !BeeUtils.anyEmpty(field.getName(), field.getAutocomplete(),
        field.getValue()) && !BeeUtils.same(field.getAutocomplete(), Autocomplete.OFF)
        && BeeUtils.hasLength(field.getValue().trim(), MIN_VALUE_LENGTH);
  }

  private static boolean saveValues(Collection<HasAutocomplete> fields) {
    Set<String> names = Sets.newHashSet();
    List<HasAutocomplete> duplicates = Lists.newArrayList();

    List<Element> elements = Lists.newArrayList();

    for (HasAutocomplete field : fields) {
      String name = field.getName();
      if (names.contains(name)) {
        duplicates.add(field);
  
      } else {
        names.add(name);
        Element element = field.isMultiline()
            ? Document.get().createTextAreaElement() : Document.get().createTextInputElement();

        DomUtils.setName(element, name);
        DomUtils.setAutocomplete(element, field.getAutocomplete());

        DomUtils.setValue(element, field.getValue().trim());

        elements.add(element);
      }
    }

    if (elements.isEmpty()) {
      return false;
    }

    String frameName = NameUtils.createUniqueName("frame");

    final IFrameElement frame = Document.get().createIFrameElement();

    frame.setName(frameName);
    frame.setSrc(Keywords.URL_ABOUT_BLANK);

    BodyPanel.conceal(frame);

    final FormElement form = Document.get().createFormElement();

    form.setTarget(frameName);
    form.setMethod(Keywords.METHOD_POST);
    form.setAction(Keywords.URL_ABOUT_BLANK);

    for (Element element : elements) {
      form.appendChild(element);
    }

    BodyPanel.conceal(form);

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        form.submit();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
          @Override
          public void execute() {
            form.removeFromParent();
            frame.removeFromParent();
          }
        });
      }
    });
    
    if (!duplicates.isEmpty()) {
      saveValues(duplicates);
    }

    return true;
  }

  private AutocompleteProvider() {
  }
}
