package com.butent.bee.client.presenter;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.HasKeyProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;

import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.HasKeyboardPaging;
import com.butent.bee.shared.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DataPresenter<T> implements HasData<T>, HasKeyProvider<T>, HasKeyboardPaging,
    HasKeyboardSelectionPolicy {

  private static class DefaultState<T> implements State<T> {
    int keyboardSelectedRow = 0;
    T keyboardSelectedRowValue = null;
    int pageSize;
    int pageStart = 0;
    int rowCount = 0;
    boolean rowCountIsExact = false;
    final List<T> rowData = new ArrayList<T>();
    final Set<Integer> selectedRows = new HashSet<Integer>();
    T selectedValue = null;
    boolean viewTouched;

    public DefaultState(int pageSize) {
      this.pageSize = pageSize;
    }

    public int getKeyboardSelectedRow() {
      return keyboardSelectedRow;
    }

    public T getKeyboardSelectedRowValue() {
      return keyboardSelectedRowValue;
    }

    public int getPageSize() {
      return pageSize;
    }

    public int getPageStart() {
      return pageStart;
    }

    public int getRowCount() {
      return rowCount;
    }

    public int getRowDataSize() {
      return rowData.size();
    }

    public T getRowDataValue(int index) {
      return rowData.get(index);
    }

    public List<T> getRowDataValues() {
      return Collections.unmodifiableList(rowData);
    }

    public T getSelectedValue() {
      return selectedValue;
    }

    public boolean isRowCountExact() {
      return rowCountIsExact;
    }

    public boolean isRowSelected(int index) {
      return selectedRows.contains(index);
    }

    public boolean isViewTouched() {
      return viewTouched;
    }
  }

  private static class PendingState<T> extends DefaultState<T> {

    private boolean keyboardSelectedRowChanged;

    private boolean keyboardStealFocus = false;

    private boolean redrawRequired = false;

    private final List<Range> replacedRanges = new ArrayList<Range>();

    public PendingState(State<T> state) {
      super(state.getPageSize());
      this.keyboardSelectedRow = state.getKeyboardSelectedRow();
      this.keyboardSelectedRowValue = state.getKeyboardSelectedRowValue();
      this.pageSize = state.getPageSize();
      this.pageStart = state.getPageStart();
      this.rowCount = state.getRowCount();
      this.rowCountIsExact = state.isRowCountExact();
      this.selectedValue = state.getSelectedValue();
      this.viewTouched = state.isViewTouched();

      int rowDataSize = state.getRowDataSize();
      for (int i = 0; i < rowDataSize; i++) {
        this.rowData.add(state.getRowDataValue(i));
      }
    }

    public void replaceRange(int start, int end) {
      replacedRanges.add(new Range(start, end - start));
    }
  }

  private static interface State<T> {

    int getKeyboardSelectedRow();

    T getKeyboardSelectedRowValue();

    int getPageSize();

    int getPageStart();

    int getRowCount();

    int getRowDataSize();

    T getRowDataValue(int index);

    List<T> getRowDataValues();

    T getSelectedValue();

    boolean isRowCountExact();

    boolean isRowSelected(int index);

    boolean isViewTouched();
  }

  static final int PAGE_INCREMENT = 30;

  private static final int LOOP_MAXIMUM = 10;

  private static final int REDRAW_MINIMUM = 5;

  private static final double REDRAW_THRESHOLD = 0.30;

  private final HasData<T> display;

  private boolean isResolvingState;

  private KeyboardPagingPolicy keyboardPagingPolicy = KeyboardPagingPolicy.CHANGE_PAGE;
  private KeyboardSelectionPolicy keyboardSelectionPolicy = KeyboardSelectionPolicy.ENABLED;

  private final ProvidesKey<T> keyProvider;

  private SafeHtml lastContents = null;

  private PendingState<T> pendingState;

  private ScheduledCommand pendingStateCommand;

  private int pendingStateLoop = 0;

  private HandlerRegistration selectionHandler;
  private SelectionModel<? super T> selectionModel;

  private State<T> state;

  private final DataView<T> view;

  public DataPresenter(HasData<T> display, DataView<T> view, int pageSize, ProvidesKey<T> keyProvider) {
    this.display = display;
    this.view = view;
    this.keyProvider = keyProvider;
    this.state = new DefaultState<T>(pageSize);
  }

  public HandlerRegistration addCellPreviewHandler(CellPreviewEvent.Handler<T> handler) {
    return view.addHandler(handler, CellPreviewEvent.getType());
  }

  public HandlerRegistration addLoadingStateChangeHandler(LoadingStateChangeEvent.Handler handler) {
    return view.addHandler(handler, LoadingStateChangeEvent.TYPE);
  }

  public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler) {
    return view.addHandler(handler, RangeChangeEvent.getType());
  }

  public HandlerRegistration addRowCountChangeHandler(RowCountChangeEvent.Handler handler) {
    return view.addHandler(handler, RowCountChangeEvent.getType());
  }

  public void clearKeyboardSelectedRowValue() {
    if (getKeyboardSelectedRowValue() != null) {
      ensurePendingState().keyboardSelectedRowValue = null;
    }
  }

  public void clearSelectionModel() {
    if (selectionHandler != null) {
      selectionHandler.removeHandler();
      selectionHandler = null;
    }
    selectionModel = null;
  }

  public void fireEvent(GwtEvent<?> event) {
    Assert.untouchable();
  }

  public void flush() {
    while (pendingStateCommand != null && !isResolvingState) {
      resolvePendingState();
    }
  }

  public int getCurrentPageSize() {
    return Math.min(getPageSize(), getRowCount() - getPageStart());
  }

  public KeyboardPagingPolicy getKeyboardPagingPolicy() {
    return keyboardPagingPolicy;
  }

  public int getKeyboardSelectedRow() {
    return KeyboardSelectionPolicy.DISABLED == keyboardSelectionPolicy ? -1
        : getCurrentState().getKeyboardSelectedRow();
  }

  public int getKeyboardSelectedRowInView() {
    return KeyboardSelectionPolicy.DISABLED == keyboardSelectionPolicy ? -1
        : state.getKeyboardSelectedRow();
  }

  public T getKeyboardSelectedRowValue() {
    return KeyboardSelectionPolicy.DISABLED == keyboardSelectionPolicy ? null
        : getCurrentState().getKeyboardSelectedRowValue();
  }

  public KeyboardSelectionPolicy getKeyboardSelectionPolicy() {
    return keyboardSelectionPolicy;
  }

  public ProvidesKey<T> getKeyProvider() {
    return keyProvider;
  }

  public int getRowCount() {
    return getCurrentState().getRowCount();
  }

  public SelectionModel<? super T> getSelectionModel() {
    return selectionModel;
  }

  public T getVisibleItem(int indexOnPage) {
    return getCurrentState().getRowDataValue(indexOnPage);
  }

  public int getVisibleItemCount() {
    return getCurrentState().getRowDataSize();
  }

  public List<T> getVisibleItems() {
    return getCurrentState().getRowDataValues();
  }

  public Range getVisibleRange() {
    return new Range(getPageStart(), getPageSize());
  }

  public boolean hasKeyboardNext() {
    if (KeyboardSelectionPolicy.DISABLED == keyboardSelectionPolicy) {
      return false;
    } else if (getKeyboardSelectedRow() < getVisibleItemCount() - 1) {
      return true;
    } else if (!keyboardPagingPolicy.isLimitedToRange()
        && (getKeyboardSelectedRow() + getPageStart() < getRowCount() - 1 || !isRowCountExact())) {
      return true;
    }
    return false;
  }

  public boolean hasKeyboardPrev() {
    if (KeyboardSelectionPolicy.DISABLED == keyboardSelectionPolicy) {
      return false;
    } else if (getKeyboardSelectedRow() > 0) {
      return true;
    } else if (!keyboardPagingPolicy.isLimitedToRange() && getPageStart() > 0) {
      return true;
    }
    return false;
  }

  public boolean hasPendingState() {
    return pendingState != null;
  }

  public boolean isEmpty() {
    return isRowCountExact() && getRowCount() == 0;
  }

  public boolean isRowCountExact() {
    return getCurrentState().isRowCountExact();
  }

  public void keyboardEnd() {
    if (!keyboardPagingPolicy.isLimitedToRange()) {
      setKeyboardSelectedRow(getRowCount() - 1, true, false);
    }
  }

  public void keyboardHome() {
    if (!keyboardPagingPolicy.isLimitedToRange()) {
      setKeyboardSelectedRow(-getPageStart(), true, false);
    }
  }

  public void keyboardNext() {
    if (hasKeyboardNext()) {
      setKeyboardSelectedRow(getKeyboardSelectedRow() + 1, true, false);
    }
  }

  public void keyboardNextPage() {
    if (KeyboardPagingPolicy.CHANGE_PAGE == keyboardPagingPolicy) {
      setKeyboardSelectedRow(getPageSize(), true, false);
    } else if (KeyboardPagingPolicy.INCREASE_RANGE == keyboardPagingPolicy) {
      setKeyboardSelectedRow(getKeyboardSelectedRow() + PAGE_INCREMENT, true, false);
    }
  }

  public void keyboardPrev() {
    if (hasKeyboardPrev()) {
      setKeyboardSelectedRow(getKeyboardSelectedRow() - 1, true, false);
    }
  }

  public void keyboardPrevPage() {
    if (KeyboardPagingPolicy.CHANGE_PAGE == keyboardPagingPolicy) {
      setKeyboardSelectedRow(-getPageSize(), true, false);
    } else if (KeyboardPagingPolicy.INCREASE_RANGE == keyboardPagingPolicy) {
      setKeyboardSelectedRow(getKeyboardSelectedRow() - PAGE_INCREMENT, true, false);
    }
  }

  public void redraw() {
    lastContents = null;
    ensurePendingState().redrawRequired = true;
  }

  public void setKeyboardPagingPolicy(KeyboardPagingPolicy policy) {
    if (policy == null) {
      throw new NullPointerException("KeyboardPagingPolicy cannot be null");
    }
    this.keyboardPagingPolicy = policy;
  }

  public void setKeyboardSelectedRow(int index, boolean stealFocus, boolean forceUpdate) {
    if (KeyboardSelectionPolicy.DISABLED == keyboardSelectionPolicy) {
      return;
    }

    ensurePendingState().viewTouched = true;

    if (!forceUpdate && getKeyboardSelectedRow() == index
        && getKeyboardSelectedRowValue() != null) {
      return;
    }

    int pageStart = getPageStart();
    int pageSize = getPageSize();
    int rowCount = getRowCount();
    int absIndex = pageStart + index;
    if (absIndex >= rowCount && isRowCountExact()) {
      absIndex = rowCount - 1;
    }
    index = Math.max(0, absIndex) - pageStart;
    if (keyboardPagingPolicy.isLimitedToRange()) {
      index = Math.max(0, Math.min(index, pageSize - 1));
    }

    int newPageStart = pageStart;
    int newPageSize = pageSize;
    PendingState<T> pending = ensurePendingState();
    pending.keyboardSelectedRow = 0;
    pending.keyboardSelectedRowValue = null;
    pending.keyboardSelectedRowChanged = true;

    if (index >= 0 && index < pageSize) {
      pending.keyboardSelectedRow = index;
      pending.keyboardSelectedRowValue = index < pending.getRowDataSize()
          ? ensurePendingState().getRowDataValue(index) : null;
      pending.keyboardStealFocus = stealFocus;
      return;
    } else if (KeyboardPagingPolicy.CHANGE_PAGE == keyboardPagingPolicy) {
      while (index < 0) {
        newPageStart -= pageSize;
        index += pageSize;
      }

      if (newPageStart < 0) {
        newPageStart = 0;
        index = 0;
      }

      while (index >= pageSize) {
        newPageStart += pageSize;
        index -= pageSize;
      }
    } else if (KeyboardPagingPolicy.INCREASE_RANGE == keyboardPagingPolicy) {
      while (index < 0) {
        newPageSize += PAGE_INCREMENT;
        newPageStart -= PAGE_INCREMENT;
        index += PAGE_INCREMENT;
      }
      if (newPageStart < 0) {
        index += newPageStart;
        newPageSize += newPageStart;
        newPageStart = 0;
      }

      while (index >= newPageSize) {
        newPageSize += PAGE_INCREMENT;
      }
      if (isRowCountExact()) {
        newPageSize = Math.min(newPageSize, rowCount - newPageStart);
        if (index >= rowCount) {
          index = rowCount - 1;
        }
      }
    }

    if (newPageStart != pageStart || newPageSize != pageSize) {
      pending.keyboardSelectedRow = index;
      setVisibleRange(new Range(newPageStart, newPageSize), false, false);
    }
  }

  public void setKeyboardSelectionPolicy(KeyboardSelectionPolicy policy) {
    if (policy == null) {
      throw new NullPointerException("KeyboardSelectionPolicy cannot be null");
    }
    this.keyboardSelectionPolicy = policy;
  }

  public final void setRowCount(int count) {
    Assert.untouchable();
  }

  public void setRowCount(int count, boolean isExact) {
    if (count == getRowCount() && isExact == isRowCountExact()) {
      return;
    }
    ensurePendingState().rowCount = count;
    ensurePendingState().rowCountIsExact = isExact;

    updateCachedData();

    RowCountChangeEvent.fire(display, count, isExact);
  }

  public void setRowData(int start, List<? extends T> values) {
    int valuesLength = values.size();
    int valuesEnd = start + valuesLength;

    int pageStart = getPageStart();
    int pageEnd = getPageStart() + getPageSize();
    int boundedStart = Math.max(start, pageStart);
    int boundedEnd = Math.min(valuesEnd, pageEnd);
    if (start != pageStart && boundedStart >= boundedEnd) {
      return;
    }

    PendingState<T> pending = ensurePendingState();
    int cacheOffset = Math.max(0, boundedStart - pageStart - getVisibleItemCount());
    for (int i = 0; i < cacheOffset; i++) {
      pending.rowData.add(null);
    }

    for (int i = boundedStart; i < boundedEnd; i++) {
      T value = values.get(i - start);
      int dataIndex = i - pageStart;
      if (dataIndex < getVisibleItemCount()) {
        pending.rowData.set(dataIndex, value);
      } else {
        pending.rowData.add(value);
      }
    }

    pending.replaceRange(boundedStart - cacheOffset, boundedEnd);

    if (valuesEnd > getRowCount()) {
      setRowCount(valuesEnd, isRowCountExact());
    }
  }

  public void setSelectionModel(final SelectionModel<? super T> selectionModel) {
    clearSelectionModel();

    this.selectionModel = selectionModel;
    if (selectionModel != null) {
      selectionHandler =
          selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
              ensurePendingState();
            }
          });
    }

    ensurePendingState();
  }

  public final void setVisibleRange(int start, int length) {
    Assert.untouchable();
  }

  public void setVisibleRange(Range range) {
    setVisibleRange(range, false, false);
  }

  public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
    setVisibleRange(range, true, forceRangeChangeEvent);
  }

  protected void scheduleFinally(ScheduledCommand command) {
    Scheduler.get().scheduleFinally(command);
  }

  List<Range> calculateModifiedRanges(TreeSet<Integer> modifiedRows, int pageStart, int pageEnd) {
    int rangeStart0 = -1;
    int rangeEnd0 = -1;
    int rangeStart1 = -1;
    int rangeEnd1 = -1;
    int maxDiff = 0;

    for (int index : modifiedRows) {
      if (index < pageStart || index >= pageEnd) {
        continue;
      } else if (rangeStart0 == -1) {
        rangeStart0 = index;
        rangeEnd0 = index;
      } else if (rangeStart1 == -1) {
        maxDiff = index - rangeEnd0;
        rangeStart1 = index;
        rangeEnd1 = index;
      } else {
        int diff = index - rangeEnd1;
        if (diff > maxDiff) {
          rangeEnd0 = rangeEnd1;
          rangeStart1 = index;
          rangeEnd1 = index;
          maxDiff = diff;
        } else {
          rangeEnd1 = index;
        }
      }
    }

    rangeEnd0 += 1;
    rangeEnd1 += 1;

    if (rangeStart1 == rangeEnd0) {
      rangeEnd0 = rangeEnd1;
      rangeStart1 = -1;
      rangeEnd1 = -1;
    }

    List<Range> toRet = new ArrayList<Range>();
    if (rangeStart0 != -1) {
      int rangeLength0 = rangeEnd0 - rangeStart0;
      toRet.add(new Range(rangeStart0, rangeLength0));
    }
    if (rangeStart1 != -1) {
      int rangeLength1 = rangeEnd1 - rangeStart1;
      toRet.add(new Range(rangeStart1, rangeLength1));
    }
    return toRet;
  }

  private PendingState<T> ensurePendingState() {
    if (pendingState == null) {
      pendingState = new PendingState<T>(state);
    }

    pendingStateCommand = new ScheduledCommand() {
      public void execute() {
        if (pendingStateCommand == this) {
          resolvePendingState();
        }
      }
    };
    scheduleFinally(pendingStateCommand);

    return pendingState;
  }

  private int findIndexOfBestMatch(State<T> st, T value, int initialIndex) {
    Object key = getRowValueKey(value);
    if (key == null) {
      return -1;
    }

    int bestMatchIndex = -1;
    int bestMatchDiff = Integer.MAX_VALUE;
    int rowDataCount = st.getRowDataSize();

    for (int i = 0; i < rowDataCount; i++) {
      T curValue = st.getRowDataValue(i);
      Object curKey = getRowValueKey(curValue);
      if (key.equals(curKey)) {
        int diff = Math.abs(initialIndex - i);
        if (diff < bestMatchDiff) {
          bestMatchIndex = i;
          bestMatchDiff = diff;
        }
      }
    }
    return bestMatchIndex;
  }

  private State<T> getCurrentState() {
    return pendingState == null ? state : pendingState;
  }

  private int getPageSize() {
    return getCurrentState().getPageSize();
  }

  private int getPageStart() {
    return getCurrentState().getPageStart();
  }

  private Object getRowValueKey(T rowValue) {
    return (keyProvider == null || rowValue == null) ? rowValue : keyProvider.getKey(rowValue);
  }

  private void resolvePendingState() {
    pendingStateCommand = null;

    if (pendingState == null) {
      pendingStateLoop = 0;
      return;
    }

    pendingStateLoop++;
    if (pendingStateLoop > LOOP_MAXIMUM) {
      pendingStateLoop = 0;
      throw new IllegalStateException(
          "A possible infinite loop has been detected in a Cell Widget. This "
              + "usually happens when your SelectionModel triggers a "
              + "SelectionChangeEvent when SelectionModel.isSelection() is "
              + "called, which causes the table to redraw continuously.");
    }

    if (isResolvingState) {
      throw new IllegalStateException(
          "The Cell Widget is attempting to render itself within the render "
              + "loop. This usually happens when your render code modifies the "
              + "state of the Cell Widget then accesses data or elements "
              + "within the Widget.");
    }
    isResolvingState = true;

    TreeSet<Integer> modifiedRows = new TreeSet<Integer>();

    State<T> oldState = state;
    PendingState<T> pending = pendingState;
    int pageStart = pending.getPageStart();
    int pageSize = pending.getPageSize();
    int pageEnd = pageStart + pageSize;
    int rowDataCount = pending.getRowDataSize();

    pending.keyboardSelectedRow = Math.max(0,
        Math.min(pending.keyboardSelectedRow, rowDataCount - 1));

    if (KeyboardSelectionPolicy.DISABLED == keyboardSelectionPolicy) {
      pending.keyboardSelectedRow = 0;
      pending.keyboardSelectedRowValue = null;
    } else if (pending.keyboardSelectedRowChanged) {
      pending.keyboardSelectedRowValue = rowDataCount > 0
          ? pending.getRowDataValue(pending.keyboardSelectedRow) : null;
    } else if (pending.keyboardSelectedRowValue != null) {
      int bestMatchIndex = findIndexOfBestMatch(pending,
          pending.keyboardSelectedRowValue, pending.keyboardSelectedRow);
      if (bestMatchIndex >= 0) {
        pending.keyboardSelectedRow = bestMatchIndex;
        pending.keyboardSelectedRowValue = rowDataCount > 0
            ? pending.getRowDataValue(pending.keyboardSelectedRow) : null;
      } else {
        pending.keyboardSelectedRow = 0;
        pending.keyboardSelectedRowValue = null;
      }
    }

    try {
      if (KeyboardSelectionPolicy.BOUND_TO_SELECTION == keyboardSelectionPolicy
          && selectionModel != null && pending.viewTouched) {
        T oldValue = oldState.getSelectedValue();
        Object oldKey = getRowValueKey(oldValue);
        T newValue = rowDataCount > 0
            ? pending.getRowDataValue(pending.getKeyboardSelectedRow()) : null;
        Object newKey = getRowValueKey(newValue);
        if (newKey != null && !newKey.equals(oldKey)) {
          boolean oldValueWasSelected = (oldValue == null) ? false
              : selectionModel.isSelected(oldValue);
          boolean newValueWasSelected = (newValue == null) ? false
              : selectionModel.isSelected(newValue);

          if (oldValueWasSelected) {
            selectionModel.setSelected(oldValue, false);
          }

          pending.selectedValue = newValue;
          if (newValue != null && !newValueWasSelected) {
            selectionModel.setSelected(newValue, true);
          }
        }
      }
    } catch (RuntimeException e) {
      isResolvingState = false;
      throw e;
    }

    boolean keyboardRowChanged = pending.keyboardSelectedRowChanged
        || (oldState.getKeyboardSelectedRow() != pending.keyboardSelectedRow)
        || (oldState.getKeyboardSelectedRowValue() == null
            && pending.keyboardSelectedRowValue != null);

    for (int i = pageStart; i < pageStart + rowDataCount; i++) {
      T rowValue = pending.getRowDataValue(i - pageStart);
      boolean isSelected = (rowValue != null && selectionModel != null
          && selectionModel.isSelected(rowValue));

      boolean wasSelected = oldState.isRowSelected(i);
      if (isSelected) {
        pending.selectedRows.add(i);
        if (!wasSelected) {
          modifiedRows.add(i);
        }
      } else if (wasSelected) {
        modifiedRows.add(i);
      }
    }

    if (pendingStateCommand != null) {
      isResolvingState = false;
      return;
    }
    pendingStateLoop = 0;

    state = pendingState;
    pendingState = null;

    boolean replacedEmptyRange = false;
    for (Range replacedRange : pending.replacedRanges) {
      int start = replacedRange.getStart();
      int length = replacedRange.getLength();
      if (length == 0) {
        replacedEmptyRange = true;
      }
      for (int i = start; i < start + length; i++) {
        modifiedRows.add(i);
      }
    }

    if (modifiedRows.size() > 0 && keyboardRowChanged) {
      modifiedRows.add(oldState.getKeyboardSelectedRow());
      modifiedRows.add(pending.keyboardSelectedRow);
    }

    List<Range> modifiedRanges = calculateModifiedRanges(modifiedRows, pageStart, pageEnd);
    Range range0 = modifiedRanges.size() > 0 ? modifiedRanges.get(0) : null;
    Range range1 = modifiedRanges.size() > 1 ? modifiedRanges.get(1) : null;
    int replaceDiff = 0;
    for (Range range : modifiedRanges) {
      replaceDiff += range.getLength();
    }

    int oldPageStart = oldState.getPageStart();
    int oldPageSize = oldState.getPageSize();
    int oldRowDataCount = oldState.getRowDataSize();
    boolean redrawRequired = pending.redrawRequired;

    if (pageStart != oldPageStart) {
      redrawRequired = true;
    } else if (rowDataCount < oldRowDataCount) {
      redrawRequired = true;
    } else if (range1 == null && range0 != null
        && range0.getStart() == pageStart
        && (replaceDiff >= oldRowDataCount || replaceDiff > oldPageSize)) {
      redrawRequired = true;
    } else if (replaceDiff >= REDRAW_MINIMUM && replaceDiff > REDRAW_THRESHOLD * oldRowDataCount) {
      redrawRequired = true;
    } else if (replacedEmptyRange && oldRowDataCount == 0) {
      redrawRequired = true;
    }

    updateLoadingState();

    try {
      if (redrawRequired) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        view.render(sb, pending.rowData, pending.pageStart, selectionModel);
        SafeHtml newContents = sb.toSafeHtml();
        if (!newContents.equals(lastContents)) {
          lastContents = newContents;
          view.replaceAllChildren(pending.rowData, newContents, pending.keyboardStealFocus);
        }
        view.resetFocus();
      } else if (range0 != null) {
        lastContents = null;

        {
          int absStart = range0.getStart();
          int relStart = absStart - pageStart;
          SafeHtmlBuilder sb = new SafeHtmlBuilder();
          List<T> replaceValues = pending.rowData.subList(relStart, relStart + range0.getLength());
          view.render(sb, replaceValues, absStart, selectionModel);
          view.replaceChildren(replaceValues, relStart, sb.toSafeHtml(),
              pending.keyboardStealFocus);
        }

        if (range1 != null) {
          int absStart = range1.getStart();
          int relStart = absStart - pageStart;
          SafeHtmlBuilder sb = new SafeHtmlBuilder();
          List<T> replaceValues = pending.rowData.subList(relStart, relStart + range1.getLength());
          view.render(sb, replaceValues, absStart, selectionModel);
          view.replaceChildren(replaceValues, relStart, sb.toSafeHtml(),
              pending.keyboardStealFocus);
        }

        view.resetFocus();
      } else if (keyboardRowChanged) {
        int oldSelectedRow = oldState.getKeyboardSelectedRow();
        if (oldSelectedRow >= 0 && oldSelectedRow < rowDataCount) {
          view.setKeyboardSelected(oldSelectedRow, false, false);
        }

        int newSelectedRow = pending.getKeyboardSelectedRow();
        if (newSelectedRow >= 0 && newSelectedRow < rowDataCount) {
          view.setKeyboardSelected(newSelectedRow, true, pending.keyboardStealFocus);
        }
      }
    } finally {
      isResolvingState = false;
    }
  }

  private void setVisibleRange(Range range, boolean clearData, boolean forceRangeChangeEvent) {
    final int start = range.getStart();
    final int length = range.getLength();
    Assert.nonNegative(start);
    Assert.nonNegative(length);

    final int pageStart = getPageStart();
    final int pageSize = getPageSize();
    final boolean pageStartChanged = (pageStart != start);
    if (pageStartChanged) {
      PendingState<T> pending = ensurePendingState();

      if (!clearData) {
        if (start > pageStart) {
          int increase = start - pageStart;
          if (getVisibleItemCount() > increase) {
            for (int i = 0; i < increase; i++) {
              pending.rowData.remove(0);
            }
          } else {
            pending.rowData.clear();
          }
        } else {
          int decrease = pageStart - start;
          if ((getVisibleItemCount() > 0) && (decrease < pageSize)) {
            for (int i = 0; i < decrease; i++) {
              pending.rowData.add(0, null);
            }

            pending.replaceRange(start, start + decrease);
          } else {
            pending.rowData.clear();
          }
        }
      }
      pending.pageStart = start;
    }

    final boolean pageSizeChanged = (pageSize != length);
    if (pageSizeChanged) {
      ensurePendingState().pageSize = length;
    }

    if (clearData) {
      ensurePendingState().rowData.clear();
    }

    updateCachedData();

    if (pageStartChanged || pageSizeChanged || forceRangeChangeEvent) {
      RangeChangeEvent.fire(display, getVisibleRange());
    }
  }

  private void updateCachedData() {
    int pageStart = getPageStart();
    int expectedLastIndex = Math.max(0, Math.min(getPageSize(), getRowCount() - pageStart));
    int lastIndex = getVisibleItemCount() - 1;

    while (lastIndex >= expectedLastIndex) {
      ensurePendingState().rowData.remove(lastIndex);
      lastIndex--;
    }
  }

  private void updateLoadingState() {
    int cacheSize = getVisibleItemCount();
    int curPageSize = isRowCountExact() ? getCurrentPageSize() : getPageSize();
    if (cacheSize >= curPageSize) {
      view.setLoadingState(LoadingState.LOADED);
    } else if (cacheSize == 0) {
      view.setLoadingState(LoadingState.LOADING);
    } else {
      view.setLoadingState(LoadingState.PARTIALLY_LOADED);
    }
  }
}
