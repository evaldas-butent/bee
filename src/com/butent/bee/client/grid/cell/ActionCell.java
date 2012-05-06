package com.butent.bee.client.grid.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.HasService;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

public class ActionCell extends AbstractCell<String> implements HasService, HasOptions {

  public enum Type {
    LINK, BUTTON;

    private SafeHtml render(String value) {
      switch (this) {
        case LINK:
          return TEMPLATE.link(value);
        case BUTTON:
          return TEMPLATE.button(value);
        default:
          Assert.untouchable();
          return null;
      }
    }
  }

  interface Template extends SafeHtmlTemplates {
    @Template("<button class=\"bee-ActionCellButton\">{0}</button>")
    SafeHtml button(String option);

    @Template("<a href=\"javascript:;\" class=\"bee-ActionCellLink\">{0}</a>")
    SafeHtml link(String option);
  }

  private static final Template TEMPLATE = GWT.create(Template.class);

  private static final Type DEFAULT_TYPE = Type.LINK;

  public static ActionCell create(ColumnDescription columnDescription) {
    ActionCell cell = new ActionCell(NameUtils.getConstant(Type.class,
        columnDescription.getElementType()));

    cell.setService(columnDescription.getService());
    cell.setOptions(columnDescription.getOptions());

    return cell;
  }

  private final Type type;

  private String service = null;
  private String options = null;

  public ActionCell() {
    this(DEFAULT_TYPE);
  }

  public ActionCell(Type type) {
    super();
    this.type = (type == null) ? DEFAULT_TYPE : type;
  }

  public String getOptions() {
    return options;
  }

  public String getService() {
    return service;
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (!BeeUtils.isEmpty(value)) {
      sb.append(type.render(value));
    }
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public void setService(String service) {
    this.service = service;
  }
}
