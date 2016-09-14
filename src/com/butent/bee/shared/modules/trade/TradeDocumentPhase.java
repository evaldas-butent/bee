package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeDocumentPhase implements HasLocalizedCaption {
  ORDER(false) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhaseOrder();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhaseOrder";
    }
  },
  PENDING(false) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhasePending();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhasePending";
    }
  },
  ACTIVE(true) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhaseActive();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhaseActive";
    }
  },
  COMPLETED(true) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhaseCompleted();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhaseCompleted";
    }
  },
  APPROVED(true) {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.trdDocumentPhaseApproved();
    }

    @Override
    public String getDocumentTypeColumnName() {
      return "PhaseApproved";
    }
  };

  private final boolean modifyStock;

  TradeDocumentPhase(boolean modifyStock) {
    this.modifyStock = modifyStock;
  }

  public abstract String getDocumentTypeColumnName();

  public String getStatusColumnName() {
    return getDocumentTypeColumnName();
  }

  public boolean modifyStock() {
    return modifyStock;
  }
}
