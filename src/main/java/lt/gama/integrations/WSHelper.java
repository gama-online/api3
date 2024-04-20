package lt.gama.integrations;

import lt.gama.service.ex.rt.GamaException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WSHelper {
    static public XMLGregorianCalendar xmlFromLocalDate(LocalDate localDate) {
        if (localDate == null) return null;
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                    localDate.getYear(),
                    localDate.getMonthValue(),
                    localDate.getDayOfMonth(),
                    DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED);
        } catch (DatatypeConfigurationException e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    static public XMLGregorianCalendar xmlFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        try {
            XMLGregorianCalendar xmlGregorianCalendar =
                    DatatypeFactory.newInstance().newXMLGregorianCalendar(
                            localDateTime.getYear(),
                            localDateTime.getMonthValue(),
                            localDateTime.getDayOfMonth(),
                            localDateTime.getHour(),
                            localDateTime.getMinute(),
                            localDateTime.getSecond(),
                            DatatypeConstants.FIELD_UNDEFINED,
                            DatatypeConstants.FIELD_UNDEFINED
                    );
            xmlGregorianCalendar.setFractionalSecond(null);
            return xmlGregorianCalendar;
        } catch (DatatypeConfigurationException e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    static public LocalDateTime localDateTimeFromXML(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) return null;
        try {
            return LocalDateTime.of(
                    xmlGregorianCalendar.getYear(),
                    xmlGregorianCalendar.getMonth(),
                    xmlGregorianCalendar.getDay(),
                    xmlGregorianCalendar.getHour(),
                    xmlGregorianCalendar.getMinute(),
                    xmlGregorianCalendar.getSecond());
        } catch (DateTimeException e) {
            throw new GamaException(e.getMessage(), e);
        }
    }
}
