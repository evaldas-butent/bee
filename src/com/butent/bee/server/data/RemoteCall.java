package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Enables to get data from the system using SOAP web service protocol.
 */
@WebService
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@Stateless
public class RemoteCall {

  /**
   * Contains column information - name and value.
   */
  public static class ColType {
    @XmlAttribute(required = true)
    public String name;
    @XmlValue
    public String value;

    public ColType() {
    }

    public ColType(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  /**
   * Contains a list of columns for a row.
   */
  public static class RowType {
    @XmlElement(name = "col")
    public ColType[] columns;

    public RowType() {
    }

    public RowType(ColType[] columns) {
      this.columns = columns;
    }
  }

  /**
   * Contains a list of rows.
   */
  public static class DataType {
    @XmlElement(name = "row")
    public RowType[] rows;
  }

  /**
   * Creates data for SOAP web service requests.
   */
  public static class DataAdapter extends XmlAdapter<DataType, List<Map<String, String>>> {
    @Override
    public DataType marshal(List<Map<String, String>> source) throws Exception {
      DataType data = new DataType();

      if (!BeeUtils.isEmpty(source)) {
        int rCnt = 0;
        RowType[] rows = new RowType[source.size()];

        for (Map<String, String> row : source) {
          ColType[] columns = new ColType[row.size()];
          int cCnt = 0;

          for (String name : row.keySet()) {
            columns[cCnt++] = new ColType(name, row.get(name));
          }
          rows[rCnt++] = new RowType(columns);
        }
        data.rows = rows;
      }
      return data;
    }

    @Override
    public List<Map<String, String>> unmarshal(DataType data) throws Exception {
      List<Map<String, String>> rows = Lists.newArrayList();

      if (!BeeUtils.isEmpty(data)) {
        for (RowType r : data.rows) {
          HashMap<String, String> row = Maps.newLinkedHashMap();

          for (ColType column : r.columns) {
            row.put(column.name, column.value);
          }
          rows.add(row);
        }
      }
      return rows;
    }
  }

  /**
   * Holds SOAP web service response data.
   */
  @XmlRootElement(name = "data")
  public static class DataHolder {
    @XmlAttribute
    public String error;
    @XmlAttribute
    public Integer affected;
    @XmlAttribute
    public List<String> columns;
    @XmlJavaTypeAdapter(DataAdapter.class)
    public List<Map<String, String>> rows;
  }

  /**
   * Holds such parameters for SOAP web service requests as view, columns, filter or orderBy.
   */
  @XmlRootElement(name = "params")
  public static class ParamHolder {
    @XmlAttribute(required = true)
    public String view;
    @XmlAttribute
    public String[] columns;
    @XmlElement
    public String filter;
    @XmlAttribute
    public String[] orderBy;
    @XmlAttribute
    public Integer limit;
    @XmlAttribute
    public Integer offset;
    @XmlAttribute
    public String idName;
    @XmlAttribute
    public String versionName;
  }

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  public String getViewData(String params) {
    Object rs = buildRequest(ParamHolder.class, params);
    ParamHolder prm = null;

    if (rs instanceof ParamHolder) {
      prm = (ParamHolder) rs;

      if (!sys.isView(prm.view)) {
        rs = "Not a view: " + prm.view;

      } else if (!BeeUtils.isEmpty(prm.idName) && sys.getView(prm.view).hasColumn(prm.idName)) {
        rs = "Id column name conflict: " + prm.idName;

      } else if (!BeeUtils.isEmpty(prm.versionName)
          && (sys.getView(prm.view).hasColumn(prm.versionName)
              || BeeUtils.same(prm.idName, prm.versionName))) {
        rs = "Version column name conflict: " + prm.versionName;

      } else {
        rs = null;
        Filter filter = null;

        if (!BeeUtils.isEmpty(prm.filter)) {
          List<IsColumn> columns = Lists.newArrayList();
          BeeView view = sys.getView(prm.view);

          for (String col : view.getColumns()) {
            columns.add(new BeeColumn(ValueType.getByTypeCode(view.getType(col).toString()), col));
          }
          filter = DataUtils.parseCondition(prm.filter, columns);

          if (filter == null) {
            rs = "Wrong filter: " + prm.filter;
          }
        }
        if (rs == null) {
          Order order = null;

          if (!BeeUtils.isEmpty(prm.orderBy)) {
            char prfx = '-';
            order = new Order();

            for (String ord : prm.orderBy) {
              order.add(ord, BeeUtils.removePrefix(ord, prfx), !BeeUtils.isPrefix(ord, prfx));
            }
          }
          rs = sys.getViewData(prm.view, sys.getViewCondition(prm.view, filter), order,
              BeeUtils.toNonNegativeInt(prm.limit), BeeUtils.toNonNegativeInt(prm.offset),
              prm.columns);
        }
      }
    }
    DataHolder data = new DataHolder();

    if (rs instanceof BeeRowSet) {
      BeeRowSet rowSet = (BeeRowSet) rs;
      data.affected = rowSet.getNumberOfRows();
      data.rows = Lists.newArrayList();
      data.columns = Lists.newArrayList(rowSet.getColumnLabels());

      if (!BeeUtils.isEmpty(data.columns)) {
        for (BeeRow r : rowSet.getRows()) {
          HashMap<String, String> row = Maps.newLinkedHashMap();

          for (String col : data.columns) {
            row.put(rowSet.getColumn(col).getId(), rowSet.getString(r, col));
          }
          if (!BeeUtils.isEmpty(prm.idName)) {
            row.put(prm.idName, BeeUtils.transform(r.getId()));
          }
          if (!BeeUtils.isEmpty(prm.versionName)) {
            row.put(prm.versionName, BeeUtils.transform(new DateTime(r.getVersion())));
          }
          data.rows.add(row);
        }
      }
    } else {
      data.error = BeeUtils.transform(rs);
    }
    return buildResponse(data);
  }

  private <T> Object buildRequest(Class<T> clazz, String params) {
    Object ret = null;

    try {
      Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
      ret = unmarshaller.unmarshal(new StringReader(params));

    } catch (JAXBException e) {
      String err;

      if (!BeeUtils.isEmpty(e.getLinkedException())) {
        err = e.getLinkedException().getMessage();
      } else {
        err = e.getMessage();
      }
      ret = err;
    } catch (Exception e) {
      ret = e.getMessage();
    }
    return ret;
  }

  private <T> String buildResponse(T data) {
    String ret;

    try {
      JAXBContext c = JAXBContext.newInstance(data.getClass());
      StringWriter writer = new StringWriter();
      Marshaller xx = c.createMarshaller();
      xx.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      xx.marshal(data, writer);
      ret = writer.toString();

    } catch (JAXBException e) {
      String err;

      if (!BeeUtils.isEmpty(e.getLinkedException())) {
        err = e.getLinkedException().getMessage();
      } else {
        err = e.getMessage();
      }
      ret = err;
    }
    return ret;
  }
}
