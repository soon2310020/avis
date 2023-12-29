package saleson.common.security;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml2.core.NameID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
class SAMLUserDetailsServiceImplTest {
	public static final String SSO_ID = "emol3";
	public static final String PASSWORD = "<PASSWORD>";
	public static final String EMAIL_DOMAIN = "@dyson.com";
	public static final String REMOTE_ENTITY_ID = "/adfs/services/trust";
	public static final String PREPIX = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/";
	@Autowired
	private SAMLUserDetailsServiceImpl userDetailsService;

	@Test
	public void testLoadUserBySAML() {
		// given
		NameID mockNameID = mock(NameID.class);
		when(mockNameID.getValue()).thenReturn(SSO_ID);

		SAMLCredential credentialsMock = mock(SAMLCredential.class);
		when(credentialsMock.getNameID()).thenReturn(mockNameID);
		when(credentialsMock.getRemoteEntityID()).thenReturn(REMOTE_ENTITY_ID);
		when(credentialsMock.getAttributeAsString(PREPIX + "emailaddress")).thenReturn(SSO_ID + EMAIL_DOMAIN);

		// when
		Object actual = userDetailsService.loadUserBySAML(credentialsMock);

		// then
		assertNotNull(actual);
		assertTrue(actual instanceof UserPrincipal);

		UserPrincipal userPrincipal = (UserPrincipal) actual;
		assertEquals(SSO_ID + EMAIL_DOMAIN, userPrincipal.getUsername());
		assertEquals(PASSWORD, userPrincipal.getPassword());
		assertTrue(userPrincipal.isEnabled());
		assertEquals(1, userPrincipal.getAuthorities().size());

		List<GrantedAuthority> authorities = new ArrayList<>(userPrincipal.getAuthorities());
		Object authority = authorities.get(0);

		assertTrue(authority instanceof SimpleGrantedAuthority);
	}
}