package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpRequest {

    private String method, path, header, parameter;

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            method = line.split(" ")[0];
            if (method.equals("GET")) {
                String[] url = line.split(" ")[1].split("[?]");
                path = url[0];
                parameter = url[1];
            }
            if (method.equals("POST")) {
                url = line.split(" ")[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            String fullPath = line.split(" ")[1];
            return fullPath.split("[?]")[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHeader(String header) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            while (!line.equals("")) {
                if (line.startsWith(header)) {
                    return line.split(" ")[1];
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getParameter(String parameter) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            String fullPath = line.split(" ")[1];
            String parametersString = fullPath.split("[?]")[1];
            System.out.println(parametersString);
            String[] parameters = parametersString.split("[&]");
            for (String param : parameters) {
                if (param.startsWith(parameter)) {
                    return param.split("[=]")[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
