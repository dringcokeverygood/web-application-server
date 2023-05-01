package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private DataBase db = new DataBase();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            String[] tokens = line.split(" ");
            String url = tokens[1];

            int contentLength = 0;
            while (line != null && !line.equals("")) {
                contentLength = getContentLength(line, contentLength);
                line = br.readLine();
            }

            Map<String, String> sm = new HashMap<>();
            if (contentLength != 0) {
                String params = IOUtils.readData(br, contentLength);
                sm = HttpRequestUtils.parseQueryString(params);
            }

            if ("/user/create".equals(url)) {
                url = "/index.html";
                signUpUser(sm);
            }

            boolean loginRequest = false;
            boolean loginSuccess = false;

            if ("/user/login".equals(url)) {
                loginRequest = true;
                if (db.findUserById(sm.get("userId")) == null || !db.findUserById(sm.get("userId")).equals(sm.get("password"))) {
                    url = "/user/login_failed.html";
                } else {
                    url = "/index.html";
                    loginSuccess = true;
                }
            }


            DataOutputStream dos = new DataOutputStream(out);
            byte[] body;
            body = Files.readAllBytes(new File("./webapp" + url).toPath());
            if (contentLength != 0 && !loginRequest) {
                response302Header(dos, url);
            }
            if (loginRequest) {
                response200LoginHeader(dos, body.length, loginSuccess);
            }
            if (contentLength == 0) {
                response200Header(dos, body.length);
            }
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void signUpUser(Map<String, String> sm) throws UnsupportedEncodingException {
        User user = new User(URLDecoder.decode(sm.get("userId"), "UTF8"), URLDecoder.decode(sm.get("password"), "UTF8"), URLDecoder.decode(sm.get("name"), "UTF8"), URLDecoder.decode(sm.get("email"), "UTF8"));
        db.addUser(user);
    }

    private static int getContentLength(String line, int contentLength) {
        if (line.contains("Content-Length:")) {
            contentLength = Integer.parseInt(line.split(" ")[1]);
        }
        return contentLength;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200LoginHeader(DataOutputStream dos, int lengthOfBodyContent, boolean loginSuccess) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Location : " + (loginSuccess?"/index.html":"/user/login_failed.html") + " \r\n");
            dos.writeBytes("Set-Cookie: logined=" + loginSuccess + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location : " + url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
