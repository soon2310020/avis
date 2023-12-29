package saleson.service.rest;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static saleson.service.rest.HTTPServiceImpl.mapParamToNameValuePair;
import static saleson.service.rest.HTTPServiceImpl.mapParamToParamsURL;

@Service
public class RestfulAPIService {
    public JSONObject send(String url, Map<String, Object> stateParams) throws JSONException {
        return sendWithBodyString(url,stateParams,null);
    }
    public JSONObject sendWithBodyString(String url, Map<String, Object> stateParams, String bodyStr) throws JSONException {
        JSONObject body = new JSONObject();
        addParamToJson(body, stateParams);
        JSONObject result = null;
        String bodyValStr=bodyStr!=null?bodyStr:body.toString();
        try {
            byte[] res = HTTPServiceImpl.doConnect(url, HttpMethod.POST, bodyValStr);
            result = new JSONObject(new String(res));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void addParamToJson(JSONObject obj, Map<String, Object> params) throws JSONException {
        if (null == obj) {
            obj = new JSONObject();
        }
        if (null != params) {
            Iterator<String> iter = params.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                obj.put(key, params.get(key));
            }
        }
    }



    public String sendResponseText(String url, Map<String, Object> stateParams, HttpMethod httpMethod, Map<String, Object> stateBody) throws JSONException {
        JSONObject body = new JSONObject();
        addParamToJson(body, stateBody);
        String result = null;
        if (stateParams != null && !stateParams.keySet().isEmpty()) {
            url += "?" + mapParamToParamsURL(stateParams);
        }
        try {
            byte[] res = HTTPServiceImpl.doConnect(url, httpMethod, body.toString());
            result = new String(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject send(String url, Map<String, Object> stateParams, HttpMethod httpMethod, Map<String, Object> stateBody) throws JSONException {
        JSONObject result = new JSONObject(sendResponseText(url, stateParams, httpMethod, stateBody));
        return result;
    }

}
