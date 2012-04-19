package com.butent.bee.client.validation;

public class CellValidateEvent {

  public interface Handler {
    boolean validateCell(CellValidateEvent event);
  }
  
  private final CellValidation cellValidation;
  
  private ValidationPhase validationPhase = null;
  private boolean canceled = false;
  
  public CellValidateEvent(CellValidation cellValidation) {
    super();
    this.cellValidation = cellValidation;
  }
  
  public void cancel() {
    setCanceled(true);
  }

  public CellValidation getCellValidation() {
    return cellValidation;
  }

  public ValidationPhase getValidationPhase() {
    return validationPhase;
  }
  
  public boolean isCanceled() {
    return canceled;
  }
  
  public boolean isPostValidation() {
    return validationPhase != null && validationPhase.isPostValidation();
  }
  
  public boolean isPreValidation() {
    return validationPhase != null && validationPhase.isPreValidation();
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  public void setValidationPhase(ValidationPhase validationPhase) {
    this.validationPhase = validationPhase;
  }
}
