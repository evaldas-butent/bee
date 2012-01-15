package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.Element;

import com.butent.bee.client.data.LocalProvider;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.FileUtils.FileInfo;
import com.butent.bee.client.view.GridContainerView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.widget.InputFile;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.List;
import java.util.Map;

public class FileCollector extends AbstractGridCallback implements DragOverHandler, DropHandler,
    DragEnterHandler, DragLeaveHandler, ChangeHandler {

  private static final List<BeeColumn> columns = Lists.newArrayList(
      new BeeColumn(ValueType.DATETIME, CrmConstants.COL_FILE_DATE, false),
      new BeeColumn(ValueType.TEXT, CrmConstants.COL_FILE_VERSION, true),
      new BeeColumn(ValueType.TEXT, CrmConstants.COL_NAME, false),
      new BeeColumn(ValueType.TEXT, CrmConstants.COL_DESCRIPTION, true),
      new BeeColumn(ValueType.LONG, CrmConstants.COL_SIZE, true),
      new BeeColumn(ValueType.TEXT, CrmConstants.COL_MIME, true));

  private static final int dateIndex = 0;
  private static final int versionIndex = 1;
  private static final int nameIndex = 2;
  private static final int descriptionIndex = 3;
  private static final int sizeIndex = 4;
  private static final int mimeIndex = 5;

  private InputElement inputElement = null;

  private final Map<Long, FileInfo> files = Maps.newHashMap();
  private long maxId = 0;

  private Element dropArea = null;
  private int dndCounter = 0;

  public FileCollector() {
  }

  public void addFiles(List<FileInfo> fileInfos) {
    if (fileInfos != null) {
      for (FileInfo info : fileInfos) {
        addFile(info);
      }
    }
  }

  @Override
  public void afterDeleteRow(long rowId) {
    getFiles().remove(rowId);
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter) {
    if (getInputElement() != null) {
      getInputElement().click();
    }
    return false;
  }

  public void clear() {
    getFiles().clear();
    LocalProvider provider = getProvider();
    if (provider != null) {
      provider.clear();
    }

    setMaxId(0);
    setDndCounter(0);
  }

  public Map<Long, FileInfo> getFiles() {
    return files;
  }

  @Override
  public BeeRowSet getInitialRowSet() {
    return new BeeRowSet(columns);
  }

  public void onChange(ChangeEvent event) {
    if (event.getSource() instanceof InputFile) {
      addFiles(FileUtils.getFileInfo(((InputFile) event.getSource())));
    }
  }
  
  public void onDragEnter(DragEnterEvent event) {
    setDndCounter(getDndCounter() + 1);
    if (getDndCounter() <= 1) {
      showDropArea();
    }
  }

  public void onDragLeave(DragLeaveEvent event) {
    setDndCounter(getDndCounter() - 1);
    if (getDndCounter() <= 0) {
      setDndCounter(0);
      hideDropArea();
    }
  }

  public void onDragOver(DragOverEvent event) {
    EventUtils.eatEvent(event);
    EventUtils.setDropEffect(event, EventUtils.EFFECT_COPY);
  }

  public void onDrop(DropEvent event) {
    EventUtils.eatEvent(event);
    
    setDndCounter(0);
    hideDropArea();

    addFiles(FileUtils.getFileInfo(event.getDataTransfer()));
  }

  @Override
  public void onShow(GridPresenter presenter) {
    GridContainerView view = presenter.getView();
    view.addDragEnterHandler(this);
    view.addDragLeaveHandler(this);
    view.addDragOverHandler(this);
    view.addDropHandler(this);

    setDropArea(view.asWidget().getElement());
  }
  
  public void setInputWidget(InputFile widget) {
    Assert.notNull(widget);
    widget.addChangeHandler(this);
    setInputElement(InputElement.as(widget.getElement()));
  }

  private void addFile(FileInfo info) {
    if (info == null || contains(info)) {
      return;
    }

    setMaxId(getMaxId() + 1);
    long id = getMaxId();
    getFiles().put(id, info);

    String[] values = new String[columns.size()];
    values[dateIndex] = TimeUtils.normalize(info.getLastModifiedDate());
    values[versionIndex] = null;
    values[nameIndex] = info.getName();
    values[descriptionIndex] = null;
    values[sizeIndex] = BeeUtils.toString(info.getSize());
    values[mimeIndex] = info.getType();

    BeeRow row = new BeeRow(id, values);

    LocalProvider provider = getProvider();
    if (provider != null) {
      provider.addRow(row);
    }

    CellGrid grid = getGrid();
    if (grid != null) {
      grid.insertRow(row);
    }
  }

  private boolean contains(FileInfo info) {
    for (FileInfo fi : getFiles().values()) {
      if (BeeUtils.same(fi.getName(), info.getName())) {
        return true;
      }
    }
    return false;
  }

  private int getDndCounter() {
    return dndCounter;
  }

  private Element getDropArea() {
    return dropArea;
  }

  private CellGrid getGrid() {
    if (getGridPresenter() == null) {
      return null;
    } else {
      return getGridPresenter().getView().getContent().getGrid();
    }
  }

  private InputElement getInputElement() {
    return inputElement;
  }

  private long getMaxId() {
    return maxId;
  }

  private LocalProvider getProvider() {
    if (getGridPresenter() != null 
        && getGridPresenter().getDataProvider() instanceof LocalProvider) {
      return (LocalProvider) getGridPresenter().getDataProvider();
    } else {
      return null;
    }
  }

  private void hideDropArea() {
    if (StyleUtils.containsClassName(getDropArea(), StyleUtils.DROP_AREA)) {
      getDropArea().removeClassName(StyleUtils.DROP_AREA);
    }
  }

  private void setDndCounter(int dndCounter) {
    this.dndCounter = dndCounter;
  }

  private void setDropArea(Element dropArea) {
    this.dropArea = dropArea;
  }

  private void setInputElement(InputElement inputElement) {
    this.inputElement = inputElement;
  }

  private void setMaxId(long maxId) {
    this.maxId = maxId;
  }

  private void showDropArea() {
    if (!StyleUtils.containsClassName(getDropArea(), StyleUtils.DROP_AREA)) {
      getDropArea().addClassName(StyleUtils.DROP_AREA);
    }
  }
}