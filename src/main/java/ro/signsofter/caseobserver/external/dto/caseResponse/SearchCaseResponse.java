package ro.signsofter.caseobserver.external.dto.caseResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class SearchCaseResponse {

    @XmlElement(name = "CautareDosareResult", namespace = "portalquery.just.ro")
    private CaseDetailsResult result;
}
