package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeDocumentStatus implements HasLocalizedCaption {
  ORDER {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusOrder();
    }
  },
  PENDING {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusPending();
    }
  },
  SIMPLE {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusSimple();
    }
  },
  APPROVED {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.trdDocumentStatusApproved();
    }
  }
}
