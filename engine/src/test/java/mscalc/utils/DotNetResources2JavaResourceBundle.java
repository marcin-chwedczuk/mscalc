package mscalc.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

public class DotNetResources2JavaResourceBundle {
    @Test
    @Disabled
    void doIt() {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new FileReader("/Users/mc/github/calculator/src/Calculator/Resources/en-US/CEngineStrings.resw"));

            Properties resourceBundleData = new Properties();
            String key = null, value = null;

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ("data".equals(reader.getLocalName())) {
                            key = reader.getAttributeValue(null, "name");
                        } else if ("value".equals(reader.getLocalName())) {
                            value = reader.getElementText();

                            if (key != null && value != null) {
                                resourceBundleData.setProperty(key, value);
                                System.out.println("Key: " + key + ", value: " + value);
                            }

                            key = value = null;
                        }
                        break;
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream("mscalc/engine/calc-engine_en.properties")) {
                resourceBundleData.store(outputStream, "en-US/CEngineStrings.resw");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
