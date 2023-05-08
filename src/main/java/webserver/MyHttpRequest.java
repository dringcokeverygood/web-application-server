package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MyHttpRequest {

    private String method, path;
    private Map<String, String> headerMap = new HashMap<>();
    private Map<String, String> parameterMap = new HashMap<>();

    public MyHttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            method = line.split(" ")[0];
            if (method.equals("GET")) {
                String[] url = line.split(" ")[1].split("[?]");
                path = url[0];
                String[] parameterSplit = url[1].split("&");
                for (String param : parameterSplit) {
                    String key = param.split("=")[0];
                    String value = param.split("=")[1];
                    parameterMap.put(key, value);
                }
            }
            if (method.equals("POST")) {
                path = line.split(" ")[1];
            }
            while (line != null && !line.equals("")) {
                if (line.contains(":")) {
                    String[] s = line.split(":");
                    headerMap.put(s[0].trim(), s[1].trim());
                }
                line = br.readLine();
            }

            if (method.equals("POST")) {
                line = br.readLine();
                String[] parameterSplit = line.split("&");
                for (String param : parameterSplit) {
                    String key = param.split("=")[0];
                    String value = param.split("=")[1];
                    parameterMap.put(key, value);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String header) {
        return headerMap.get(header);
    }

    public String getParameter(String parameter) {
        return parameterMap.get(parameter);
    }
}
