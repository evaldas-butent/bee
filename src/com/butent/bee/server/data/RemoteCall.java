package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@WebService
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@Stateless
public class RemoteCall {

  public static class FieldType {
    @XmlAttribute(required = true)
    public String name;
    @XmlValue
    public String value;

    public FieldType() {
    }

    public FieldType(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  public static class RowType {
    @XmlElement(name = "col")
    public FieldType[] fields;

    public RowType() {
    }

    public RowType(FieldType[] fields) {
      this.fields = fields;
    }
  }

  public static class DataType {
    @XmlElement(name = "row")
    public RowType[] rows;
  }

  public static class DataAdapter extends XmlAdapter<DataType, List<Map<String, String>>> {
    @Override
    public DataType marshal(List<Map<String, String>> source) throws Exception {
      DataType data = new DataType();

      if (!BeeUtils.isEmpty(source)) {
        int rCnt = 0;
        RowType[] rows = new RowType[source.size()];

        for (Map<String, String> row : source) {
          FieldType[] fields = new FieldType[row.size()];
          int fCnt = 0;

          for (String name : row.keySet()) {
            fields[fCnt++] = new FieldType(name, row.get(name));
          }
          rows[rCnt++] = new RowType(fields);
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
          HashMap<String, String> row = Maps.newHashMap();

          for (FieldType field : r.fields) {
            row.put(field.name, field.value);
          }
          rows.add(row);
        }
      }
      return rows;
    }
  }

  @XmlRootElement(name = "data")
  public static class DataHolder {
    @XmlAttribute
    public String error;
    @XmlAttribute
    public Integer affected;
    @XmlAttribute
    public Set<String> columns;
    @XmlJavaTypeAdapter(DataAdapter.class)
    public List<Map<String, String>> rows;
  }

  @XmlRootElement(name = "params")
  public static class ParamHolder {
    @XmlAttribute(required = true)
    public String view;
    @XmlAttribute
    public String[] fields;
    @XmlElement
    public String filter;
    @XmlAttribute
    public String[] orderBy;
    @XmlAttribute
    public Integer limit;
    @XmlAttribute
    public Integer offset;
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

      if (!sys.isView(prm.view) && !sys.isTable(prm.view)) {
        rs = "Not a view: " + prm.view;
      } else {
        Filter filter = null;

        if (!BeeUtils.isEmpty(prm.filter)) {
          List<IsColumn> columns = Lists.newArrayList();
          BeeView view = sys.getView(prm.view);

          for (String col : view.getColumns()) {
            columns.add(new BeeColumn(ValueType.getByTypeCode(view.getType(col).toString()), col));
          }
          filter = DataUtils.parseCondition(prm.filter, columns);
        }
        Order order = null;

        if (!BeeUtils.isEmpty(prm.orderBy)) {
          char prfx = '-';
          order = new Order();

          for (String ord : prm.orderBy) {
            order.add(BeeUtils.removePrefix(ord, prfx), !BeeUtils.isPrefix(ord, prfx));
          }
        }
        rs = sys.getViewData(prm.view, sys.getViewCondition(prm.view, filter), order,
            BeeUtils.toNonNegativeInt(prm.limit), BeeUtils.toNonNegativeInt(prm.offset));
      }
    }
    DataHolder data = new DataHolder();

    if (rs instanceof BeeRowSet) {
      BeeRowSet rowSet = (BeeRowSet) rs;
      data.affected = rowSet.getNumberOfRows();
      data.rows = Lists.newArrayList();
      data.columns = Sets.newLinkedHashSet();

      for (int i = 0; i < rowSet.getNumberOfColumns(); i++) {
        String col = rowSet.getColumnId(i);

        if (!BeeUtils.isEmpty(prm.fields) && !ArrayUtils.contains(col, prm.fields)) {
          continue;
        }
        data.columns.add(col);
      }
      if (!BeeUtils.isEmpty(data.columns)) {
        boolean idMode = true;
        boolean versionMode = true;

        if (!BeeUtils.isEmpty(prm.fields)) {
          idMode = ArrayUtils.contains(BeeTable.DEFAULT_ID_FIELD, prm.fields);
          versionMode = ArrayUtils.contains(BeeTable.DEFAULT_VERSION_FIELD, prm.fields);
        }
        for (BeeRow r : rowSet.getRows()) {
          HashMap<String, String> row = Maps.newLinkedHashMap();

          for (String col : data.columns) {
            row.put(rowSet.getColumn(col).getId(), rowSet.getString(r, col));
          }
          if (idMode) {
            row.put(BeeTable.DEFAULT_ID_FIELD, BeeUtils.transform(r.getId()));
          }
          if (versionMode) {
            row.put(BeeTable.DEFAULT_VERSION_FIELD, BeeUtils.transform(r.getVersion()));
          }
          data.rows.add(row);
        }
        if (idMode) {
          data.columns.add(BeeTable.DEFAULT_ID_FIELD);
        }
        if (versionMode) {
          data.columns.add(BeeTable.DEFAULT_VERSION_FIELD);
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
      JAXBContext c = JAXBContext.newInstance(clazz);
      ret = c.createUnmarshaller().unmarshal(new StringReader(params));

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
