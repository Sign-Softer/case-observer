package ro.signsofter.caseobserver.external.dto.caseResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

//@XmlRootElement(name = "CautareDosareResult", namespace = "portalquery.just.ro")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class CaseDetailsResult {
    @XmlElement(name = "Dosar", namespace = "portalquery.just.ro")
    private CaseDetailsDto caseDetails;
}
