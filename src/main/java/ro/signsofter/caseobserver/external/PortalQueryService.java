package ro.signsofter.caseobserver.external;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.soap.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.signsofter.caseobserver.exception.portal.PortalQueryException;
import ro.signsofter.caseobserver.external.dto.caseResponse.CaseDetailsDto;
import ro.signsofter.caseobserver.external.dto.caseResponse.FetchCaseEnvelope;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class PortalQueryService {

    @Autowired
    private PortalProperties portalProperties;

    public CaseDetailsDto fetchCaseDetails(String caseNumber, String institution) throws PortalQueryException {
        int attempts = Math.max(1, portalProperties.getRetries() + 1);
        Exception last = null;
        for (int i = 0; i < attempts; i++) {
            try {
                String soapResponse = sendSoapRequest(caseNumber, institution);
                JAXBContext context = JAXBContext.newInstance(FetchCaseEnvelope.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                FetchCaseEnvelope envelope = (FetchCaseEnvelope) unmarshaller.unmarshal(new StringReader(soapResponse));

                if (envelope == null || envelope.getBody() == null || envelope.getBody().getResponse() == null
                        || envelope.getBody().getResponse().getResult() == null
                        || envelope.getBody().getResponse().getResult().getCaseDetails() == null) {
                    throw new PortalQueryException("Portal returned an empty or malformed response");
                }

                return envelope.getBody().getResponse().getResult().getCaseDetails();
            } catch (Exception e) {
                last = e;
                // simple retry on transient errors
            }
        }
        throw new PortalQueryException("Error fetching case details: " + (last != null ? last.getMessage() : "unknown error"));
    }

    private String sendSoapRequest(String caseNumber, String institution) throws Exception {
        SOAPMessage soapMessage = createSoapRequest(caseNumber, institution);
        HttpURLConnection conn = createConnection();

        try (var outputStream = conn.getOutputStream()) {
            soapMessage.writeTo(outputStream);
        }

        return readResponse(conn);
    }

    private SOAPMessage createSoapRequest(String caseNumber, String institution) throws Exception {
        // Create SOAP Message
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // Create SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
        envelope.addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");

        // Create SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement bodyElement = soapBody.addChildElement("CautareDosare", "", "portalquery.just.ro");

        // Add Parameters
        bodyElement.addChildElement("numarDosar").addTextNode(caseNumber);
        bodyElement.addChildElement("obiectDosar").addTextNode("");
        bodyElement.addChildElement("numeParte").addTextNode("");
        bodyElement.addChildElement("institutie").addTextNode(institution);
//        bodyElement.addChildElement("dataStart").addTextNode("2022-01-01"); // Optional
//        bodyElement.addChildElement("dataStop").addTextNode("2024-01-01");  // Optional
//        bodyElement.addChildElement("dataUltimaModificareStart").addTextNode("2022-01-01");  // Optional
//        bodyElement.addChildElement("dataUltimaModificareStop").addTextNode("2024-01-01");  // Optional

        // Save and return SOAP message
        soapMessage.saveChanges();

        return soapMessage;
    }

    private HttpURLConnection createConnection() throws Exception {
        URL url = new URL(portalProperties.getBaseUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", portalProperties.getHost());
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction", portalProperties.getSoapAction());
        conn.setConnectTimeout(portalProperties.getConnectTimeoutMs());
        conn.setReadTimeout(portalProperties.getReadTimeoutMs());
        conn.setDoOutput(true);

        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        InputStream inputStream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        try (inputStream) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String payload = result.toString(StandardCharsets.UTF_8);
            if (code < 200 || code >= 300) {
                throw new PortalQueryException("Portal responded with status " + code + ": " + payload);
            }
            return payload;
        }
    }
}
