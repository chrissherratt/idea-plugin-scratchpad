package org.intellij.scratchpad;

import org.jdom.Element;
import org.jdom.CDATA;
import org.jdom.Namespace;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XMLHelper {

    private static final String CONTENTS = "Contents";
    private static final String NAME = "Name";
    private static final String SCRATCH_PAD = "ScratchPad";

    public static Map<String, String> readDocuments(Element element) {
        Map<String, String> docs = new LinkedHashMap<String, String>();
        List docElements = element.getChildren(SCRATCH_PAD);
        if (docElements == null) return docs;
        for (int i = 0; i < docElements.size(); i++) {
            Element docElement = (Element) docElements.get(i);
            Element docElementName = docElement.getChild(NAME);
            Element docElementContents = docElement.getChild(CONTENTS);
            if (docElementName != null && docElementName.getText() != null) {
                String content = docElementContents.getText();
                docs.put(docElementName.getText(), content);
            }
        }
        return docs;
    }

    public static void writeDocuments(Element element, Map<String, String> docs) {
        Set<String> docNames = docs.keySet();
        for (String docName : docNames) {
            String contents = docs.get(docName);
            if (contents==null) contents = "";

            Element docElementName = new Element(NAME);
            docElementName.setText(docName);

            Element docElementContents = new Element(CONTENTS);
            docElementContents.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
            docElementContents.setContent(new CDATA(contents));

            Element docElement = new Element(SCRATCH_PAD);
            docElement.addContent(docElementName);
            docElement.addContent(docElementContents);

            element.addContent(docElement);
        }
    }

}
