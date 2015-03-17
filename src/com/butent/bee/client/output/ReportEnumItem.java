package com.butent.bee.client.output;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportEnumItem extends ReportItem implements ClickHandler {

  private static final String ENUM = "ENUM";

  private String enumKey;
  private Label filter;

  public ReportEnumItem(String name, String caption,
      Class<? extends Enum<? extends HasCaption>> en) {
    this(name, caption);
    Assert.notNull(en);
    enumKey = NameUtils.getClassName(en);
  }

  protected ReportEnumItem(String name, String caption) {
    super(name, caption);
  }

  @Override
  public void clearFilter() {
    if (filter != null) {
      filter.setTitle(null);
      filter.setText(null);
      filter.addStyleName(getStyle() + "-filter-empty");
    }
  }

  @Override
  public void deserialize(String data) {
    Map<String, String> map = Codec.deserializeMap(data);

    if (!BeeUtils.isEmpty(map)) {
      enumKey = map.get(ENUM);
    }
  }

  @Override
  public String evaluate(SimpleRow row) {
    return EnumUtils.getCaption(enumKey, row.getInt(getName()));
  }

  @Override
  public String getFilter() {
    Set<Integer> values = getFilterValues();

    if (BeeUtils.isEmpty(values)) {
      return null;
    }
    return Codec.beeSerialize(values);
  }

  @Override
  public Label getFilterWidget() {
    if (filter == null) {
      filter = new Label();
      filter.addStyleName(getStyle() + "-filter");
      filter.addStyleName(getStyle() + "-filter-empty");
      filter.addClickHandler(this);
    }
    return filter;
  }

  @Override
  public String getStyle() {
    return STYLE_ENUM;
  }

  @Override
  public void onClick(ClickEvent event) {
    final ListBox list = new ListBox(true);
    Set<Integer> old = getFilterValues();
    int x = 0;

    for (String caption : EnumUtils.getCaptions(enumKey)) {
      list.addItem(caption);
      list.getOptionElement(x).setSelected(old.contains(x));
      x++;
    }
    list.setAllVisible();

    Global.inputWidget(Localized.getConstants().values(), list, new InputCallback() {
      @Override
      public void onSuccess() {
        Set<Integer> idxs = new HashSet<>();

        for (int i = 0; i < list.getItemCount(); i++) {
          if (list.getOptionElement(i).isSelected()) {
            idxs.add(i);
          }
        }
        setFilter(Codec.beeSerialize(idxs));
      }
    });
  }

  @Override
  public String serialize() {
    return super.serialize(Codec.beeSerialize(Collections.singletonMap(ENUM, enumKey)));
  }

  @Override
  public ReportItem setFilter(String data) {
    Set<Integer> idxs = new HashSet<>();
    List<String> caps = new ArrayList<>();
    String[] arr = Codec.beeDeserializeCollection(data);

    if (!ArrayUtils.isEmpty(arr)) {
      for (String idx : arr) {
        int i = BeeUtils.toInt(idx);
        idxs.add(i);
        caps.add(EnumUtils.getCaption(enumKey, i));
      }
    }
    Label widget = getFilterWidget();
    widget.setTitle(BeeUtils.joinInts(idxs));
    widget.setText(BeeUtils.joinItems(caps));
    widget.setStyleName(getStyle() + "-filter-empty", BeeUtils.isEmpty(widget.getTitle()));
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    Set<Integer> values = getFilterValues();

    if (BeeUtils.isEmpty(values) || !row.getRowSet().hasColumn(getName())) {
      return true;
    }
    return values.contains(row.getInt(getName()));
  }

  private Set<Integer> getFilterValues() {
    Set<Integer> values = new HashSet<>();

    if (filter != null && !BeeUtils.isEmpty(filter.getTitle())) {
      for (String value : BeeUtils.split(filter.getTitle(), ',')) {
        values.add(BeeUtils.toInt(value));
      }
    }
    return values;
  }
}
