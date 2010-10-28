package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class HasFrom<T> extends SqlQuery<T> {

  private List<IsFrom> fromList;

  public T addFrom(String source) {
    addFrom(source, null);
    return getReference();
  }

  public T addFrom(String source, String alias) {
    if (BeeUtils.isEmpty(fromList)) {
      addFrom(new FromSingle(source, alias));
    } else {
      addFromList(new FromList(source, alias));
    }
    return getReference();
  }

  public T addFrom(SqlSelect source, String alias) {
    if (BeeUtils.isEmpty(fromList)) {
      addFrom(new FromSingle(source, alias));
    } else {
      addFromList(new FromList(source, alias));
    }
    return getReference();
  }

  public T addFromFull(String source, IsCondition on) {
    addFromFull(source, null, on);
    return getReference();
  }

  public T addFromFull(String source, String alias, IsCondition on) {
    addFromJoin(new FromFull(source, alias, on));
    return getReference();
  }

  public T addFromFull(SqlSelect source, String alias, IsCondition on) {
    addFromJoin(new FromFull(source, alias, on));
    return getReference();
  }

  public T addFromInner(String source, IsCondition on) {
    addFromInner(source, null, on);
    return getReference();
  }

  public T addFromInner(String source, String alias, IsCondition on) {
    addFromJoin(new FromInner(source, alias, on));
    return getReference();
  }

  public T addFromInner(SqlSelect source, String alias, IsCondition on) {
    addFromJoin(new FromInner(source, alias, on));
    return getReference();
  }

  public T addFromLeft(String source, IsCondition on) {
    addFromLeft(source, null, on);
    return getReference();
  }

  public T addFromLeft(String source, String alias, IsCondition on) {
    addFromJoin(new FromLeft(source, alias, on));
    return getReference();
  }

  public T addFromLeft(SqlSelect source, String alias, IsCondition on) {
    addFromJoin(new FromLeft(source, alias, on));
    return getReference();
  }

  public T addFromRight(String source, IsCondition on) {
    addFromRight(source, null, on);
    return getReference();
  }

  public T addFromRight(String source, String alias, IsCondition on) {
    addFromJoin(new FromRight(source, alias, on));
    return getReference();
  }

  public T addFromRight(SqlSelect source, String alias, IsCondition on) {
    addFromJoin(new FromRight(source, alias, on));
    return getReference();
  }

  public List<IsFrom> getFrom() {
    return fromList;
  }

  private void addFrom(IsFrom from) {
    if (BeeUtils.isEmpty(fromList)) {
      fromList = new ArrayList<IsFrom>();
    }
    fromList.add(from);
  }

  private void addFromJoin(FromJoin from) {
    Assert.notEmpty(fromList, "First FROM source cannot be of type JOIN");

    for (IsFrom f : fromList) {
      Assert.state(!(f instanceof FromList),
          "Cannot mix FROM sources with types JOIN and LIST");
    }
    addFrom(from);
  }

  private void addFromList(FromList from) {
    for (IsFrom f : fromList) {
      Assert.state(!(f instanceof FromJoin),
          "Cannot mix FROM sources with types JOIN and LIST");
    }
    addFrom(from);
  }
}
