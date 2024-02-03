package dev.kinau.resourcepackvalidator.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

@Slf4j
public class ReportGenerator {

    private final List<TestCase> testCases;

    public ReportGenerator(List<TestCase> testCases, File reportFile)  {
        this.testCases = testCases;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element root = createRootNode(doc);
            for (TestCase testCase : testCases) {
                root.appendChild(createTestCaseElement(doc, testCase));
            }
            writeXml(doc, new FileOutputStream(reportFile));
        } catch (ParserConfigurationException | FileNotFoundException | TransformerException ex) {
            log.error("Failed to create report", ex);
        }
    }

    private Element createRootNode(Document doc) {
        Element rootElement = doc.createElement("testsuite");
        rootElement.setAttribute("tests", String.valueOf(testCases.size()));
        rootElement.setAttribute("failures", String.valueOf(testCases.stream().filter(testCase -> testCase.failure() != null).count()));
        rootElement.setAttribute("time", String.valueOf(testCases.stream().mapToLong(TestCase::time).sum() / 1000.0));
        doc.appendChild(rootElement);
        return rootElement;
    }

    private Element createTestCaseElement(Document doc, TestCase testCase) {
        Element element = doc.createElement("testcase");
        // Those are the attributes displayed in GitLab
        element.setAttribute("name", StringEscapeUtils.escapeXml11(testCase.name()));
        element.setAttribute("time", StringEscapeUtils.escapeXml11(String.valueOf(testCase.time() / 1000.0)));
        element.setAttribute("classname", "Resource Pack Validator");
        if (testCase.fails() > 0)
            element.setAttribute("file", "Failed " + testCase.fails() + " time" + (testCase.fails() > 1 ? "s" : ""));
        if (testCase.failure() != null) {
            element.appendChild(createFailureElement(doc, testCase.failure()));
        }
        return element;
    }

    private Element createFailureElement(Document doc, Failure failure) {
        Element element = doc.createElement("failure");
        if (failure.data() != null)
            element.setTextContent(StringEscapeUtils.escapeXml11(failure.data()));
        return element;
    }

    private static void writeXml(Document doc, OutputStream output) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);
    }
}
