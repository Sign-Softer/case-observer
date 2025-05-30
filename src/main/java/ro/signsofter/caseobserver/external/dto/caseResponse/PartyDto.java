package ro.signsofter.caseobserver.external.dto.caseResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PartyDto {
    @XmlElement(name = "nume", namespace = "portalquery.just.ro")
    private String name;

    @XmlElement(name = "calitateParte", namespace = "portalquery.just.ro")
    private String role;
}