package com.butent.bee.server.modules.classifiers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data") class CloudResponse {

  @XmlRootElement(name = "company")
  static class ResponseCompany {
    public String code;
    @XmlElement(name = "title")
    public String name;
    public String address;
    public String phone;
    public String mobile;
    public String fax;
    public String website;
    public String email;
    public Long emailId;
    @XmlElement(name = "pvmCode")
    public String VATCode;
  }

  public String error;
  @XmlElementWrapper(name = "companies")
  @XmlElementRef
  public List<ResponseCompany> companies;
}
