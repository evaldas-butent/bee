package com.butent.bee.client.view.form;

import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.WidgetDescription;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeFrame;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.DateLabel;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.client.widget.DecimalLabel;
import com.butent.bee.client.widget.DoubleLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InlineInternalLink;
import com.butent.bee.client.widget.IntegerLabel;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Link;
import com.butent.bee.client.widget.LongLabel;
import com.butent.bee.client.widget.Meter;
import com.butent.bee.client.widget.Progress;
import com.butent.bee.client.widget.TextLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class DisplayWidget {

  private final int dataIndex;
  private final AbstractCellRenderer renderer;
  private final WidgetDescription widgetDescription;

  public DisplayWidget(int dataIndex, AbstractCellRenderer renderer,
      WidgetDescription widgetDescription) {
    this.dataIndex = dataIndex;
    this.renderer = renderer;
    this.widgetDescription = widgetDescription;
  }

  public String getValue(IsRow row) {
    if (renderer != null) {
      return renderer.render(row);
    } else if (row != null) {
      return row.getString(dataIndex);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public String getWidgetId() {
    return widgetDescription.getWidgetId();
  }

  public FormWidget getWidgetType() {
    return widgetDescription.getWidgetType();
  }

  public void refresh(Widget widget, IsRow row) {
    String value = getValue(row);
    FormWidget type = getWidgetType();

    switch (type) {
      case AUDIO:
        if (!BeeUtils.isEmpty(value) && widget instanceof Audio) {
          ((Audio) widget).setSrc(value);
        }
        break;

      case BUTTON:
        if (!BeeUtils.isEmpty(value) && widget instanceof BeeButton) {
          ((BeeButton) widget).setHTML(value);
        }
        break;

      case CURRENCY_LABEL:
        if (widget instanceof DecimalLabel) {
          ((DecimalLabel) widget).setValue(BeeUtils.toDecimalOrNull(value));
        }
        break;

      case DATA_SELECTOR:
        if (widget instanceof DataSelector) {
          ((DataSelector) widget).setDisplayValue(BeeUtils.trim(value));
        }
        break;

      case DATE_LABEL:
        if (widget instanceof DateLabel) {
          ((DateLabel) widget).setValue(TimeUtils.toDateOrNull(value));
        }
        break;

      case DATE_TIME_LABEL:
        if (widget instanceof DateTimeLabel) {
          ((DateTimeLabel) widget).setValue(TimeUtils.toDateTimeOrNull(value));
        }
        break;

      case DECIMAL_LABEL:
        if (widget instanceof DecimalLabel) {
          ((DecimalLabel) widget).setValue(BeeUtils.toDecimalOrNull(value));
        }
        break;

      case DOUBLE_LABEL:
        if (widget instanceof DoubleLabel) {
          ((DoubleLabel) widget).setValue(BeeUtils.toDoubleOrNull(value));
        }
        break;

      case FRAME:
        if (!BeeUtils.isEmpty(value) && widget instanceof BeeFrame) {
          ((BeeFrame) widget).setUrl(value);
        }
        break;

      case HYPERLINK:
        if (!BeeUtils.isEmpty(value) && widget instanceof InternalLink) {
          ((InternalLink) widget).update(value);
        }
        break;

      case HTML_LABEL:
        if (widget instanceof Html) {
          ((Html) widget).setHTML(BeeUtils.trim(value));
        }
        break;

      case IMAGE:
        if (!BeeUtils.isEmpty(value) && widget instanceof BeeImage) {
          ImageResource resource = Images.get(value);
          if (resource == null) {
            ((BeeImage) widget).setUrl(value);
          } else {
            ((BeeImage) widget).setResource(resource);
          }
          widget.getElement().setId(getWidgetId());
        }
        break;

      case INLINE_HYPERLINK:
        if (!BeeUtils.isEmpty(value) && widget instanceof InlineInternalLink) {
          ((InlineInternalLink) widget).update(value);
        }
        break;

      case INTEGER_LABEL:
        if (widget instanceof IntegerLabel) {
          ((IntegerLabel) widget).setValue(BeeUtils.toIntOrNull(value));
        }
        break;

      case LINK:
        if (!BeeUtils.isEmpty(value) && widget instanceof Link) {
          ((Link) widget).update(value);
        }
        break;

      case LONG_LABEL:
        if (widget instanceof LongLabel) {
          ((LongLabel) widget).setValue(BeeUtils.toLongOrNull(value));
        }
        break;

      case METER:
        if (widget instanceof Meter) {
          if (BeeUtils.isDouble(value)) {
            ((Meter) widget).setValue(BeeUtils.toDouble(value));
          } else {
            ((Meter) widget).setValue(((Meter) widget).getMin());
          }
        }
        break;

      case PROGRESS:
        if (widget instanceof Progress) {
          if (BeeUtils.isPositiveDouble(value)) {
            ((Progress) widget).setValue(BeeUtils.toDouble(value));
          } else {
            ((Progress) widget).setValue(BeeConst.DOUBLE_ZERO);
          }
        }
        break;

      case TAB_BAR:
        if (widget instanceof TabBar) {
          int idx = ((TabBar) widget).getIndex(value);
          if (BeeConst.isUndef(idx) && BeeUtils.isDigit(value)) {
            idx = BeeUtils.toInt(value);
          }

          if (idx >= 0 && idx < ((TabBar) widget).getItemCount()) {
            ((TabBar) widget).selectTab(idx, false);
          }
        }
        break;

      case TEXT_LABEL:
        if (widget instanceof TextLabel) {
          ((TextLabel) widget).setValue(value);
        }
        break;

      case VIDEO:
        if (!BeeUtils.isEmpty(value) && widget instanceof Video) {
          ((Video) widget).setSrc(value);
        }
        break;

      case CANVAS:
      case SVG:
      default:
        BeeKeeper.getLog().warning("refresh display:", type.getTagName(), "not supported");
    }
  }
}
