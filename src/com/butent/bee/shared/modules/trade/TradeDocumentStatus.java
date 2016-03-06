package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeDocumentStatus implements HasLocalizedCaption {
  ORDER {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusOrder();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "StatusOrder";
    }
  },
  PENDING {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusPending();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "StatusPending";
    }
  },
  SIMPLE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusSimple();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "StatusSimple";
    }
  },
  APPROVED {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusApproved();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "StatusApproved";
    }
  };

  public abstract String getDocumentTypeColumnName();
}
