package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

import java.util.EnumSet;

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

    @Override
    public boolean isEditable(boolean isAdministrator) {
      return true;
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

    @Override
    public boolean isEditable(boolean isAdministrator) {
      return true;
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

    @Override
    public boolean isEditable(boolean isAdministrator) {
      return true;
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

    @Override
    public boolean isEditable(boolean isAdministrator) {
      return isAdministrator;
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

    @Override
    public boolean isEditable(boolean isAdministrator) {
      return false;
    }
  };

  public static EnumSet<TradeDocumentPhase> getStockPhases() {
    EnumSet<TradeDocumentPhase> stockPhases = EnumSet.noneOf(TradeDocumentPhase.class);

    for (TradeDocumentPhase phase : values()) {
      if (phase.modifyStock()) {
        stockPhases.add(phase);
      }
    }

    return stockPhases;
  }

  private final boolean modifyStock;

  TradeDocumentPhase(boolean modifyStock) {
    this.modifyStock = modifyStock;
  }

  public abstract String getDocumentTypeColumnName();

  public String getStatusColumnName() {
    return getDocumentTypeColumnName();
  }

  public abstract boolean isEditable(boolean isAdministrator);

  public boolean modifyStock() {
    return modifyStock;
  }
}
