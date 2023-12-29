package com.emoldino.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.util.DateUtils2.Zone;

public class HttpUtils {

	public static HttpServletRequest getRequest() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest req = attrs == null ? null : attrs.getRequest();
		return req;
	}

	public static String getUserAgent() {
		String value = getHeaderValue("User-Agent", null);
		return value;
	}

	public static boolean isAndroid() {
		String userAgent = getUserAgent();
		return userAgent != null && userAgent.contains("Android");
	}

	public static boolean isiOS() {
		String userAgent = getUserAgent();
		return userAgent != null && (userAgent.contains("iPhone") || userAgent.contains("iPad"));
	}

	public static String getReferer() {
		String value = getHeaderValue("Referer", null);
		return value;
	}

	public static String getTimeZone() {
		String value = getHeaderValue("TimeZone", Zone.SYS);
		return value;
	}

	private static String getHeaderValue(String name, String defaultValue) {
		String value = ThreadUtils.getProp("HttpUtils.header." + name, () -> {
			HttpServletRequest req = getRequest();
			if (req == null) {
				return defaultValue;
			}
			String _value = req.getHeader(name);
			return ValueUtils.toString(_value, defaultValue);
		});
		return value;
	}

	public static String getRequestUrl() {
		String value = ThreadUtils.getProp("HttpUtils.requestUrl", () -> {
			HttpServletRequest req = getRequest();
			if (req == null) {
				return null;
			}
			StringBuilder buf = new StringBuilder();
			buf.append(req.getMethod()).append(" ");
			String target = req.getRequestURL().toString();
			buf.append(target);
			return buf.toString();
		});
		return value;
	}

	public static String getRequestStr() {
		String value = ThreadUtils.getProp("HttpUtils.requestStr", () -> {
			HttpServletRequest req = HttpUtils.getRequest();
			if (req == null) {
				return null;
			}
			StringBuilder buf = new StringBuilder();
			String url = getRequestUrl();
			buf.append(url);
			String queryString = req.getQueryString();
			if (!ObjectUtils.isEmpty(queryString)) {
				if (!url.contains("?")) {
					buf.append("?");
				} else if (!url.endsWith("&") && !url.endsWith("?")) {
					buf.append("&");
				}
				buf.append(queryString);
			}
			return buf.toString();
		});
		return value;
	}

	public static String getParamsStr() {
		String value = ThreadUtils.getProp("HttpUtils.paramsStr", () -> {
			HttpServletRequest req = getRequest();
			if (req == null || req.getParameterMap() == null) {
				return null;
			}
			StringBuilder buf = new StringBuilder();
			int[] i = { 0 };
			req.getParameterMap().forEach((k, v) -> {
				if (ObjectUtils.isEmpty(v)) {
					return;
				}
				for (String str : v) {
					if (ObjectUtils.isEmpty(str)) {
						continue;
					}
					if (i[0]++ != 0) {
						buf.append(System.lineSeparator()).append("&");
					}
					buf.append(k).append("=").append(str);
				}
			});
			return buf.toString();
		});
		return value;
	}

	public static HttpStatus getStatus(Throwable t, HttpStatus defaultStatus) {
		HttpStatus _defaultStatus = defaultStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : defaultStatus;
		return t == null ? _defaultStatus : getStatus(t.getClass(), _defaultStatus);
	}

	private static HttpStatus getStatus(Class<?> eclass, HttpStatus defaultStatus) {
		if (eclass == null || eclass.equals(RuntimeException.class) || eclass.equals(Exception.class) || eclass.equals(Throwable.class)) {
			return defaultStatus;
		}
		ResponseStatus status = eclass.getAnnotation(ResponseStatus.class);
		if (status != null) {
			return status.value();
		}
		return getStatus(eclass.getSuperclass(), defaultStatus);
	}

	public static <T> T get(//
			String url, //
			List<Object> pathVarList, MultiValueMap<String, String> params, MultiValueMap<String, String> headers, //
			Class<T> returnClass, ParameterizedTypeReference<T> returnTypeRef, //
			int timeout) {
		return call(HttpMethod.GET, url, pathVarList, params, null, headers, returnClass, returnTypeRef, timeout);
	}

	public static <T> T call(//
			HttpMethod httpMethod, //
			String url, //
			List<Object> pathVarList, MultiValueMap<String, String> params, Object body, MultiValueMap<String, String> headers, //
			Class<T> returnClass, ParameterizedTypeReference<T> returnTypeRef, //
			int timeout) {
		Assert.notNull(url, "url is required!!");
		url = url.trim();

		if (returnClass != null && returnClass.equals(void.class)) {
			returnClass = null;
		}

		if (!ObjectUtils.isEmpty(params)) {
			if (pathVarList == null) {
				pathVarList = new ArrayList<Object>();
			} else {
				pathVarList = new ArrayList<Object>(pathVarList);
			}
			StringBuilder buf = new StringBuilder(url);
			if (url.endsWith("?") || url.endsWith("&")) {

			} else if (url.contains("?")) {
				buf.append("&");
			} else {
				buf.append("?");
			}
			int i = 0;
			for (String key : params.keySet()) {
				List<String> values = params.get(key);
				int j = 0;
				for (String value : values) {
					buf.append(i++ == 0 ? "" : "&").append(key).append("={").append(key).append(j++).append("}");
					pathVarList.add(value);
				}
			}

			url = buf.toString();
		}

		HttpEntity<?> reqEntity = new HttpEntity<>(body, headers);

		Object[] pathVars = pathVarList == null ? new Object[0] : pathVarList.toArray(new Object[pathVarList.size()]);

		RestOperations restOperations = getRestOperations(timeout);
		ResponseEntity<T> respEntity;
		if (returnTypeRef == null && returnClass != null) {
			Class<T> responseType = returnClass;
			respEntity = restOperations.exchange(url, httpMethod, reqEntity, responseType, pathVars);
		} else {
			respEntity = restOperations.exchange(url, httpMethod, reqEntity, returnTypeRef, pathVars);
		}

		T ret = returnClass == null && returnTypeRef == null ? null : respEntity.getBody();

		return ret;
	}

	private static final Map<Integer, RestOperations> REST_OPERATIONS = new ConcurrentHashMap<>();

	private static RestOperations getRestOperations(int timeout) {
		if (REST_OPERATIONS.containsKey(timeout)) {
			return REST_OPERATIONS.get(timeout);
		}

		RestOperations restOperations = SyncCtrlUtils.wrap("HttpUtils.restOperations." + timeout, REST_OPERATIONS, timeout, () -> {
			HttpClient httpClient = null;
			try {
				// SSL
				SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(trustAllSSLContext());

				if (csf != null) {
					HttpClientBuilder cb = HttpClients.custom();
					if (csf != null) {
						cb.setSSLSocketFactory(csf);
					}
					httpClient = cb.build();
				}
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			HttpComponentsClientHttpRequestFactory hrf = new HttpComponentsClientHttpRequestFactory();
			if (httpClient != null) {
				hrf.setHttpClient(httpClient);
			}
			int to = timeout <= 0 ? 30000 : timeout;
			hrf.setConnectTimeout(to);
			hrf.setReadTimeout(to);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setMessageConverters(Arrays.asList(new StringHttpMessageConverter(), new MappingJackson2HttpMessageConverter()));
			return restTemplate;
		});

		return restOperations;
	}

	private static SSLContext trustAllSSLContext() throws GeneralSecurityException {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		} };
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, trustAllCerts, null);
		return context;
	}

	public static void respondFile(InputStream is, String fileName, String contentType, HttpServletResponse response) {
		fileName = ValueUtils.toString(fileName, "Un-named");
		contentType = ValueUtils.toString(contentType, MediaType.APPLICATION_OCTET_STREAM_VALUE);

		OutputStream os = null;
		try {
			os = response.getOutputStream();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			AbstractException ae = ValueUtils.toAe(e, "FILE_DOWN_FAIL");
			throw ae;
		} finally {
			ValueUtils.closeQuietly(is);
			ValueUtils.closeQuietly(os);
		}
	}

	public static void respondWorkbook(Workbook wb, String fileName, HttpServletResponse response) {
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			wb.write(bos);
			is = new ByteArrayInputStream(bos.toByteArray());
			HttpUtils.respondFile(is, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response);
		} catch (Exception e) {
			AbstractException ae = ValueUtils.toAe(e, "FILE_DOWN_FAIL");
			throw ae;
		} finally {
			ValueUtils.closeQuietly(is);
			ValueUtils.closeQuietly(wb);
			ValueUtils.closeQuietly(bos);
		}
	}

	public static void respondFile(ByteArrayOutputStream bos, String fileName, String contentType, HttpServletResponse response) {
		fileName = ValueUtils.toString(fileName, "Un-named");
		contentType = ValueUtils.toString(contentType, MediaType.APPLICATION_OCTET_STREAM_VALUE);

		try (OutputStream os = response.getOutputStream()) {
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			bos.writeTo(os);
		} catch (Exception e) {
			throw ValueUtils.toAe(e, "RESPOND_FAIL");
		}
	}

}
