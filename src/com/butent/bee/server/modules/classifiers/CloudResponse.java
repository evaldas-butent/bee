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

    @XmlElement(name = "pvmCode")
    public String VATCode;

    @XmlElement(name = "title")
    public String name;

    @XmlElement(name = "postCode")
    public String postIndex;

    @XmlElement(name = "city")
    public String cityName;

    @XmlElement(name = "street")
    public String address;
    public String houseNo;
    public String addressRest;

    public String phone;
    public String mobile;
    public String fax;

    public String website;
    public String email;

    public Long country;
    public String countryName;

    public Long city;
    public Long emailId;

    public Long companyType;
    public String typeName;
  }

  public String error;
  @XmlElementWrapper(name = "companies")
  @XmlElementRef
  public List<ResponseCompany> companies;
}
