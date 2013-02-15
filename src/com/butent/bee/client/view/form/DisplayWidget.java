package com.butent.bee.client.view.form;

import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.FileGroup;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HandlesRendering;
import com.butent.bee.client.richtext.RichTextEditor;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.WidgetDescription;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeFrame;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.DateLabel;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.client.widget.DecimalLabel;
import com.butent.bee.client.widget.DoubleLabel;
import com.butent.bee.client.widget.Flag;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.IntegerLabel;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Link;
import com.butent.bee.client.widget.LongLabel;
import com.butent.bee.client.widget.Meter;
import com.butent.bee.client.widget.Progress;
import com.butent.bee.client.widget.TextLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class DisplayWidget {

  private static final BeeLogger logger = LogUtils.getLogger(DisplayWidget.class);
  
  private final int dataIndex;
  private final AbstractCellRenderer renderer;
  private final WidgetDescription widgetDescription;
  
  private final boolean dataBound;

  public DisplayWidget(int dataIndex, AbstractCellRenderer renderer,
      WidgetDescription widgetDescription) {
    this.dataIndex = dataIndex;
    this.renderer = renderer;
    this.widgetDescription = widgetDescription;
    
    this.dataBound = dataIndex >= 0 || renderer != null 
        || !BeeUtils.isEmpty(widgetDescription.getRowProperty());
  }

  public String getValue(IsRow row) {
    if (renderer != null) {
      return renderer.render(row);

    } else if (row != null) {
      if (dataIndex >= 0) {
        return row.getString(dataIndex);
      }
      if (hasRowProperty()) {
        return row.getProperty(widgetDescription.getRowProperty());
      }
    }

    return BeeConst.STRING_EMPTY;
  }
  
  public String getWidgetId() {
    return widgetDescription.getWidgetId();
  }

  public String getWidgetName() {
    return widgetDescription.getWidgetName();
  }

  public FormWidget getWidgetType() {
    return widgetDescription.getWidgetType();
  }

  public boolean hasRowProperty() {
    return !BeeUtils.isEmpty(widgetDescription.getRowProperty());
  }
  
  public boolean hasSource(String source) {
    if (BeeUtils.isEmpty(source)) {
      return false;
    } else {
      return BeeUtils.inListSame(source, widgetDescription.getSource(), 
          widgetDescription.getRowProperty());
    }
  }

  public void refresh(Widget widget, IsRow row) {
    if (widget instanceof HandlesRendering) {
      ((HandlesRendering) widget).render(row);
      return;
    }
    
    if (!dataBound) {
      return;
    }
    
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
          ((DataSelector) widget).onRefresh(row);
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
        
      case FLAG:
        if (widget instanceof Flag) {
          ((Flag) widget).render(value);
        }
        break;
        
      case FILE_GROUP:
        if (widget instanceof FileGroup && hasRowProperty()) {
          ((FileGroup) widget).render(value);
        }
        break;

      case FRAME:
        if (!BeeUtils.isEmpty(value) && widget instanceof BeeFrame) {
          ((BeeFrame) widget).setUrl(value);
        }
        break;

      case HYPERLINK:
        if (!BeeUtils.isEmpty(value) && widget instanceof InternalLink) {
          ((InternalLink) widget).setHTML(value);
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
      
      case RICH_TEXT_EDITOR:
        if (widget instanceof RichTextEditor) {
          ((RichTextEditor) widget).setValue(value);
        }
        break;

      case TAB_BAR:
        if (widget instanceof TabBar) {
          if (BeeUtils.isDigit(value)) {
            int idx = BeeUtils.toInt(value);
            if (idx >= 0 && idx < ((TabBar) widget).getItemCount()) {
              ((TabBar) widget).selectTab(idx, false);
            }
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
        logger.warning("refresh display:", type.getTagName(), "not supported");
    }
  }
}
