package com.butent.bee.client.composite;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Link;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FileGroup extends HtmlTable implements HasOptions, HasCaption {

  public enum Column implements HasCaption {
    ICON("icon", null, false, true) {
      @Override
      Widget createDisplay() {
        return new BeeImage();
      }

      @Override
      Widget createEditor(StoredFile sf) {
        return null;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
        if (widget instanceof BeeImage && !BeeUtils.isEmpty(sf.getIcon())) {
          String url = StoredFile.getIconUrl(sf.getIcon());
          ((BeeImage) widget).setUrl(url);
        }
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        return false;
      }
    },

    DATE("date", "Data", false, false) {
      @Override
      Widget createDisplay() {
        return new DateTimeLabel(false);
      }

      @Override
      Widget createEditor(StoredFile sf) {
        InputDateTime editor = new InputDateTime();
        editor.setDateTime(sf.getFileDate());
        return editor;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
        if (widget instanceof DateTimeLabel) {
          ((DateTimeLabel) widget).setValue(sf.getFileDate());
        }
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        boolean changed = false;
        if (widget instanceof InputDateTime) {
          DateTime dt = ((InputDateTime) widget).getDateTime();

          if (dt != null && !TimeUtils.equals(dt, sf.getFileDate())) {
            changed = true;
            sf.setFileDate(dt);
          }
        }
        return changed;
      }
    },

    VERSION("version", "Versija", false, false) {
      @Override
      Widget createDisplay() {
        return new BeeLabel();
      }

      @Override
      Widget createEditor(StoredFile sf) {
        InputText editor = new InputText();
        editor.setValue(sf.getFileVersion());
        return editor;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
        if (widget instanceof BeeLabel) {
          ((BeeLabel) widget).setText(BeeUtils.trim(sf.getFileVersion()));
        }
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        if (widget instanceof InputText) {
          String value = ((InputText) widget).getValue();
          if (!BeeUtils.equalsTrim(sf.getFileVersion(), value)) {
            sf.setFileVersion(Strings.emptyToNull(BeeUtils.trim(value)));
            return true;
          }
        }
        return false;
      }
    },

    NAME("name", "Pavadinimas", true, false) {
      @Override
      Widget createDisplay() {
        return new Link();
      }

      @Override
      Widget createEditor(StoredFile sf) {
        InputText editor = new InputText();
        editor.setValue(BeeUtils.notEmpty(sf.getCaption(), sf.getName()));
        return editor;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
        if (widget instanceof Link) {
          ((Link) widget).setHref(FileUtils
              .getUrl(BeeUtils.notEmpty(sf.getCaption(), sf.getName()), sf.getFileId()));
          ((Link) widget).setText(BeeUtils.notEmpty(sf.getCaption(), sf.getName()));
        }
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        boolean changed = false;
        if (widget instanceof InputText) {
          String value = BeeUtils.trim(((InputText) widget).getValue());

          if (BeeUtils.isEmpty(value) || BeeUtils.equalsTrim(value, sf.getName())) {
            changed = !BeeUtils.isEmpty(sf.getCaption());
            sf.setCaption(null);
          } else if (!BeeUtils.equalsTrim(value, sf.getCaption())) {
            changed = true;
            sf.setCaption(value);
          }
        }
        return changed;
      }
    },

    DESCRIPTION("description", "Aprašymas", false, false) {
      @Override
      Widget createDisplay() {
        return new BeeLabel();
      }

      @Override
      Widget createEditor(StoredFile sf) {
        InputArea editor = new InputArea();
        editor.setValue(BeeUtils.trim(sf.getDescription()));
        return editor;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
        if (widget instanceof BeeLabel) {
          ((BeeLabel) widget).setText(BeeUtils.trim(sf.getDescription()));
        }
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        if (widget instanceof InputArea) {
          String value = ((InputArea) widget).getValue();
          if (!BeeUtils.equalsTrim(sf.getDescription(), value)) {
            sf.setDescription(Strings.emptyToNull(BeeUtils.trim(value)));
            return true;
          }
        }
        return false;
      }
    },

    SIZE("size", "Dydis", false, true) {
      @Override
      Widget createDisplay() {
        return new BeeLabel();
      }

      @Override
      Widget createEditor(StoredFile sf) {
        return null;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
        if (widget instanceof BeeLabel) {
          ((BeeLabel) widget).setText(FileUtils.sizeToText(sf.getSize()));
        }
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        return false;
      }
    },

    TYPE("type", "Tipas", false, false) {
      @Override
      Widget createDisplay() {
        return new BeeLabel();
      }

      @Override
      Widget createEditor(StoredFile sf) {
        InputText editor = new InputText();
        editor.setValue(sf.getType());
        return editor;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
        if (widget instanceof BeeLabel) {
          ((BeeLabel) widget).setText(BeeUtils.trim(sf.getType()));
        }
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        if (widget instanceof InputText) {
          String value = ((InputText) widget).getValue();
          if (!BeeUtils.equalsTrim(sf.getType(), value)) {
            sf.setType(Strings.emptyToNull(BeeUtils.trim(value)));
            return true;
          }
        }
        return false;
      }
    },

    EDIT("edit", "Koreguoti", false, true) {
      @Override
      Widget createDisplay() {
        return new BeeImage(Global.getImages().silverEdit());
      }

      @Override
      Widget createEditor(StoredFile sf) {
        return null;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        return false;
      }
    },

    DELETE("delete", "Išmesti", false, true) {
      @Override
      Widget createDisplay() {
        return new BeeImage(Global.getImages().silverMinus());
      }

      @Override
      Widget createEditor(StoredFile sf) {
        return null;
      }

      @Override
      void refresh(Widget widget, StoredFile sf) {
      }

      @Override
      boolean update(Widget widget, StoredFile sf) {
        return false;
      }
    };

    private final String label;
    private final String caption;

    private final boolean alwaysVisible;
    private final boolean readOnly;

    private Column(String label, String caption, boolean alwaysVisible, boolean readOnly) {
      this.label = label;
      this.caption = caption;
      this.alwaysVisible = alwaysVisible;
      this.readOnly = readOnly;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    abstract Widget createDisplay();

    abstract Widget createEditor(StoredFile sf);

    abstract void refresh(Widget widget, StoredFile sf);

    abstract boolean update(Widget widget, StoredFile sf);

    private String getLabel() {
      return label;
    }

    private boolean isAlwaysVisible() {
      return alwaysVisible;
    }

    private boolean isReadOnly() {
      return readOnly;
    }
  }

  private static final String STYLE_PREFIX = "bee-FileGroup-";

  private static final String STYLE_CELL = "Cell";

  private static final String STYLE_COLUMN = "Column";

  private static final String STYLE_ROW = "Row";
  private static final String STYLE_EDITING = "editing";
  private static final String STYLE_EDITOR = "editor-";

  private static final String STYLE_CAPTION = "caption";
  private static final String STYLE_INPUT = "input";

  private static final List<Column> DEFAULT_VISIBLE_COLUMNS =
      Lists.newArrayList(Column.ICON, Column.NAME, Column.SIZE);
  private static final List<Column> DEFAULT_EDITABLE_COLUMNS = Lists.newArrayList();

  public static Widget getDefaultFace() {
    return new BeeButton("Pasirinkite bylas");
  }

  public static List<Column> parseColumns(String input) {
    List<Column> columns = Lists.newArrayList();
    if (BeeUtils.isEmpty(input) || BeeUtils.same(input, BeeConst.NONE)) {
      return columns;
    }

    if (BeeUtils.same(input, BeeConst.ALL)) {
      Collections.addAll(columns, Column.values());
      return columns;
    }

    List<String> labels = NameUtils.toList(input);
    for (String label : labels) {
      for (Column column : Column.values()) {
        if (BeeUtils.inListSame(label, column.getLabel(), column.getCaption())) {
          if (!columns.contains(column)) {
            columns.add(column);
          }
        }
      }
    }
    return columns;
  }

  private final List<StoredFile> files = Lists.newArrayList();

  private String caption = null;
  private String options = null;

  private final List<Column> columns = Lists.newArrayList();
  private final List<Column> editable = Lists.newArrayList();

  public FileGroup() {
    this(DEFAULT_VISIBLE_COLUMNS);
  }

  public FileGroup(List<Column> visibleColumns) {
    this(visibleColumns, DEFAULT_EDITABLE_COLUMNS);
  }

  public FileGroup(List<Column> visibleColumns, List<Column> editableColumns) {
    this.addStyleName(STYLE_PREFIX + "panel");

    initColumns(visibleColumns, editableColumns);
  }

  public void addFile(final StoredFile sf) {
    if (sf == null || contains(sf)) {
      return;
    }

    getFiles().add(sf);

    int row = getRowCount();
    insertRow(row);
    getRowFormatter().addStyleName(row, STYLE_PREFIX + STYLE_ROW);

    for (int col = 0; col < columns.size(); col++) {
      Column column = columns.get(col);

      Widget widget = column.createDisplay();
      widget.addStyleName(STYLE_PREFIX + column.getLabel());

      switch (column) {
        case EDIT:
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              int index = getIndex(sf.getFileId());
              if (index >= 0 && !editable.isEmpty()) {
                edit(index);
              }
            }
          });
          break;

        case DELETE:
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              int index = getIndex(sf.getFileId());
              if (index >= 0) {
                getFiles().remove(index);
                removeRow(index);
              }
            }
          });
          break;

        default:
          column.refresh(widget, sf);
      }

      setWidget(row, col, widget, STYLE_PREFIX + column.getLabel() + STYLE_CELL);
      if (row == 0) {
        getColumnFormatter().addStyleName(col, STYLE_PREFIX + column.getLabel() + STYLE_COLUMN);
      }
    }
  }

  public void addFiles(List<StoredFile> storedFiles) {
    if (storedFiles != null) {
      for (StoredFile sf : storedFiles) {
        addFile(sf);
      }
    }
  }

  @Override
  public void clear() {
    getFiles().clear();
    super.clear();
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public List<StoredFile> getFiles() {
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
  public boolean isEmpty() {
    return getFiles().isEmpty();
  }

  public void render(String serialized) {
    if (!isEmpty()) {
      clear();
    }
    addFiles(StoredFile.restoreCollection(serialized));
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  private boolean contains(StoredFile sf) {
    return getIndex(sf.getFileId()) >= 0;
  }

  private void edit(final int index) {
    getRowFormatter().addStyleName(index, STYLE_PREFIX + STYLE_EDITING);

    final StoredFile sf = getFiles().get(index);

    String pfx = STYLE_PREFIX + STYLE_EDITOR;

    final HtmlTable panel = new HtmlTable();
    panel.addStyleName(pfx + "panel");

    int colCaption = 0;
    final int colInput = 1;

    int row = 0;

    for (Column column : editable) {
      BeeLabel captionWidget = new BeeLabel(column.getCaption());
      captionWidget.addStyleName(pfx + STYLE_CAPTION);
      panel.setWidget(row, colCaption, captionWidget, pfx + STYLE_CAPTION + STYLE_CELL);

      Widget editor = column.createEditor(sf);
      editor.addStyleName(pfx + column.getLabel());
      panel.setWidget(row, colInput, editor, pfx + column.getLabel() + STYLE_CELL);

      panel.getRowFormatter().addStyleName(row, pfx + column.getLabel() + STYLE_ROW);
      if (row == 0) {
        panel.getColumnFormatter().addStyleName(colCaption, pfx + STYLE_CAPTION + STYLE_COLUMN);
        panel.getColumnFormatter().addStyleName(colInput, pfx + STYLE_INPUT + STYLE_COLUMN);
      }
      row++;
    }

    Global.inputWidget("Bylos duomenų koregavimas", panel, new InputCallback() {
      @Override
      public void onCancel() {
        getRowFormatter().removeStyleName(index, STYLE_PREFIX + STYLE_EDITING);
      }

      @Override
      public void onSuccess() {
        getRowFormatter().removeStyleName(index, STYLE_PREFIX + STYLE_EDITING);

        Set<Column> changedColumns = Sets.newHashSet();
        for (int i = 0; i < editable.size(); i++) {
          Column column = editable.get(i);
          if (column.update(panel.getWidget(i, colInput), sf)) {
            changedColumns.add(column);
          }
        }

        if (!changedColumns.isEmpty()) {
          refresh(index, changedColumns);
        }
      }
    }, false, null, getWidget(index, columns.indexOf(Column.EDIT)));
  }

  private int getIndex(long fileId) {
    for (int i = 0; i < getFiles().size(); i++) {
      if (getFiles().get(i).getFileId() == fileId) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private void initColumns(List<Column> visibleColumns, List<Column> editableColumns) {
    if (!BeeUtils.isEmpty(editableColumns)) {
      for (Column column : editableColumns) {
        if (!column.isReadOnly() && !editable.contains(column)) {
          editable.add(column);
        }
      }
    }

    List<Column> vc = Lists.newArrayList();
    if (BeeUtils.isEmpty(visibleColumns)) {
      vc.addAll(DEFAULT_VISIBLE_COLUMNS);
    } else {
      vc.addAll(visibleColumns);
    }

    for (Column column : vc) {
      if (Column.EDIT.equals(column) && editable.isEmpty()) {
        continue;
      }
      if (!columns.contains(column)) {
        columns.add(column);
      }
    }

    for (Column column : Column.values()) {
      if (column.isAlwaysVisible() && !columns.contains(column)) {
        columns.add(column);
      }
    }

    for (Column column : editable) {
      if (!columns.contains(column)) {
        columns.add(column);
      }
    }

    if (!editable.isEmpty() && !columns.contains(Column.EDIT)) {
      columns.add(Column.EDIT);
    }
  }

  private void refresh(int row, Collection<Column> changedColumns) {
    StoredFile sf = getFiles().get(row);

    for (int col = 0; col < columns.size(); col++) {
      Column column = columns.get(col);
      if (changedColumns.contains(column)) {
        column.refresh(getWidget(row, col), sf);
      }
    }
  }
}