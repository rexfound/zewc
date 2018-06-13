package com.datadelivery.WorldCupPool2018.object;

import java.util.List;

/**
 * Created by handy.kestury on 6/12/2018.
 */
public class GraphResponseBody {

  List<GraphColumn> cols;

  List<GraphRow> rows;

  public List<GraphColumn> getCols() {
    return cols;
  }

  public void setCols(final List<GraphColumn> cols) {
    this.cols = cols;
  }

  public List<GraphRow> getRows() {
    return rows;
  }

  public void setRows(final List<GraphRow> rows) {
    this.rows = rows;
  }
}
