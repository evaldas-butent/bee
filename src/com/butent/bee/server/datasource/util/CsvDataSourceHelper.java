package com.butent.bee.server.datasource.util;

import com.google.common.collect.Lists;

import com.butent.bee.shared.data.DataException;
import com.butent.bee.shared.data.DataTable;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.Reasons;
import com.butent.bee.shared.data.TableColumn;
import com.butent.bee.shared.data.TableRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

import com.ibm.icu.util.ULocale;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

public class CsvDataSourceHelper {

  public static Reader getCsvFileReader(String file) throws DataException {
    Reader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      throw new DataException(Reasons.INVALID_REQUEST,
          "Couldn't read csv file from: " + file);
    }
    return reader;
  }

  public static Reader getCsvUrlReader(String url) throws DataException {
    Reader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
    } catch (MalformedURLException e) {
      throw new DataException(Reasons.INVALID_REQUEST, "url is malformed: " + url);
    } catch (IOException e) {
      throw new DataException(Reasons.INVALID_REQUEST,
          "Couldn't read csv file from url: " + url);
    }
    return reader;
  }

  public static IsTable read(Reader reader, List<IsColumn> columns,
      Boolean headerRow) throws IOException, CsvDataSourceException {
    return read(reader, columns, headerRow, null);
  }

  public static IsTable read(Reader reader, List<IsColumn> columns,
      Boolean headerRow, ULocale locale) throws IOException, CsvDataSourceException {
    IsTable dataTable = new DataTable();

    if (reader == null) {
      return dataTable;
    }

    CSVReader csvReader = new CSVReader(reader);
    Map<ValueType, ValueFormatter> defaultFormatters =
        ValueFormatter.createDefaultFormatters(locale);

    String[] line;
    boolean firstLine = true;
    while ((line = csvReader.readNext()) != null) {
      if ((line.length == 1) && (line[0].equals(""))) {
        continue;
      }

      if ((columns != null) && (line.length != columns.size())) {
        throw new CsvDataSourceException(Reasons.INTERNAL_ERROR,
            "Wrong number of columns in the data.");
      }
      if (firstLine) {
        if (columns == null) {
          columns = Lists.newArrayList();
        }

        List<IsColumn> tempColumns = new ArrayList<IsColumn>();

        for (int i = 0; i < line.length; i++) {
          IsColumn tempCol = (columns.isEmpty() || columns.get(i) == null)
              ? null : columns.get(i);

          String id = ((tempCol == null) || (tempCol.getId() == null))
              ? "Col" + (i) : tempCol.getId();
          ValueType type = ((tempCol == null) || (tempCol.getType() == null))
              ? ValueType.TEXT : tempCol.getType();
          String label = ((tempCol == null) || (tempCol.getLabel() == null))
              ? "Column" + i : tempCol.getLabel();
          String pattern = ((tempCol == null) || (tempCol.getPattern() == null))
              ? "" : tempCol.getPattern();

          tempCol = new TableColumn(id, type, label);
          tempCol.setPattern(pattern);
          tempColumns.add(tempCol);
        }

        if (headerRow) {
          for (int i = 0; i < line.length; i++) {
            String string = line[i];
            if (string == null) {
              tempColumns.get(i).setLabel("");
            } else {
              tempColumns.get(i).setLabel(line[i].trim());
            }
          }
        }

        columns = tempColumns;
        dataTable = new DataTable();
        dataTable.addColumns(columns);
      }
      if (!(firstLine && headerRow)) {
        IsRow row = new TableRow();
        for (int i = 0; i < line.length; i++) {
          IsColumn col = columns.get(i);
          ValueType valueType = col.getType();
          String string = line[i];
          if (string != null) {
            string = string.trim();
          }
          String pattern = col.getPattern();
          ValueFormatter valueFormatter;
          if (pattern == null || pattern.equals("")) {
            valueFormatter = defaultFormatters.get(valueType);
          } else {
            valueFormatter = ValueFormatter.createFromPattern(valueType, pattern, locale);
          }
          Value value = valueFormatter.parse(string);
          
          row.addCell(value);
        }
        dataTable.addRow(row);
      }

      firstLine = false;
    }

    return dataTable;
  }

  private CsvDataSourceHelper() {
  }
}
