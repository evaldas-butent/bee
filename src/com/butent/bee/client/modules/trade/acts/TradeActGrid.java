package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.FilterComponent;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class TradeActGrid extends AbstractGridInterceptor {

  private final TradeActKind kind;

  private Button supplementCommand;
  private Button returnCommand;
  private Button copyCommand;
  private Button templateCommand;

  private TradeActKind newActKind;

  TradeActGrid(TradeActKind kind) {
    this.kind = kind;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if ((kind == null || kind == TradeActKind.SALE)
        && BeeKeeper.getUser().canCreateData(VIEW_TRADE_ACTS)) {
      presenter.getHeader().addCommandItem(ensureSupplementCommand());
      presenter.getHeader().addCommandItem(ensureReturnCommand());
    }

    if ((kind == null || kind.enableCopy())
        && BeeKeeper.getUser().canCreateData(VIEW_TRADE_ACTS)) {
      presenter.getHeader().addCommandItem(ensureCopyCommand());
    }
    if ((kind == null || kind.enableTemplate())
        && BeeKeeper.getUser().canCreateData(VIEW_TRADE_ACT_TEMPLATES)) {
      presenter.getHeader().addCommandItem(ensureTemplateCommand());
    }

    super.afterCreatePresenter(presenter);
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    newActKind = kind;

    if (kind == null) {
      final List<TradeActKind> kinds = Lists.newArrayList(TradeActKind.SALE, TradeActKind.TENDER,
          TradeActKind.PURCHASE, TradeActKind.WRITE_OFF, TradeActKind.RESERVE);

      List<String> options = new ArrayList<>();
      for (TradeActKind k : kinds) {
        options.add(k.getCaption());
      }

      Global.choice(Localized.getConstants().tradeActNew(), null, options, new ChoiceCallback() {
        @Override
        public void onSuccess(int value) {
          if (BeeUtils.isIndex(kinds, value)) {
            newActKind = kinds.get(value);
            getGridView().startNewRow(false);
          }
        }
      });

      return false;

    } else {
      return super.beforeAddRow(presenter, copy);
    }
  }

  @Override
  public List<FilterComponent> getInitialUserFilters(List<FilterComponent> defaultFilters) {
    if (!BeeUtils.isEmpty(defaultFilters)) {
      for (FilterComponent component : defaultFilters) {
        if (component != null && BeeUtils.same(component.getName(), COL_TA_SERIES)) {
          return super.getInitialUserFilters(defaultFilters);
        }
      }
    }

    BeeRowSet series = TradeActKeeper.getUserSeries();
    if (DataUtils.isEmpty(series)) {
      return super.getInitialUserFilters(defaultFilters);
    }

    List<FilterComponent> result = new ArrayList<>();
    if (!BeeUtils.isEmpty(defaultFilters)) {
      result.addAll(defaultFilters);
    }

    FilterComponent component = new FilterComponent(COL_TA_SERIES,
        FilterValue.of(DataUtils.buildIdList(series)));
    result.add(component);

    return result;
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActGrid(kind);
  }

  @Override
  public void onActiveRowChange(ActiveRowChangeEvent event) {
    TradeActKind k = TradeActKeeper.getKind(event.getRowValue(), getDataIndex(COL_TA_KIND));

    if (supplementCommand != null) {
      TradeActKeeper.setCommandEnabled(supplementCommand, k == TradeActKind.SALE);
    }
    if (returnCommand != null) {
      TradeActKeeper.setCommandEnabled(returnCommand, k == TradeActKind.SALE);
    }

    if (copyCommand != null) {
      TradeActKeeper.setCommandEnabled(copyCommand, k != null && k.enableCopy());
    }
    if (templateCommand != null) {
      TradeActKeeper.setCommandEnabled(templateCommand, k != null && k.enableTemplate());
    }

    super.onActiveRowChange(event);
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    TradeActKeeper.prepareNewTradeAct(newRow, newActKind);
    return super.onStartNewRow(gridView, oldRow, newRow);
  }

  private Button ensureCopyCommand() {
    if (copyCommand == null) {
      copyCommand = new Button(Localized.getConstants().actionCopy(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
          });

      TradeActKeeper.addCommandStyle(copyCommand, "copy");
      TradeActKeeper.setCommandEnabled(copyCommand, false);
    }
    return copyCommand;
  }

  private Button ensureReturnCommand() {
    if (returnCommand == null) {
      returnCommand = new Button(Localized.getConstants().taKindReturn(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
          });

      TradeActKeeper.addCommandStyle(returnCommand, "return");
      TradeActKeeper.setCommandEnabled(returnCommand, false);
    }
    return returnCommand;
  }

  private Button ensureSupplementCommand() {
    if (supplementCommand == null) {
      supplementCommand = new Button(Localized.getConstants().taKindSupplement(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
          });

      TradeActKeeper.addCommandStyle(supplementCommand, "supplement");
      TradeActKeeper.setCommandEnabled(supplementCommand, false);
    }
    return supplementCommand;
  }

  private Button ensureTemplateCommand() {
    if (templateCommand == null) {
      templateCommand = new Button(Localized.getConstants().tradeActSaveAsTemplate(),
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
          });

      TradeActKeeper.addCommandStyle(templateCommand, "template");
      TradeActKeeper.setCommandEnabled(templateCommand, false);
    }
    return templateCommand;
  }
}
