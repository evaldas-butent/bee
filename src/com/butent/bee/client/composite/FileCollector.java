package com.butent.bee.client.composite;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.utils.FileInfo;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputFile;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.LongLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Set;

public class FileCollector extends HtmlTable implements DragOverHandler, DropHandler,
    DragEnterHandler, DragLeaveHandler, HasOptions {

  private static final String STYLE_PREFIX = "bee-FileCollector-";
  private static final String STYLE_CELL = "Cell";
  private static final String STYLE_COLUMN = "Column";
  private static final String STYLE_EDIT = "edit";
  private static final String STYLE_DELETE = "delete";
  private static final String STYLE_EDITING = "editing";

  private static final int DATE_COLUMN = 0;
  private static final int VERSION_COLUMN = 1;
  private static final int NAME_COLUMN = 2;
  private static final int DESCRIPTION_COLUMN = 3;
  private static final int SIZE_COLUMN = 4;
  private static final int TYPE_COLUMN = 5;

  private static final String DATE_LABEL = "date";
  private static final String VERSION_LABEL = "version";
  private static final String NAME_LABEL = "name";
  private static final String DESCRIPTION_LABEL = "description";
  private static final String SIZE_LABEL = "size";
  private static final String TYPE_LABEL = "type";

  private static final int EDIT_COLUMN = 6;
  private static final int DELETE_COLUMN = 7;

  private final InputFile inputFile;

  private final List<FileInfo> files = Lists.newArrayList();

  private Element dropArea = null;
  private int dndCounter = 0;

  private String options = null;
  private final Set<Integer> editable = Sets.newHashSet();

  public FileCollector() {
    this.inputFile = new InputFile(true);
    inputFile.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        addFiles(FileUtils.getFileInfo(FileCollector.this.inputFile.getFiles()));
      }
    });

    inputFile.getElement().getStyle().setVisibility(Visibility.HIDDEN);
    inputFile.addStyleName(STYLE_PREFIX + "hiddenWidget");

    BeeButton button = new BeeButton("Pasirinkite bylas");
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        FileCollector.this.inputFile.click();
      }
    });

    button.addStyleName(STYLE_PREFIX + "chooseWidget");

    setWidget(0, 0, button, STYLE_PREFIX + "chooseCell");
    setWidget(0, 1, inputFile, STYLE_PREFIX + "hiddenCell");

    this.addStyleName(STYLE_PREFIX + "panel");

    getRowFormatter().addStyleName(0, STYLE_PREFIX + "chooseRow");
  }

  public void addFiles(List<FileInfo> fileInfos) {
    if (fileInfos != null) {
      for (FileInfo info : fileInfos) {
        addFile(info);
      }
    }
  }

  public <T extends HasAllDragAndDropHandlers & IsWidget> void bindDnd(T view) {
    view.addDragEnterHandler(this);
    view.addDragLeaveHandler(this);
    view.addDragOverHandler(this);
    view.addDropHandler(this);

    setDropArea(view.asWidget().getElement());
  }

  @Override
  public void clear() {
    getFiles().clear();
    setDndCounter(0);

    int rowCount = getRowCount() - 1;
    for (int r = 0; r < rowCount; r++) {
      removeRow(0);
    }
  }

  public List<FileInfo> getFiles() {
    return files;
  }

  @Override
  public String getIdPrefix() {
    return "files";
  }

  @Override
  public String getOptions() {
    return options;
  }

  @Override
  public void onDragEnter(DragEnterEvent event) {
    setDndCounter(getDndCounter() + 1);
    if (getDndCounter() <= 1) {
      showDropArea();
    }
  }

  @Override
  public void onDragLeave(DragLeaveEvent event) {
    setDndCounter(getDndCounter() - 1);
    if (getDndCounter() <= 0) {
      setDndCounter(0);
      hideDropArea();
    }
  }

  @Override
  public void onDragOver(DragOverEvent event) {
    EventUtils.setDropEffect(event, EventUtils.EFFECT_COPY);
  }

  @Override
  public void onDrop(DropEvent event) {
    event.stopPropagation();

    setDndCounter(0);
    hideDropArea();

    addFiles(FileUtils.getFileInfo(FileUtils.getFiles(event.getNativeEvent())));
  }

  @Override
  public void setOptions(String options) {
    this.options = options;

    editable.clear();

    if (!BeeUtils.isEmpty(options)) {
      Set<String> colNames = NameUtils.toSet(options.toLowerCase());

      if (colNames.contains(DATE_LABEL)) {
        editable.add(DATE_COLUMN);
      }
      if (colNames.contains(VERSION_LABEL)) {
        editable.add(VERSION_COLUMN);
      }
      if (colNames.contains(NAME_LABEL)) {
        editable.add(NAME_COLUMN);
      }
      if (colNames.contains(DESCRIPTION_LABEL)) {
        editable.add(DESCRIPTION_COLUMN);
      }
      if (colNames.contains(TYPE_LABEL)) {
        editable.add(TYPE_COLUMN);
      }
    }
  }

  private void addFile(final FileInfo info) {
    if (info == null || contains(info)) {
      return;
    }

    getFiles().add(info);

    int row = getRowCount() - 1;
    insertRow(row);
    getRowFormatter().addStyleName(row, STYLE_PREFIX + "row");

    DateTimeLabel lastMod = new DateTimeLabel(false);
    lastMod.addStyleName(STYLE_PREFIX + DATE_LABEL);
    lastMod.setValue(BeeUtils.nvl(info.getFileDate(), info.getLastModified()));
    setWidget(row, DATE_COLUMN, lastMod, STYLE_PREFIX + DATE_LABEL + STYLE_CELL);

    BeeLabel versionLabel = new BeeLabel(info.getFileVersion());
    versionLabel.addStyleName(STYLE_PREFIX + VERSION_LABEL);
    setWidget(row, VERSION_COLUMN, versionLabel, STYLE_PREFIX + VERSION_LABEL + STYLE_CELL);

    BeeLabel nameLabel = new BeeLabel(BeeUtils.notEmpty(info.getCaption(), info.getName()));
    nameLabel.addStyleName(STYLE_PREFIX + NAME_LABEL);
    setWidget(row, NAME_COLUMN, nameLabel, STYLE_PREFIX + NAME_LABEL + STYLE_CELL);

    BeeLabel descrLabel = new BeeLabel(info.getDescription());
    descrLabel.addStyleName(STYLE_PREFIX + DESCRIPTION_LABEL);
    setWidget(row, DESCRIPTION_COLUMN, descrLabel, STYLE_PREFIX + DESCRIPTION_LABEL + STYLE_CELL);

    LongLabel sizeLabel = new LongLabel(false);
    sizeLabel.addStyleName(STYLE_PREFIX + SIZE_LABEL);
    sizeLabel.setValue(info.getSize());
    setWidget(row, SIZE_COLUMN, sizeLabel, STYLE_PREFIX + SIZE_LABEL + STYLE_CELL);

    BeeLabel typeLabel = new BeeLabel(info.getType());
    typeLabel.addStyleName(STYLE_PREFIX + TYPE_LABEL);
    setWidget(row, TYPE_COLUMN, typeLabel, STYLE_PREFIX + TYPE_LABEL + STYLE_CELL);

    if (!editable.isEmpty()) {
      BeeImage edit = new BeeImage(Global.getImages().edit());
      edit.addStyleName(STYLE_PREFIX + STYLE_EDIT);
      edit.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          int index = getIndex(info.getName());
          if (index >= 0 && !editable.isEmpty()) {
            edit(index);
          }
        }
      });
      setWidget(row, EDIT_COLUMN, edit, STYLE_PREFIX + STYLE_EDIT + STYLE_CELL);
    }

    BeeImage delete = new BeeImage(Global.getImages().delete());
    delete.addStyleName(STYLE_PREFIX + STYLE_DELETE);
    delete.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int index = getIndex(info.getName());
        if (index >= 0) {
          getFiles().remove(index);
          removeRow(index);
        }
      }
    });
    setWidget(row, DELETE_COLUMN, delete, STYLE_PREFIX + STYLE_DELETE + STYLE_CELL);

    if (row == 0) {
      if (!editable.isEmpty()) {
        getColumnFormatter().addStyleName(EDIT_COLUMN, STYLE_PREFIX + STYLE_EDIT + STYLE_COLUMN);
      }
      getColumnFormatter().addStyleName(DELETE_COLUMN, STYLE_PREFIX + STYLE_DELETE + STYLE_COLUMN);
    }
  }

  private boolean contains(FileInfo info) {
    return getIndex(info.getName()) >= 0;
  }

  private void edit(final int index) {
    getRowFormatter().addStyleName(index, STYLE_PREFIX + STYLE_EDITING);

    final FileInfo fi = getFiles().get(index);
    
    String pfx = STYLE_PREFIX + STYLE_EDIT + "-";
    
    HtmlTable panel = new HtmlTable();
    panel.addStyleName(pfx + "panel");
    
    int colLabel = 0;
    int colInput = 1;
    
    int row = 0;
    
    final InputDate inputDate;
    final InputText inputVersion;
    final InputText inputName;
    final InputArea inputDescr;
    final InputText inputType;
    
    if (editable.contains(DATE_COLUMN)) {
      panel.setWidget(row, colLabel, new BeeLabel("Data"));

      inputDate = new InputDate(ValueType.DATETIME);
      inputDate.addStyleName(pfx + DATE_LABEL);
      inputDate.setValue(BeeUtils.nvl(fi.getFileDate(), fi.getLastModified()));
      
      panel.setWidget(row, colInput, inputDate, pfx + DATE_LABEL + STYLE_CELL);
      row++;
    } else {
      inputDate = null;
    }
    
    if (editable.contains(VERSION_COLUMN)) {
      panel.setWidget(row, colLabel, new BeeLabel("Versija"));

      inputVersion = new InputText();
      inputVersion.addStyleName(pfx + VERSION_LABEL);
      inputVersion.setValue(fi.getFileVersion());

      panel.setWidget(row, colInput, inputVersion, pfx + VERSION_LABEL + STYLE_CELL);
      row++;
    } else {
      inputVersion = null;
    }

    if (editable.contains(NAME_COLUMN)) {
      panel.setWidget(row, colLabel, new BeeLabel("Pavadinimas"));

      inputName = new InputText();
      inputName.addStyleName(pfx + NAME_LABEL);
      inputName.setValue(BeeUtils.notEmpty(fi.getCaption(), fi.getName()));
      
      panel.setWidget(row, colInput, inputName, pfx + NAME_LABEL + STYLE_CELL);
      row++;
    } else {
      inputName = null;
    }

    if (editable.contains(DESCRIPTION_COLUMN)) {
      panel.setWidget(row, colLabel, new BeeLabel("Aprašymas"));

      inputDescr = new InputArea();
      inputDescr.addStyleName(pfx + DESCRIPTION_LABEL);
      inputDescr.setValue(fi.getDescription());

      panel.setWidget(row, colInput, inputDescr, pfx + DESCRIPTION_LABEL + STYLE_CELL);
      row++;
    } else {
      inputDescr = null;
    }
    
    if (editable.contains(TYPE_COLUMN)) {
      panel.setWidget(row, colLabel, new BeeLabel("Tipas"));
      
      inputType = new InputText();
      inputType.addStyleName(pfx + TYPE_LABEL);
      inputType.setValue(fi.getType());

      panel.setWidget(row, colInput, inputType, pfx + TYPE_LABEL + STYLE_CELL);
      row++;
    } else {
      inputType = null;
    }
    
    Global.inputWidget("Bylos duomenų koregavimas", panel, new InputCallback() {
      @Override
      public void onCancel() {
        getRowFormatter().removeStyleName(index, STYLE_PREFIX + STYLE_EDITING);
      }

      @Override
      public void onSuccess() {
        getRowFormatter().removeStyleName(index, STYLE_PREFIX + STYLE_EDITING);
        
        Set<Integer> changed = Sets.newHashSet();
        if (inputDate != null) {
          HasDateValue date = inputDate.getDate();
          boolean upd = false; 
          
          if (date == null || TimeUtils.equals(date, fi.getLastModified())) {
            upd = (fi.getFileDate() != null);
            fi.setFileDate(null);
          } else if (!TimeUtils.equals(date, fi.getFileDate())) {
            upd = true;
            fi.setFileDate(date.getDateTime());
          }
          
          if (upd) {
            changed.add(DATE_COLUMN);
          }
        }
        
        if (inputVersion != null) {
          if (!BeeUtils.equalsTrim(fi.getFileVersion(), inputVersion.getValue())) {
            fi.setFileVersion(Strings.emptyToNull(BeeUtils.trim(inputVersion.getValue())));
            changed.add(VERSION_COLUMN);
          }
        }

        if (inputName != null) {
          String name = BeeUtils.trim(inputName.getValue());
          boolean upd = false; 
          
          if (BeeUtils.isEmpty(name) || BeeUtils.equalsTrim(name, fi.getName())) {
            upd = !BeeUtils.isEmpty(fi.getCaption());
            fi.setCaption(null);
          } else if (!BeeUtils.equalsTrim(name, fi.getCaption())) {
            upd = true;
            fi.setCaption(name);
          }
          
          if (upd) {
            changed.add(NAME_COLUMN);
          }
        }
        
        if (inputDescr != null) {
          if (!BeeUtils.equalsTrim(fi.getDescription(), inputDescr.getValue())) {
            fi.setDescription(Strings.emptyToNull(BeeUtils.trim(inputDescr.getValue())));
            changed.add(DESCRIPTION_COLUMN);
          }
        }
        
        if (inputType != null) {
          if (!BeeUtils.equalsTrim(fi.getType(), inputType.getValue())) {
            fi.setType(Strings.emptyToNull(BeeUtils.trim(inputType.getValue())));
            changed.add(TYPE_COLUMN);
          }
        }
        
        if (!changed.isEmpty()) {
          refresh(index, changed);
        }
      }
    }, false, null, getWidget(index, EDIT_COLUMN), false);
  }

  private int getDndCounter() {
    return dndCounter;
  }

  private Element getDropArea() {
    return dropArea;
  }

  private int getIndex(String fileName) {
    for (int i = 0; i < getFiles().size(); i++) {
      if (BeeUtils.same(getFiles().get(i).getName(), fileName)) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private void hideDropArea() {
    if (StyleUtils.hasClassName(getDropArea(), StyleUtils.DROP_AREA)) {
      getDropArea().removeClassName(StyleUtils.DROP_AREA);
    }
  }

  private void refresh(int row, Set<Integer> columns) {
    FileInfo info = getFiles().get(row);
    Widget widget;

    if (columns.contains(DATE_COLUMN)) {
      widget = getWidget(row, DATE_COLUMN);
      if (widget instanceof DateTimeLabel) {
        ((DateTimeLabel) widget).setValue(BeeUtils.nvl(info.getFileDate(), info.getLastModified()));
      }
    }

    if (columns.contains(VERSION_COLUMN)) {
      widget = getWidget(row, VERSION_COLUMN);
      if (widget instanceof BeeLabel) {
        ((BeeLabel) widget).setText(BeeUtils.trim(info.getFileVersion()));
      }
    }

    if (columns.contains(NAME_COLUMN)) {
      widget = getWidget(row, NAME_COLUMN);
      if (widget instanceof BeeLabel) {
        ((BeeLabel) widget).setText(BeeUtils.notEmpty(info.getCaption(), info.getName()));
      }
    }

    if (columns.contains(DESCRIPTION_COLUMN)) {
      widget = getWidget(row, DESCRIPTION_COLUMN);
      if (widget instanceof BeeLabel) {
        ((BeeLabel) widget).setText(BeeUtils.trim(info.getDescription()));
      }
    }
    
    if (columns.contains(TYPE_COLUMN)) {
      widget = getWidget(row, TYPE_COLUMN);
      if (widget instanceof BeeLabel) {
        ((BeeLabel) widget).setText(BeeUtils.trim(info.getType()));
      }
    }
  }
  
  private void setDndCounter(int dndCounter) {
    this.dndCounter = dndCounter;
  }

  private void setDropArea(Element dropArea) {
    this.dropArea = dropArea;
  }

  private void showDropArea() {
    if (!StyleUtils.hasClassName(getDropArea(), StyleUtils.DROP_AREA)) {
      getDropArea().addClassName(StyleUtils.DROP_AREA);
    }
  }
}