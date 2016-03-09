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
  ACTIVE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusActive();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "StatusActive";
    }
  },
  COMPLETED {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusCompleted();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "StatusCompleted";
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
