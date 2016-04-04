package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeDocumentPhase implements HasLocalizedCaption {
  ORDER {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhaseOrder();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhaseOrder";
    }
  },
  PENDING {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhasePending();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhasePending";
    }
  },
  ACTIVE {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhaseActive();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhaseActive";
    }
  },
  COMPLETED {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhaseCompleted();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhaseCompleted";
    }
  },
  APPROVED {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhaseApproved();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhaseApproved";
    }
  };

  public abstract String getDocumentTypeColumnName();
}
