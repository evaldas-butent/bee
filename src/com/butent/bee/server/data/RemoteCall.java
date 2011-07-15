package com.butent.bee.server.data;

import com.google.common.collect.Lists;

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
import java.util.List;

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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

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
  @XmlRootElement(name = "col")
  public static class DataColumn {
    @XmlAttribute
    public String name;
    @XmlValue
    public String value;
  }

  /**
   * Contains a list of columns for a row.
   */
  @XmlRootElement(name = "row")
  public static class DataRow {
    @XmlAttribute
    public String id;
    @XmlAttribute
    public String version;
    @XmlElementRef
    public List<DataColumn> columns;

    public void addColumn(String name, String value) {
      if (columns == null) {
        columns = Lists.newArrayList();
      }
      DataColumn col = new DataColumn();
      col.name = name;
      col.value = value;
      columns.add(col);
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
    @XmlElementRef
    @XmlElementWrapper
    public List<DataRow> rows;
  }

  /**
   * Holds such parameters for SOAP web service requests as view, columns, filter or orderBy.
   */
  @XmlRootElement(name = "params")
  public static class ParamHolder {
    @XmlAttribute
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
    public boolean showId;
    @XmlAttribute
    public boolean showVersion;
    @XmlAttribute
    public boolean skipColumns;
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

      } else {
        rs = null;
        Filter filter = null;
        Order order = null;
        BeeView view = sys.getView(prm.view);

        if (!BeeUtils.isEmpty(prm.columns)) {
          for (String col : prm.columns) {
            if (!view.hasColumn(col)) {
              rs = "Unrecognized view column: " + col;
              break;
            }
          }
        }
        if (rs == null && !BeeUtils.isEmpty(prm.filter)) {
          List<IsColumn> columns = Lists.newArrayList();

          for (String col : view.getColumns()) {
            columns.add(new BeeColumn(ValueType.getByTypeCode(view.getType(col).toString()), col));
          }
          filter = DataUtils.parseCondition(prm.filter, columns);

          if (filter == null) {
            rs = "Wrong filter: " + prm.filter;
          }
        }
        if (rs == null && !BeeUtils.isEmpty(prm.orderBy)) {
          char prfx = '-';
          order = new Order();

          for (String ord : prm.orderBy) {
            String col = BeeUtils.removePrefix(ord, prfx);

            if (!view.hasColumn(col)) {
              rs = "Unrecognized order column: " + col;
              break;
            }
            order.add(ord, col, !BeeUtils.isPrefix(ord, prfx));
          }
        }
        if (rs == null) {
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
      data.columns = Lists.newArrayList(rowSet.getColumnLabels());
      data.rows = Lists.newArrayList();

      if (prm.showId || prm.showVersion || !prm.skipColumns) {
        for (BeeRow r : rowSet.getRows()) {
          DataRow row = new DataRow();

          if (prm.showId) {
            row.id = BeeUtils.transform(r.getId());
          }
          if (prm.showVersion) {
            row.version = BeeUtils.transform(new DateTime(r.getVersion()));
          }
          if (!prm.skipColumns) {
            for (String col : data.columns) {
              row.addColumn(rowSet.getColumn(col).getId(), rowSet.getString(r, col));
            }
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
    } catch (Exception e) {
      ret = e.getMessage();
    }
    return ret;
  }
}
