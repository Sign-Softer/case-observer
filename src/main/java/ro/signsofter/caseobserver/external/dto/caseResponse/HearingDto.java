package ro.signsofter.caseobserver.external.dto.caseResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class HearingDto {

    @XmlElement(name = "complet", namespace = "portalquery.just.ro")
    private String judicialPanel;

    @XmlElement(name = "data", namespace = "portalquery.just.ro")
    private String date;

    @XmlElement(name = "ora", namespace = "portalquery.just.ro")
    private String time;

    @XmlElement(name = "solutie", namespace = "portalquery.just.ro")
    private String solution;

    @XmlElement(name = "solutieSumar", namespace = "portalquery.just.ro")
    private String summary;

    @XmlElement(name = "dataPronuntare", namespace = "portalquery.just.ro")
    private String pronouncementDate;
}