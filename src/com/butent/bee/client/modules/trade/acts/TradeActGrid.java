package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.event.logical.ActiveRowChangeEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.EnumUtils;

public class TradeActGrid extends AbstractGridInterceptor {

  private final TradeActKind kind;

  private Button supplementCommand;
  private Button returnCommand;
  private Button copyCommand;
  private Button templateCommand;

  TradeActGrid(TradeActKind kind) {
    super();
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
  public String getCaption() {
    if (kind == null) {
      return Localized.getConstants().tradeActsAll();
    } else {
      return Localized.getConstants().tradeActs() + " - " + kind.getCaption();
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActGrid(kind);
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    Filter filter = (kind == null) ? null : kind.getFilter();
    gridDescription.setFilter(filter);
    return true;
  }

  @Override
  public void onActiveRowChange(ActiveRowChangeEvent event) {
    TradeActKind k = getRowKind(event.getRowValue());

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

  private TradeActKind getRowKind(IsRow row) {
    if (row == null) {
      return null;
    } else {
      return EnumUtils.getEnumByIndex(TradeActKind.class,
          row.getInteger(getDataIndex(COL_TA_KIND)));
    }
  }
}
