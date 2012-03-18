package com.butent.bee.shared.data;

import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.shared.data.XmlExpression.XmlBoolean;
import com.butent.bee.shared.data.XmlExpression.XmlCase;
import com.butent.bee.shared.data.XmlExpression.XmlCast;
import com.butent.bee.shared.data.XmlExpression.XmlDate;
import com.butent.bee.shared.data.XmlExpression.XmlDatetime;
import com.butent.bee.shared.data.XmlExpression.XmlDivide;
import com.butent.bee.shared.data.XmlExpression.XmlMinus;
import com.butent.bee.shared.data.XmlExpression.XmlMultiply;
import com.butent.bee.shared.data.XmlExpression.XmlNumber;
import com.butent.bee.shared.data.XmlExpression.XmlPlus;
import com.butent.bee.shared.data.XmlExpression.XmlRound;
import com.butent.bee.shared.data.XmlExpression.XmlString;
import com.butent.bee.shared.data.XmlExpression.XmlSwitch;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlValue;

@XmlSeeAlso({
    XmlNumber.class, XmlString.class, XmlBoolean.class, XmlDate.class, XmlDatetime.class,
    XmlPlus.class, XmlMinus.class, XmlMultiply.class, XmlDivide.class, XmlRound.class,
    XmlCast.class, XmlSwitch.class, XmlCase.class})
public abstract class XmlExpression {

  public abstract static class XmlHasMember extends XmlExpression {
    @XmlElementRef
    public XmlExpression member;
  }

  public abstract static class XmlHasMembers extends XmlExpression {
    @XmlElementRef
    public List<XmlExpression> members;
  }

  @XmlRootElement(name = "number", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlNumber extends XmlExpression {
  }

  @XmlRootElement(name = "string", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlString extends XmlExpression {
  }

  @XmlRootElement(name = "boolean", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlBoolean extends XmlExpression {
  }

  @XmlRootElement(name = "date", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlDate extends XmlExpression {
  }

  @XmlRootElement(name = "datetime", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlDatetime extends XmlExpression {
  }

  @XmlRootElement(name = "plus", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlPlus extends XmlHasMembers {
  }

  @XmlRootElement(name = "minus", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlMinus extends XmlHasMembers {
  }

  @XmlRootElement(name = "multiply", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlMultiply extends XmlHasMembers {
  }

  @XmlRootElement(name = "divide", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlDivide extends XmlHasMembers {
  }

  @XmlRootElement(name = "round", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlRound extends XmlHasMember {
    @XmlAttribute
    public int precision;
  }

  @XmlRootElement(name = "cast", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlCast extends XmlHasMember {
    @XmlAttribute
    public int precision;
    @XmlAttribute
    public int scale;
  }

  @XmlRootElement(name = "switch", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlSwitch extends XmlHasMember {
    @XmlElementRef
    public List<XmlCase> cases;
    @XmlElementWrapper(name = "else", namespace = DataUtils.DEFAULT_NAMESPACE)
    @XmlElementRef
    public List<XmlExpression> elseExpression;
  }

  @XmlRootElement(name = "case", namespace = DataUtils.DEFAULT_NAMESPACE)
  public static class XmlCase {
    @XmlElementRef
    public List<XmlExpression> members;
  }

  @XmlValue
  public String content;
  @XmlAttribute
  public SqlDataType type;
}
