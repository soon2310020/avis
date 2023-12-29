package vn.edu.topica.edumall.api.lms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create the test case
     */
    public AppTest() throws IOException {
        super();
        testApp();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() throws IOException {
        String source = objectMapper.readValue(getClass().getClassLoader()
                .getResourceAsStream("data/PR_source.json"), JsonNode.class).toString();
        String destination = objectMapper.readValue(getClass().getClassLoader()
                .getResourceAsStream("data/PR_result.json"), JsonNode.class).toString();

        JSONAssert.assertEquals(source, destination, true);
    }
}
