package vn.com.twendie.avis.authen;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import vn.com.twendie.avis.authen.model.payload.SignUpRequest;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
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
	public void testApp() {
		assertTrue(true);
	}

	public static void main(String argsp[]) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		SignUpRequest request = new SignUpRequest();
		request.setAddress("Ha noi - Viet nam");
		request.setUsername("trungnt");
		request.setPassword("avis2020");
		request.setCode("Avis");
		request.setEmail("trungnt@twendie.vn");
		request.setMobile("09099999999");
		request.setIdCard("13999823444");
		System.out.println(mapper.writeValueAsString(request));
	}
}
