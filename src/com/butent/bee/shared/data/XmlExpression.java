// CHECKSTYLE:OFF
package com.butent.bee.shared.data;

import com.butent.bee.shared.data.XmlExpression.XmlBoolean;
import com.butent.bee.shared.data.XmlExpression.XmlBulk;
import com.butent.bee.shared.data.XmlExpression.XmlCase;
import com.butent.bee.shared.data.XmlExpression.XmlCast;
import com.butent.bee.shared.data.XmlExpression.XmlConcat;
import com.butent.bee.shared.data.XmlExpression.XmlDate;
import com.butent.bee.shared.data.XmlExpression.XmlDatetime;
import com.butent.bee.shared.data.XmlExpression.XmlDivide;
import com.butent.bee.shared.data.XmlExpression.XmlMinus;
import com.butent.bee.shared.data.XmlExpression.XmlMultiply;
import com.butent.bee.shared.data.XmlExpression.XmlName;
import com.butent.bee.shared.data.XmlExpression.XmlNumber;
import com.butent.bee.shared.data.XmlExpression.XmlNvl;
import com.butent.bee.shared.data.XmlExpression.XmlPlus;
import com.butent.bee.shared.data.XmlExpression.XmlString;
import com.butent.bee.shared.data.XmlExpression.XmlSwitch;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlValue;

@XmlSeeAlso({
    XmlNumber.class, XmlString.class, XmlBoolean.class, XmlDate.class, XmlDatetime.class,
    XmlPlus.class, XmlMinus.class, XmlMultiply.class, XmlDivide.class, XmlBulk.class,
    XmlCast.class, XmlSwitch.class, XmlCase.class, XmlNvl.class, XmlConcat.class, XmlName.class })
public abstract class XmlExpression {

  public static class XmlHasMember extends XmlExpression {
    @XmlElementRef
    public XmlExpression member;
  }

  public static class XmlHasMembers extends XmlExpression {
    @XmlElementRef
    public List<XmlExpression> members;
  }

  @XmlRootElement(name = "name", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlName extends XmlExpression {
  }

  @XmlRootElement(name = "number", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlNumber extends XmlExpression {
  }

  @XmlRootElement(name = "string", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlString extends XmlExpression {
  }

  @XmlRootElement(name = "boolean", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlBoolean extends XmlExpression {
  }

  @XmlRootElement(name = "date", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlDate extends XmlExpression {
  }

  @XmlRootElement(name = "datetime", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlDatetime extends XmlExpression {
  }

  @XmlRootElement(name = "plus", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlPlus extends XmlHasMembers {
  }

  @XmlRootElement(name = "minus", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlMinus extends XmlHasMembers {
  }

  @XmlRootElement(name = "multiply", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlMultiply extends XmlHasMembers {
  }

  @XmlRootElement(name = "divide", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlDivide extends XmlHasMembers {
  }

  @XmlRootElement(name = "bulk", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlBulk extends XmlHasMembers {
  }

  @XmlRootElement(name = "cast", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlCast extends XmlHasMember {
    @XmlAttribute
    public int precision;
    @XmlAttribute
    public int scale;
  }

  @XmlRootElement(name = "switch", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlSwitch extends XmlHasMember {
    @XmlElementRef
    public List<XmlCase> cases;
    @XmlElement(name = "else", namespace = DataUtils.EXPRESSION_NAMESPACE)
    public XmlHasMember elseExpression;
  }

  @XmlRootElement(name = "case", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlCase {
    @XmlElementRef
    public XmlExpression whenExpression;
    @XmlElement(name = "then", namespace = DataUtils.EXPRESSION_NAMESPACE)
    public XmlHasMember thenExpression;
  }

  @XmlRootElement(name = "nvl", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlNvl extends XmlHasMembers {
  }

  @XmlRootElement(name = "concat", namespace = DataUtils.EXPRESSION_NAMESPACE)
  public static class XmlConcat extends XmlHasMembers {
  }

  @XmlValue
  public String content;
  @XmlAttribute
  public String type;
}
