package com.stg.service3rd.common.dto.soap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JacksonXmlRootElement(localName = "soapenv:Envelope")
public class SoapRequest {

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:soapenv")
    private String env = "http://schemas.xmlsoap.org/soap/envelope/";
    
    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:urn")
    private String urn = "urn:mbageas:erp:fs-pm";

    @JacksonXmlProperty(localName = "soapenv:Header")
    private SoapHeader header;
    
    @JacksonXmlProperty(localName = "soapenv:Body")
    private SoapRequestBody body;
}
