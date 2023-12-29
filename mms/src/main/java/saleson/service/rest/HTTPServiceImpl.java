package saleson.service.rest;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class HTTPServiceImpl {
    public void send(String phone, String message) {

    }

    public static byte[] doConnect(String url, HttpMethod method, byte[] body) throws IOException {
        HttpURLConnection connection = buildConnection(url, method);
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(body);
        out.flush();
        out.close();
        byte[] bytes = IOUtils.toByteArray(connection);
        connection.disconnect();
        return bytes;
    }

    public static byte[] doConnect(String url, HttpMethod method, String body) throws IOException {
        return doConnect(url, method, body.getBytes());
    }

    private static HttpURLConnection buildConnection(String uri, HttpMethod method) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method.name());
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON.toString());
        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);
        return connection;
    }

    public static List<NameValuePair> mapParamToNameValuePair(Map<String, Object> paramsMap) {
        if (paramsMap == null || paramsMap.keySet().isEmpty()) return new ArrayList<>();
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        paramsMap.keySet().stream().forEach(key -> {

            if (paramsMap.get(key) instanceof List) {
                ((List) paramsMap.get(key)).stream().forEach(val -> {
                    nameValuePairs.add(new NameValuePair() {
                        @Override
                        public String getName() {
                            return key;
                        }

                        @Override
                        public String getValue() {
                            return String.valueOf(val);
                        }
                    });
                });
            } else if (paramsMap.get(key) instanceof Arrays) {
                Arrays.stream((Object[]) paramsMap.get(key)).forEach(val -> {
                    nameValuePairs.add(new NameValuePair() {
                        @Override
                        public String getName() {
                            return key;
                        }

                        @Override
                        public String getValue() {
                            return String.valueOf(val);
                        }
                    });
                });
            } else {
                nameValuePairs.add(new NameValuePair() {
                    @Override
                    public String getName() {
                        return key;
                    }

                    @Override
                    public String getValue() {
                        return String.valueOf(paramsMap.get(key));
                    }
                });
            }
        });
        return nameValuePairs;
    }
    public static String mapParamToParamsURL(Map<String, Object> paramsMap) {
        if (paramsMap == null || paramsMap.keySet().isEmpty()) return "";
        List<NameValuePair> nameValuePairs = mapParamToNameValuePair(paramsMap);
        return URLEncodedUtils.format(nameValuePairs, StandardCharsets.UTF_8);
    }
}
