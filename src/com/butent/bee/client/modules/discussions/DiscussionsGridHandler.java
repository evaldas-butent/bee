package com.butent.bee.client.modules.discussions;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.client.modules.discussions.DiscussionsList.ListType;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AttachmentRenderer;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditorAssistant;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.search.AbstractFilterSupplier;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

class DiscussionsGridHandler extends AbstractGridInterceptor {

  private static final int DEFAULT_STAR_COUNT = 3;
  private static final String NAME_STAR = "Star";

  private final ListType type;
  private final Long userId;

  public DiscussionsGridHandler(ListType type) {
    this.type = type;
    this.userId = BeeKeeper.getUser().getUserId();
  }

  @Override
  public String getColumnCaption(String columnName) {
    if (PROP_STAR.equals(columnName)) {
      return Stars.getDefaultHeader();
    } else if (PROP_ATTACHMENT.equals(columnName)) {
      return Images.asString(Images.get(AttachmentRenderer.IMAGE_ATTACHMENT));
    } else {
      return super.getColumnCaption(columnName);
    }
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {
    return DeleteMode.SINGLE;
  }

  @Override
  public AbstractFilterSupplier getFilterSupplier(String columnName,
      ColumnDescription columnDescription) {
    if (BeeUtils.same(columnName, NAME_STAR)) {
      return new DiscussStarFilterSupplier(columnDescription.getFilterOptions());
    } else {
      return super.getFilterSupplier(columnName, columnDescription);
    }
  }

  @Override
  public void onEditStart(final EditStartEvent event) {
    if (PROP_STAR.equals(event.getColumnId())) {
      IsRow row = event.getRowValue();
      if (row == null) {
        return;
      }

      if (row.getProperty(PROP_USER) == null) {
        return;
      }

      final CellSource source = CellSource.forProperty(PROP_STAR, ValueType.INTEGER);
      EditorAssistant.editStarCell(DEFAULT_STAR_COUNT, event, source, new Consumer<Integer>() {
        @Override
        public void accept(Integer parameter) {
          updateStar(event, source, parameter);
        }
      });
    }
  }

  @Override
  public boolean onLoad(GridDescription gridDescription) {
    gridDescription.setFilter(type.getFilter(new LongValue(userId)));
    return true;
  }

  private void updateStar(final EditStartEvent event, final CellSource source,
      final Integer value) {
    final long rowId = event.getRowValue().getId();

    Filter filter = Filter.and(ComparisonFilter.isEqual(COL_DISCUSSION, new LongValue(rowId)),
        ComparisonFilter.isEqual(CommonsConstants.COL_USER, new LongValue(userId)));

    Queries.update(VIEW_DISCUSSIONS_USERS, filter, COL_STAR, new IntegerValue(value),
        new Queries.IntCallback() {

          @Override
          public void onSuccess(Integer result) {
            BeeKeeper.getBus().fireEvent(
                new CellUpdateEvent(VIEW_DISCUSSIONS, rowId, event.getRowValue().getVersion(),
                    source, (value == null) ? null : BeeUtils.toString(value)));
          }
        });
  }
}
