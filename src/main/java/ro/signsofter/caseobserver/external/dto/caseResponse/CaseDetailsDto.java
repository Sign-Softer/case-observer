package ro.signsofter.caseobserver.external.dto.caseResponse;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import lombok.Data;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class CaseDetailsDto {

    @XmlElement(name = "numar", namespace = "portalquery.just.ro")
    private String number;

    @XmlElement(name = "institutie", namespace = "portalquery.just.ro")
    private String institution;

    @XmlElement(name = "departament", namespace = "portalquery.just.ro")
    private String department;

    @XmlElement(name = "categorieCaz", namespace = "portalquery.just.ro")
    private String caseCategory;

    @XmlElement(name = "categorieCazNume", namespace = "portalquery.just.ro")
    private String caseCategoryName;

    @XmlElement(name = "stadiuProcesual", namespace = "portalquery.just.ro")
    private String proceduralStage;

    @XmlElement(name = "stadiuProcesualNume", namespace = "portalquery.just.ro")
    private String proceduralStageName;

    @XmlElement(name = "obiect", namespace = "portalquery.just.ro")
    private String subject;

    // TODO use XMLJavaTypeAdapter
    @XmlElement(name = "dataModificare", namespace = "portalquery.just.ro")
    private String modificationDateTime;

    @XmlElementWrapper(name = "parti", namespace = "portalquery.just.ro")
    @XmlElement(name = "DosarParte", namespace = "portalquery.just.ro")
    private List<PartyDto> parties;

    @XmlElementWrapper(name = "sedinte", namespace = "portalquery.just.ro")
    @XmlElement(name = "DosarSedinta", namespace = "portalquery.just.ro")
    private List<HearingDto> hearings;

}