package lt.gama.helpers;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2016-10-07.
 */
public final class XMLUtils {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private XMLUtils() {}

    public static void error(HttpServletResponse response, String message) {
        response.setContentType("text/plain; charset=utf-8");
        try {
            response.getWriter().print(message);
        } catch (IOException e) {
            log.error("XMLUtils: " + e.getMessage(), e);
        }
    }

    public static void outputXML(HttpServletResponse response, Document xmlDoc, String filename) throws TransformerException, IOException {
        outputXML(response, xmlDoc, filename, false);
    }

    public static void outputXML(HttpServletResponse response, Document xmlDoc, String filename, boolean prettyOutput) throws TransformerException, IOException {
        // prepare response
        response.setContentType("text/xml; charset=utf-8");
        response.setHeader("Content-disposition", "attachment; filename=" + filename);

        // write the content
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        if (prettyOutput) transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(xmlDoc);
        transformer.transform(source, new StreamResult(response.getOutputStream()));
    }

    public static Document document() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    public static Document document(InputStream is, boolean namespaceAware) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(namespaceAware);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(is);
    }

    public static Document document(InputSource is, boolean namespaceAware) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(namespaceAware);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(is);
    }

    public static Document document(InputStream is) throws ParserConfigurationException, IOException, SAXException {
        return document(is, false);
    }

    public static Document document(InputSource is) throws ParserConfigurationException, IOException, SAXException {
        return document(is, false);
    }

    public static Element createElement(Document doc, Node parent, String ns, List<String> names, String value) {
        return createElement(doc, parent, ns, names, value, 0);
    }

    public static Element createElement(Document doc, Node parent, String ns, List<String> names, String value, int maxLen) {
        Validators.checkArgument(CollectionsHelper.hasValue(names), "No names");
        Element elementParent = null;
        for (String name : names) {
            Element elementChild = ns == null ? doc.createElement(name) : doc.createElementNS(ns, name);
            (elementParent == null ? parent : elementParent).appendChild(elementChild);
            elementParent = elementChild;
        }
        if (value != null) addElementValue(doc, elementParent, maxLen <= 0 ? value : value.substring(0, Math.min(maxLen, value.length())));
        return elementParent;
    }

    public static Element createElement(Document doc, Node parent, String ns, String name, String value) {
        return createElement(doc, parent, ns, Collections.singletonList(name), value);
    }

    public static Element createElement(Document doc, Node parent, String ns, String name, String value, int maxLen) {
        return createElement(doc, parent, ns, Collections.singletonList(name), value, maxLen);
    }

    public static Element createElement(Document doc, Node parent, String name, String value) {
        return createElement(doc, parent, null, Collections.singletonList(name), value);
    }

    public static Element createElement(Document doc, Node parent, String name, String value, int maxLen) {
        return createElement(doc, parent, null, Collections.singletonList(name), value, maxLen);
    }

    public static Element createElement(Document doc, Node parent, List<String> names, String value) {
        return createElement(doc, parent, null, names, value);
    }

    public static Element createElement(Document doc, Node parent, List<String> names, String value, int maxLen) {
        return createElement(doc, parent, null, names, value, maxLen);
    }

    public static Element createElement(Document doc, Node parent, String name) {
        return createElement(doc, parent, Collections.singletonList(name));
    }

    public static Element createElement(Document doc, Node parent, List<String> names) {
        return createElement(doc, parent, names, null);
    }

    public static Element createElementNS(Document doc, Node parent, String ns, String name) {
        return createElement(doc, parent, ns, name, null);
    }

    public static String deleteSpacesEmptyND(String value) {
        return value == null || StringHelper.deleteSpaces(value).length() == 0 ? "ND" : StringHelper.deleteSpaces(value);
    }

    public static String trimEmptyND(String value) {
        return StringHelper.isEmpty(value) ? "ND" : value.trim();
    }

    public static String getElementValue(Document doc, String name) {
        NodeList nodes = Validators.checkNotNull(doc.getElementsByTagNameNS("*", name), "XML error - no '" + name + "' element");
        Validators.checkArgument(nodes.getLength() == 1, "XML error - '{0}' appears not once but {1}", name, nodes.getLength());
        return nodes.item(0).getTextContent();
    }

    public static void addElementValue(Document doc, Element element, String value) {
        element.appendChild(doc.createTextNode(value));
    }
}
