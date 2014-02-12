package com.butent.bee.shared.modules.calendar;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskPriority;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskStatus;
import com.butent.bee.shared.modules.crm.TaskType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;

public class CalendarTask implements BeeSerializable {
  
  private enum Serial {
    TYPE, ID, START, END, SUMMARY, DESCRIPTION, COMPANY_NAME, PRIORITY, STATUS, OWNER, EXECUTOR,
    OBSERVERS, BACKGRUOND, FOREGROUND, STYLE 
  }
  
  public static CalendarTask restore(String s) {
    CalendarTask ct = new CalendarTask();
    ct.deserialize(s);
    return ct;
  }
  
  private TaskType type;
  private long id;
  
  private DateTime start;
  private DateTime end;

  private String summary;
  private String description;
  
  private String companyName;

  private TaskPriority priority;
  private TaskStatus status;
  
  private Long owner;
  private Long executor;
  
  private Collection<Long> observers;
  
  private String background;
  private String foreground;

  private Long style;

  public CalendarTask(TaskType type, long id, SimpleRow row) {
    this.type = type;
    this.id = id;

    this.start = row.getDateTime(CrmConstants.COL_START_TIME);
    this.end = row.getDateTime(CrmConstants.COL_FINISH_TIME);
    
    this.summary = row.getValue(CrmConstants.COL_SUMMARY);
    this.description = row.getValue(CrmConstants.COL_DESCRIPTION);
    
    this.companyName = row.getValue(CommonsConstants.ALS_COMPANY_NAME);
    
    this.priority = EnumUtils.getEnumByIndex(TaskPriority.class, 
        row.getInt(CrmConstants.COL_PRIORITY));
    this.status = EnumUtils.getEnumByIndex(TaskStatus.class, 
        row.getInt(CrmConstants.COL_STATUS));
    
    this.owner = row.getLong(CrmConstants.COL_OWNER);
    this.executor = row.getLong(CrmConstants.COL_EXECUTOR);
  }
  
  private CalendarTask() {
    super();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      String value = arr[i];
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (members[i]) {
        case TYPE:
          setType(Codec.unpack(TaskType.class, value));
          break;
        case ID:
          setId(BeeUtils.toLong(value));
          break;

        case START:
          setStart(DateTime.restore(value));
          break;
        case END:
          setEnd(DateTime.restore(value));
          break;

        case SUMMARY:
          setSummary(value);
          break;
        case DESCRIPTION:
          setDescription(value);
          break;
        
        case COMPANY_NAME:
          setCompanyName(value);
          break;
        
        case PRIORITY:
          setPriority(Codec.unpack(TaskPriority.class, value));
          break;
        case STATUS:
          setStatus(Codec.unpack(TaskStatus.class, value));
          break;

        case OWNER:
          setOwner(BeeUtils.toLongOrNull(value));
          break;
        case EXECUTOR:
          setExecutor(BeeUtils.toLongOrNull(value));
          break;
        
        case OBSERVERS:
          setObservers(Codec.deserializeIdList(value));
          break;
        
        case BACKGRUOND:
          setBackground(value);
          break;
        case FOREGROUND:
          setForeground(value);
          break;

        case STYLE:
          setStyle(BeeUtils.toLongOrNull(value));
          break;
      }
    }
  }

  public String getBackground() {
    return background;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getDescription() {
    return description;
  }

  public DateTime getEnd() {
    return end;
  }

  public Long getExecutor() {
    return executor;
  }

  public String getForeground() {
    return foreground;
  }

  public long getId() {
    return id;
  }

  public Collection<Long> getObservers() {
    return observers;
  }

  public Long getOwner() {
    return owner;
  }

  public TaskPriority getPriority() {
    return priority;
  }

  public DateTime getStart() {
    return start;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public Long getStyle() {
    return style;
  }

  public String getSummary() {
    return summary;
  }

  public TaskType getType() {
    return type;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case TYPE:
          arr[i++] = Codec.pack(getType());
          break;
        case ID:
          arr[i++] = getId();
          break;
          
        case START:
          arr[i++] = getStart();
          break;
        case END:
          arr[i++] = getEnd();
          break;

        case SUMMARY:
          arr[i++] = getSummary();
          break;
        case DESCRIPTION:
          arr[i++] = getDescription();
          break;

        case COMPANY_NAME:
          arr[i++] = getCompanyName();
          break;
        
        case PRIORITY:
          arr[i++] = Codec.pack(getPriority());
          break;
        case STATUS:
          arr[i++] = Codec.pack(getStatus());
          break;

        case OWNER:
          arr[i++] = getOwner();
          break;
        case EXECUTOR:
          arr[i++] = getExecutor();
          break;

        case OBSERVERS:
          arr[i++] = getObservers();
          break;

        case FOREGROUND:
          arr[i++] = getForeground();
          break;
        case BACKGRUOND:
          arr[i++] = getBackground();
          break;

        case STYLE:
          arr[i++] = getStyle();
          break;
      }
    }
    
    return Codec.beeSerialize(arr);
  }

  public void setBackground(String background) {
    this.background = background;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setEnd(DateTime end) {
    this.end = end;
  }

  public void setExecutor(Long executor) {
    this.executor = executor;
  }

  public void setForeground(String foreground) {
    this.foreground = foreground;
  }

  public void setObservers(Collection<Long> observers) {
    this.observers = observers;
  }

  public void setOwner(Long owner) {
    this.owner = owner;
  }

  public void setPriority(TaskPriority priority) {
    this.priority = priority;
  }

  public void setStart(DateTime start) {
    this.start = start;
  }

  public void setStatus(TaskStatus status) {
    this.status = status;
  }

  public void setStyle(Long style) {
    this.style = style;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  private void setId(long id) {
    this.id = id;
  }

  private void setType(TaskType type) {
    this.type = type;
  }
}
