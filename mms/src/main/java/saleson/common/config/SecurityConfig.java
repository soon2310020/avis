package saleson.common.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.sql.DataSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPArtifactBinding;
import org.springframework.security.saml.processor.HTTPPAOS11Binding;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.HTTPSOAP11Binding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.storage.EmptyStorageFactory;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.ArtifactResolutionProfile;
import org.springframework.security.saml.websso.ArtifactResolutionProfileImpl;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileECPImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.emoldino.framework.security.EmoldinoLogoutHandler;

import saleson.common.security.JwtAuthenticationEntryPoint;
import saleson.common.security.JwtAuthenticationFilter;
import saleson.common.security.OpAuthenticationFailureHandler;
import saleson.common.security.OpAuthenticationSuccessHandler;
import saleson.common.security.OpUserDetailsService;
import saleson.common.security.SAMLAuthenticationSuccessHandler;
import saleson.common.security.SAMLUserDetailsServiceImpl;

@EnableWebSecurity
public class SecurityConfig {

	public static final String DYSON_PRODUCTION_URL = "https://ds0124.emoldino.com";
	public static final String NESTLE_ENTITY_URL = "https://sts.windows.net/12a3af23-a769-4654-847f-958f3d479f4a/";
	public static final String ELECTROLUX_ENTITY_URL = "https://sts.windows.net/d2007bef-127d-4591-97ac-10d72fe28031/";

	@Configuration
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		@Autowired
		private OpUserDetailsService opUserDetailsService;

		@Autowired
		private JwtAuthenticationEntryPoint unauthorizedHandler;

		@Autowired
		private PasswordEncoder passwordEncoder;

		@Bean
		public JwtAuthenticationFilter jwtAuthenticationFilter() {
			return new JwtAuthenticationFilter();
		}

		@Bean
		public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
			StrictHttpFirewall firewall = new StrictHttpFirewall();
			firewall.setAllowUrlEncodedSlash(true);
			return firewall;
		}

		@Override
		public void configure(WebSecurity web) throws Exception {
			super.configure(web);
			web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
			//swagger
			web.ignoring().antMatchers(//
					"/api/common/fle-tmp/**", //
					"/api/common/sys-mng", //
					"/v2/api-docs", //
					"/swagger-resources/**", //
					"**/swagger-resources/**", //
					"/swagger-ui.html", //
					"/webjars/**", //
					"/swagger/**", //
					"**/springfox-swagger-ui/**", //
					"/webjars/springfox-swagger-ui/**" //
			);
		}

		@Override
		public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
			authenticationManagerBuilder//
					.userDetailsService(opUserDetailsService)//
					.passwordEncoder(passwordEncoder);
		}

		@Bean(BeanIds.AUTHENTICATION_MANAGER)
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http//
					.cors()//

					.and()//
					.csrf()//
					.disable()//
					.exceptionHandling()//
					.authenticationEntryPoint(unauthorizedHandler)//

					.and()//
					.sessionManagement()//
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS)//

					.and()//
					.antMatcher("/api/**")//
					.authorizeRequests()//
					.antMatchers(//
							"/", //
							"/favicon.ico", //
							"/images/**", //
							"/**/*.ttf", //
							"/**/*.woff", //
							"/**/*.woff2", //
							"/**/*.png", //
							"/**/*.gif", //
							"/**/*.svg", //
							"/**/*.jpg", //
							"/**/*.html", //
							"/**/*.css", //
							"/**/*.map", //
							"/**/*.vue", //
							"/**/*.js"//
					).permitAll()//
					.antMatchers("/api/common/fle-tmp/**").permitAll()//
					.antMatchers("/api/auth/**", "/api/integration/**").permitAll()//					
					.antMatchers("/api/user/checkUsernameAvailability", "/api/user/checkEmailAvailability").permitAll()//
					.antMatchers(HttpMethod.GET, "/api/polls/**", "/api/users/**").permitAll()//
					.antMatchers("/swagger-resources/**").permitAll()//
//					.anyRequest().authenticated()//

					// JWT security filter
					.and()//
					.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		}
	}

	@Configuration
	@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
	@Order(1)
	public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter implements InitializingBean, DisposableBean {

		@Value("${host.url}")
		private String host;

		@Value("${saml.idp}")
		private String samlIdp; // EntryId

		@Autowired
		private DataSource dataSource;

		@Autowired
		private OpUserDetailsService opUserDetailsService;

		@Autowired
		private AuthenticationSuccessHandler opAuthenticationSuccessHandler;

		@Autowired
		private AuthenticationFailureHandler opAuthenticationFailureHandler;

		@Bean
		public JwtAuthenticationFilter jwtAuthenticationFilter2() {
			return new JwtAuthenticationFilter();
		}

		/*
		 * SAML Configuration
		 */
		@Autowired
		private SAMLUserDetailsServiceImpl samlUserDetailsService;

		private Timer backgroundTaskTimer;
		private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;

		public void init() {
			this.backgroundTaskTimer = new Timer(true);
			this.multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
		}

		public void shutdown() {
			this.backgroundTaskTimer.purge();
			this.backgroundTaskTimer.cancel();
			this.multiThreadedHttpConnectionManager.shutdown();
		}

		@Override
		public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
			authenticationManagerBuilder//
					.authenticationProvider(samlAuthenticationProvider()) // SAML
					.userDetailsService(opUserDetailsService)//
					.passwordEncoder(passwordEncoder());
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http//
					.csrf()//
					.csrfTokenRepository(new HttpSessionCsrfTokenRepository())//
					.ignoringAntMatchers("/mms/transfer/**", "/mms/data", "/mms/data/**")//
					.ignoringAntMatchers("/api/common/fle-tmp/**")//
					.ignoringAntMatchers("/api/auth/**", "/api/integration/**")//					
					.ignoringAntMatchers("/terminal-test/**")//
					.ignoringAntMatchers("/h2/**")//
					.ignoringAntMatchers("/api/common/ipc/**")//
					.ignoringAntMatchers("/api/analysis/dat-col", "/api/analysis/dat-col/**")//
					.ignoringAntMatchers("/api/**", "/logout")//

					// SAML Filter
					.and()//
					.addFilterBefore(jwtAuthenticationFilter2(), UsernamePasswordAuthenticationFilter.class)//
					.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)//
					.addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)//
					.addFilterBefore(samlFilter(), CsrfFilter.class)//

					.authorizeRequests()//
					.antMatchers(HttpMethod.GET, "/actuator/**").permitAll()//
					.antMatchers(//
							"/apis/**", // REST Docs 테스트용 API
							"/robots.txt", //
							"/favicon.ico", //
							"/images/**", //
							"/**/*.ttf", //
							"/**/*.woff", //
							"/**/*.woff2", //
							"/**/*.png", //
							"/**/*.gif", //
							"/**/*.svg", //
							"/**/*.jpg", //
							"/**/*.html", //
							"/**/*.css", //
							"/**/*.map", //
							"/**/*.vue", //
							"/**/*.js"//
					).permitAll()//
					.antMatchers("/create-account/**").permitAll()//
					.antMatchers("/api/auth/**", "/api/integration/**").permitAll()//					
					.antMatchers("/mms/transfer/**", "/mms/data", "/mms/data/heartbit", "/mms/data/heartbeat", "/mms/data/refine").permitAll()//
					.antMatchers("/mms/data/rawdata", "/mms/data/cdata", "/mms/data/adata/", "/mms/data/terminal").authenticated()//
					.antMatchers("/login", "/register").permitAll()//
					.antMatchers("/terminal-test/**").permitAll()//
					.antMatchers("/h2/**").permitAll()//
					.antMatchers("/logout").permitAll()//
					.antMatchers("/error").permitAll()//
					.antMatchers("/saml/**").permitAll() // SAML
					.antMatchers("/swagger-resources/**").permitAll()// swagger
					.antMatchers("/api/common/fle-tmp/**").permitAll()//
					.antMatchers(HttpMethod.POST, "/api/users").permitAll()//
					.antMatchers(HttpMethod.POST, "/api/users/create-user").permitAll()//
					.antMatchers("/api/users/create-user/**").permitAll()//
					.antMatchers("/api/users/fpassword").permitAll()//
					.antMatchers("/api/users/cpassword/**").permitAll()//
					.antMatchers("/api/users/send_pw_change_mail/**").permitAll()//
					.antMatchers("/api/users/pc_mail_sent/**").permitAll()//
					.antMatchers("/api/users/reset_password/**").permitAll()//
					.antMatchers("/api/users/password_changed/**").permitAll()//
					.antMatchers("/api/molds/wut").permitAll()//
					.antMatchers("/api/common/ipc/**").permitAll()//
					.antMatchers("/api/analysis/dat-col", "/api/analysis/dat-col/**").permitAll()//
					.anyRequest().authenticated()//

					.and()//
					.formLogin()//
					.loginPage("/login").permitAll()//
					.successHandler(opAuthenticationSuccessHandler)//
					.failureHandler(opAuthenticationFailureHandler)//

					.and()//
					.logout()//
					.logoutUrl("/logout")//
					.logoutSuccessUrl("/")//
//					.logoutSuccessHandler(logoutSuccessHandler)//

					.invalidateHttpSession(true)//
					.addLogoutHandler(logoutHandler())//
//					.deleteCookies(cookieNamesToClear)//

//					// remember-me 나중에 붙이자..
//					.and()//
//					.rememberMe()//
//					.rememberMeParameter("remember-me")//
//					.tokenRepository(tokenRepository())//
			;
		}

		@Bean
		public AuthenticationSuccessHandler opAuthenticationSuccessHandler() {
			return new OpAuthenticationSuccessHandler();
		}

		@Bean
		public AuthenticationFailureHandler opAuthenticationFailureHandler() {
			return new OpAuthenticationFailureHandler("/login?error");
		}

		@Bean
		public PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}

		@Bean
		public PersistentTokenRepository tokenRepository() {
			JdbcTokenRepositoryImpl jdbcTokenRepositoryImpl = new JdbcTokenRepositoryImpl();
			jdbcTokenRepositoryImpl.setDataSource(dataSource);
			return jdbcTokenRepositoryImpl;
		}

		/**
		 * SAML
		 * Central storage of cryptographic keys
		 * @return
		 */
		@Bean
		public KeyManager keyManager() {
			DefaultResourceLoader loader = new DefaultResourceLoader();
			Resource storeFile = loader.getResource("classpath:/saml/samlKeystore.jks");
			String storePass = "nalle123";
			Map<String, String> passwords = new HashMap<String, String>();
			passwords.put("apollo", "nalle123");
			String defaultKey = "apollo";
			return new JKSKeyManager(storeFile, storePass, passwords, defaultKey);
		}


		@Bean
		@Qualifier("idp-testDyson")
		public ExtendedMetadataDelegate testDysonExtendedMetadataProvider() throws MetadataProviderException {
			//String idpMetadataURL = "https://idp-test.dyson.com/FederationMetadata/2007-06/FederationMetadata.xml";
			String idpMetadataURL = "https://login.microsoftonline.com/b6e8236b-ceb2-401d-9169-2917d0b07d48/federationmetadata/2007-06/federationmetadata.xml?appid=3da72739-948e-467b-8a0b-2711e14fabe2";
			return getExtendedMetadataDelegate(idpMetadataURL);
		}

		@Bean
		@Qualifier("idp-testPng")
		public ExtendedMetadataDelegate testPngExtendedMetadataProvider() throws MetadataProviderException {

			//String idpMetadataURL = "https://metada.s3.us-east-2.amazonaws.com/idp-png.xml";
			String idpMetadataURL = "https://s3.us-west-2.amazonaws.com/secure.notion-static.com/cae075f0-ef47-4264-a41a-cac12ba4115f/emoldino_prod.xml?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20221208%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20221208T155014Z&X-Amz-Expires=86400&X-Amz-Signature=59399a880dccca82a13c84b5f921b569f027ecea37ddfd1790d76cc00175e80c&X-Amz-SignedHeaders=host&response-content-disposition=filename%3D%22emoldino%2520prod.xml%22&x-id=GetObject";
			//String idpMetadataURL = "https://fedauthtst.pg.com/pf/federation_metadata.ping?PartnerSpId=https://pg.emoldino.com";
			return getExtendedMetadataDelegate(idpMetadataURL);
		}

		@Bean
		@Qualifier("idp-dyson")
		public ExtendedMetadataDelegate dysonExtendedMetadataProvider() throws MetadataProviderException {
			//String idpMetadataURL = "https://idp.dyson.com/FederationMetadata/2007-06/FederationMetadata.xml";
			String idpMetadataURL = "https://login.microsoftonline.com/b6e8236b-ceb2-401d-9169-2917d0b07d48/federationmetadata/2007-06/federationmetadata.xml?appid=491115fb-489c-4753-8142-0d96eac0b29f";
			return getExtendedMetadataDelegate(idpMetadataURL);
		}

		@Bean
		@Qualifier("idp-png")
		public ExtendedMetadataDelegate pngExtendedMetadataProvider() throws MetadataProviderException {
			String idpMetadataURL = "https://metada.s3.us-east-2.amazonaws.com/png-prod-2022.xml";
			return getExtendedMetadataDelegate(idpMetadataURL);
		}

		@Bean
		@Qualifier("idp-nestle")
		public ExtendedMetadataDelegate nestleExtendedMetadataProvider() throws MetadataProviderException {
			String idpMetadataURL = "https://login.microsoftonline.com/12a3af23-a769-4654-847f-958f3d479f4a/federationmetadata/2007-06/federationmetadata.xml?appid=d4f7e29c-3c9a-4e6a-a04d-3168ea133c9a";
			return getExtendedMetadataDelegate(idpMetadataURL);
		}

		@Bean
		@Qualifier("idp-electrolux")
		public ExtendedMetadataDelegate electroluxExtendedMetadataProvider() throws MetadataProviderException {
			String idpMetadataURL = "https://login.microsoftonline.com/d2007bef-127d-4591-97ac-10d72fe28031/federationmetadata/2007-06/federationmetadata.xml?appid=c685a8d4-c055-452c-863a-b2481ae1388d";
			return getExtendedMetadataDelegate(idpMetadataURL);
		}

		/**
		 * Metadata Delegate
		 * @param idpMetadataURL
		 * @return
		 * @throws MetadataProviderException
		 */
		private ExtendedMetadataDelegate getExtendedMetadataDelegate(String idpMetadataURL) throws MetadataProviderException {
//			HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
//					this.backgroundTaskTimer, httpClient(), idpMetadataURL);
//			httpMetadataProvider.setParserPool(parserPool());

			MetadataProvider metadataProvider = metadataProvider(idpMetadataURL);
			ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(metadataProvider, extendedMetadata());
			extendedMetadataDelegate.setMetadataTrustCheck(false);
			extendedMetadataDelegate.setMetadataRequireSignature(false);
			backgroundTaskTimer.purge();
			return extendedMetadataDelegate;
		}

		private MetadataProvider metadataProvider(String idpMetadataURL) {
			if (idpMetadataURL.startsWith("http")) {
				return httpMetadataProvider(idpMetadataURL);
			} else {
				return fileSystemMetadataProvider(idpMetadataURL);
			}
		}

		private HTTPMetadataProvider httpMetadataProvider(String idpMetadataURL) {
			try {
				HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(this.backgroundTaskTimer, httpClient(), idpMetadataURL);
				httpMetadataProvider.setParserPool(parserPool());
				return httpMetadataProvider;
			} catch (MetadataProviderException e) {
				e.printStackTrace();
				return null;
			}
		}

		private FilesystemMetadataProvider fileSystemMetadataProvider(String idpMetadataURL) {
			DefaultResourceLoader loader = new DefaultResourceLoader();
			Resource metadataResource = loader.getResource(idpMetadataURL);
			File samlMetadata = null;
			try {
				samlMetadata = metadataResource.getFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			FilesystemMetadataProvider filesystemMetadataProvider = null;
			try {
				filesystemMetadataProvider = new FilesystemMetadataProvider(samlMetadata);
			} catch (MetadataProviderException e) {
				e.printStackTrace();
			}
			filesystemMetadataProvider.setParserPool(parserPool());
			return filesystemMetadataProvider;
		}

		/**
		 * Setup advanced info about metadata
		 * @return
		 */
		@Bean
		public ExtendedMetadata extendedMetadata() {
			ExtendedMetadata extendedMetadata = new ExtendedMetadata();
			extendedMetadata.setIdpDiscoveryEnabled(true);
			extendedMetadata.setSigningAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
			extendedMetadata.setSignMetadata(true);
			extendedMetadata.setEcpEnabled(true);
			return extendedMetadata;
		}

		/**
		 * IDP Metadata configuration - paths to metadata of IDPs in circle of trust
		 * @return
		 * @throws MetadataProviderException
		 */
		@Bean
		@Qualifier("metadata")
		public CachingMetadataManager metadata() throws MetadataProviderException {
			List<MetadataProvider> providers = new ArrayList<>();

			if (DYSON_PRODUCTION_URL.equals(host)) {
				providers.add(dysonExtendedMetadataProvider());

			} else if (NESTLE_ENTITY_URL.equals(samlIdp)) {
				providers.add(nestleExtendedMetadataProvider());

			} else if (ELECTROLUX_ENTITY_URL.equals(samlIdp)) {
				providers.add(electroluxExtendedMetadataProvider());

			}
			else {
				providers.add(pngExtendedMetadataProvider());
			}
			return new CachingMetadataManager(providers);
		}

		/**
		 * Filter automatically generates default SP metadata
		 * @return
		 */
		@Bean
		public MetadataGenerator metadataGenerator() {
			MetadataGenerator metadataGenerator = new MetadataGenerator();
			metadataGenerator.setEntityBaseURL(host);
			metadataGenerator.setEntityId(host);
			metadataGenerator.setExtendedMetadata(extendedMetadata());
			metadataGenerator.setIncludeDiscoveryExtension(false);
			metadataGenerator.setKeyManager(keyManager());
			return metadataGenerator;
		}

		/**
		 * The filter is waiting for connections on URL suffixed with filterSuffix
		 * and presents SP metadata setMetadataTrustCheckthere
		 * @return
		 */
		@Bean
		public MetadataDisplayFilter metadataDisplayFilter() {
			return new MetadataDisplayFilter();
		}

		/**
		 * XML parser pool needed for OpenSAML parsing
		 * @return
		 */
		@Bean(initMethod = "initialize")
		public StaticBasicParserPool parserPool() {
			return new StaticBasicParserPool();
		}

		@Bean(name = "parserPoolHolder")
		public ParserPoolHolder parserPoolHolder() {
			return new ParserPoolHolder();
		}

		/**
		 * IDP Discovery Service
		 * @return
		 */
		@Bean
		public SAMLDiscovery samlIDPDiscovery() {
			SAMLDiscovery idpDiscovery = new SAMLDiscovery();
			idpDiscovery.setIdpSelectionPath("/saml/discovery");
			return idpDiscovery;
		}

		/**
		 * SAML Authentication Provider responsible for validating of received SAML
		 * @return
		 */
		@Bean
		public SAMLAuthenticationProvider samlAuthenticationProvider() {
			SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
			samlAuthenticationProvider.setUserDetails(samlUserDetailsService);
			samlAuthenticationProvider.setForcePrincipalAsString(false);
			return samlAuthenticationProvider;
		}

		/**
		 * Provider of default SAML ContextSAMLContextProviderLB
		 * @return
		 */
		@Bean
		public SAMLContextProviderImpl contextProvider() {
			if (DYSON_PRODUCTION_URL.equals(host) || NESTLE_ENTITY_URL.equals(samlIdp) || ELECTROLUX_ENTITY_URL.equals(samlIdp)) {
				SAMLContextProviderLB cp = new SAMLContextProviderLB();
				cp.setScheme("https");
				cp.setServerName(host.replace("https://", ""));
				cp.setServerPort(443);
				cp.setIncludeServerPortInRequestURL(false);
				cp.setContextPath("/");
				cp.setStorageFactory(new EmptyStorageFactory());
				return cp;
			} else {
				return new SAMLContextProviderImpl();
			}
		}

		/**
		 * Initialization of OpenSAML library
		 * @return
		 */
		@Bean
		public static SAMLBootstrap sAMLBootstrap() {
			return new SAMLBootstrap();
		}

		/**
		 * Logger for SAML messages and events
		 * @return
		 */
		@Bean
		public SAMLDefaultLogger samlLogger() {
			SAMLDefaultLogger logger = new SAMLDefaultLogger();
			logger.setLogAllMessages(true);
			logger.setLogErrors(true);
			logger.setLogMessagesOnException(true);
			return logger;
		}

		/**
		 * SAML 2.0 WebSSO Assertion Consumer
		 * @return
		 */
		@Bean
		public WebSSOProfileConsumer webSSOprofileConsumer() {
			final long maxAgeSeconds = 15 * 24 * 60 * 60;
			WebSSOProfileConsumerImpl consumer = new WebSSOProfileConsumerImpl();
			consumer.setMaxAuthenticationAge(maxAgeSeconds);

			return consumer;
		}

		/**
		 * SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
		 * @return
		 */
		@Bean
		public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
			return new WebSSOProfileConsumerHoKImpl();
		}

		/**
		 * SAML 2.0 Web SSO profile
		 * @return
		 */
		@Bean
		public WebSSOProfile webSSOprofile() {
			return new WebSSOProfileImpl();
		}

		/**
		 * SAML 2.0 Holder-of-Key Web SSO profile
		 * @return
		 */
		@Bean
		public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
			return new WebSSOProfileConsumerHoKImpl();
		}

		/**
		 * SAML 2.0 ECP profile
		 * @return
		 */
		@Bean
		public WebSSOProfileECPImpl ecpprofile() {
			return new WebSSOProfileECPImpl();
		}

		@Bean
		public SingleLogoutProfile logoutprofile() {
			return new SingleLogoutProfileImpl();
		}

		@Bean
		public WebSSOProfileOptions defaultWebSSOProfileOptions() {
			WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
			webSSOProfileOptions.setIncludeScoping(false);
			return webSSOProfileOptions;
		}

		/**
		 * Entry point to initialize authentication, default values taken from properties file
		 * @return
		 */
		@Bean
		public SAMLEntryPoint samlEntryPoint() {
			SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
			samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
			return samlEntryPoint;
		}

		@Bean
		public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
			SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
			samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
			samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
			samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
			return samlWebSSOHoKProcessingFilter;
		}

		/**
		 * Processing filter for WebSSO profile messages
		 * @return
		 * @throws Exception
		 */
		@Bean
		public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
			SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
			samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
			samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
			samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
			return samlWebSSOProcessingFilter;
		}

		@Bean
		public MetadataGeneratorFilter metadataGeneratorFilter() {
			return new MetadataGeneratorFilter(metadataGenerator());
		}

		/**
		 * Filter processing incoming logout messages
		 * First argument determines URL user will be redirected to after successful global logout
		 * @return
		 */
		@Bean
		public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
			return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
		}

		/**
		 * Overrides default logout processing filter with the one processing SAML messages
		 * @return
		 */
		@Bean
		public SAMLLogoutFilter samlLogoutFilter() {
			return new SAMLLogoutFilter(//
					successLogoutHandler(), //
					new LogoutHandler[] { logoutHandler() }, //
					new LogoutHandler[] { logoutHandler() }//
			);
		}

		/**
		 * Handler for successful logout
		 * @return
		 */
		@Bean
		public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
			SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
			successLogoutHandler.setDefaultTargetUrl("/");
			return successLogoutHandler;
		}

		/**
		 * Logout handler terminating local session
		 * @return
		 */
		@Bean
		public SecurityContextLogoutHandler logoutHandler() {
			SecurityContextLogoutHandler logoutHandler = new EmoldinoLogoutHandler();
			logoutHandler.setInvalidateHttpSession(true);
			logoutHandler.setClearAuthentication(true);
			return logoutHandler;
		}

		/**
		 * Handler deciding where to redirect user after successful login
		 * @return
		 */
		@Bean
		public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
			/*SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler =
					new SavedRequestAwareAuthenticationSuccessHandler();
			successRedirectHandler.setDefaultTargetUrl("/");
			return successRedirectHandler;*/

			SavedRequestAwareAuthenticationSuccessHandler successHandler = new SAMLAuthenticationSuccessHandler();
			return successHandler;
		}

		/**
		 * Handler deciding where to redirect user after failed login
		 * @return
		 */
		@Bean
		public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
			OpAuthenticationFailureHandler failureHandler = new OpAuthenticationFailureHandler();
			failureHandler.setUseForward(false);
//			failureHandler.setDefaultFailureUrl("/login?error=sso");
			failureHandler.setDefaultFailureUrl("/login?error");
			return failureHandler;
		}

		private ArtifactResolutionProfile artifactResolutionProfile() {
			final ArtifactResolutionProfileImpl artifactResolutionProfile = new ArtifactResolutionProfileImpl(httpClient());
			artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding()));
			return artifactResolutionProfile;
		}

		@Bean
		public HTTPArtifactBinding artifactBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
			return new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile());
		}

		@Bean
		public HTTPSOAP11Binding soapBinding() {
			return new HTTPSOAP11Binding(parserPool());
		}

		@Bean
		public HTTPPostBinding httpPostBinding() {
			return new HTTPPostBinding(parserPool(), velocityEngine());
		}

		@Bean
		public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
			return new HTTPRedirectDeflateBinding(parserPool());
		}

		@Bean
		public HTTPSOAP11Binding httpSOAP11Binding() {
			return new HTTPSOAP11Binding(parserPool());
		}

		@Bean
		public HTTPPAOS11Binding httpPAOS11Binding() {
			return new HTTPPAOS11Binding(parserPool());
		}

		/**
		 * SAML Processor
		 * @return
		 */
		@Bean
		public SAMLProcessorImpl processor() {
			Collection<SAMLBinding> bindings = new ArrayList<>();
			bindings.add(httpRedirectDeflateBinding());
			bindings.add(httpPostBinding());
			bindings.add(artifactBinding(parserPool(), velocityEngine()));
			bindings.add(httpSOAP11Binding());
			bindings.add(httpPAOS11Binding());
			return new SAMLProcessorImpl(bindings);
		}

		/**
		 * Define the security filter chain in order to support SSO Auth by using SAML 2.0
		 *
		 * @return Filter chain proxy
		 * @throws Exception
		 */
		@Bean
		public FilterChainProxy samlFilter() throws Exception {
			List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"), metadataDisplayFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"), samlWebSSOProcessingFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSOHoK/**"), samlWebSSOHoKProcessingFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"), samlLogoutProcessingFilter()));
			chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"), samlIDPDiscovery()));
			return new FilterChainProxy(chains);
		}

		/**
		 * Initialization of the velocity engine
		 * @return
		 */
		@Bean
		public VelocityEngine velocityEngine() {
			return VelocityFactory.getEngine();
		}

		@Bean
		public HttpClient httpClient() {
			return new HttpClient(this.multiThreadedHttpConnectionManager);
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			init();
		}

		@Override
		public void destroy() throws Exception {
			shutdown();
		}
	}
}
