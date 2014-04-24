package cz.muni.exceptions.listener.classifier;

import cz.muni.exceptions.listener.db.model.TicketClass;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 *
 */
public class StaxPackageDataParserTest {

    private StaxPackageDataParser parser = new StaxPackageDataParser();

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullInputStream() {
        parser.parseInput(null);
    }

    @Test
    public void testParseValidPackagesData() throws IOException {
        InputStream dataStream = loadStream("classifier/valid_packages.xml");

        Node tree = parser.parseInput(dataStream);
        Assert.assertEquals(tree.getChildren().size(), 2);

        Node thirdTokenNodeA = new Node("logging", TicketClass.UTILS, 1.0, Collections.EMPTY_SET);
        Node secondTokenNodeA = new Node("jboss", TicketClass.INTEGRATION, 0.5, Collections.singleton(thirdTokenNodeA));
        Node firstTokenNodeA = new Node("org", TicketClass.UNKNOWN, 0.0, Collections.singleton(secondTokenNodeA));

        Node secondTokenNodeB = new Node("lang", TicketClass.JVM, 1.0, Collections.EMPTY_SET);
        Node firstTokenNodeB = new Node("java", TicketClass.UNKNOWN, 0.0, Collections.singleton(secondTokenNodeB));

        Assert.assertTrue(tree.getChildren().contains(firstTokenNodeA));
        Assert.assertTrue(tree.getChildren().contains(firstTokenNodeB));

        dataStream.close();
    }

    private InputStream loadStream(String path) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return classLoader.getResourceAsStream(path);
    }
}
