package com.butent.bee.shared.data;

import com.butent.bee.shared.data.XmlExpression.XmlExpr;
import com.butent.bee.shared.data.XmlExpression.XmlPlus;
import com.butent.bee.shared.data.XmlExpression.XmlRound;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlValue;

@XmlSeeAlso({XmlExpr.class, XmlPlus.class, XmlRound.class})
public class XmlExpression {

  @XmlRootElement(name = "expr", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlExpr extends XmlExpression {
  }

  @XmlRootElement(name = "plus", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlPlus extends XmlExpression {
    @XmlElementRef
    public Collection<XmlExpression> expression;
  }

  @XmlRootElement(name = "round", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlRound extends XmlExpression {
    @XmlElementRef
    public XmlExpression expression;
    @XmlAttribute
    public int precision;
  }

  @XmlValue
  public String content;
}
