package com.datadelivery.WorldCupPool2018.object;

/**
 * Created by handy.kestury on 6/12/2018.
 */
public class GraphColumn {

  String id;

  String label;

  String type;

  public GraphColumn(String id, String label) {
    this.id = id;
    this.label = label;
    this.type = "number";
  }

  public GraphColumn(String label) {
    this.id = "";
    this.label = label;
    this.type = "number";
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }
}
