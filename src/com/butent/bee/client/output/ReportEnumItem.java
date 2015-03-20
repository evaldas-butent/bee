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
import java.util.Objects;
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
      setFilterValues(null);
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
  public ReportItem deserializeFilter(String data) {
    if (!BeeUtils.isEmpty(data)) {
      Set<Integer> idxs = new HashSet<>();
      String[] arr = Codec.beeDeserializeCollection(data);

      if (!ArrayUtils.isEmpty(arr)) {
        for (String idx : arr) {
          idxs.add(BeeUtils.toInt(idx));
        }
      }
      setFilterValues(idxs);
    }
    return this;
  }

  @Override
  public String evaluate(SimpleRow row) {
    return EnumUtils.getCaption(enumKey, row.getInt(getName()));
  }

  @Override
  public Label getFilterWidget() {
    if (filter == null) {
      filter = new Label();
      filter.addStyleName(getStyle() + "-filter");
      filter.addClickHandler(this);
      clearFilter();
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
        setFilterValues(idxs);
      }
    });
  }

  @Override
  public String serialize() {
    return super.serialize(Codec.beeSerialize(Collections.singletonMap(ENUM, enumKey)));
  }

  @Override
  public String serializeFilter() {
    if (filter == null) {
      return null;
    }
    return Codec.beeSerialize(getFilterValues());
  }

  @Override
  public ReportItem setFilter(String value) {
    if (!BeeUtils.isEmpty(value)) {
      int idx = 0;
      Set<Integer> index = null;

      for (String caption : EnumUtils.getCaptions(enumKey)) {
        if (Objects.equals(caption, value)) {
          index = Collections.singleton(idx);
          break;
        }
        idx++;
      }
      setFilterValues(index);
    }
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

    if (filter != null) {
      values.addAll(BeeUtils.toInts(filter.getTitle()));
    }
    return values;
  }

  private void setFilterValues(Set<Integer> idxs) {
    List<String> caps = new ArrayList<>();

    if (!BeeUtils.isEmpty(idxs)) {
      for (Integer idx : idxs) {
        caps.add(EnumUtils.getCaption(enumKey, idx));
      }
    }
    Label widget = getFilterWidget();
    widget.setTitle(BeeUtils.joinInts(idxs));
    widget.setText(BeeUtils.joinItems(caps));
    widget.setStyleName(getStyle() + "-filter-empty", BeeUtils.isEmpty(widget.getTitle()));
  }
}
