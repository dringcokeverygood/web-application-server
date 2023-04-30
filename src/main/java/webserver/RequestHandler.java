package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

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
            if (url.indexOf("?") != -1) {
                int index = url.indexOf("?");
                String requestPath = url.substring(0, index);
                String params = url.substring(index + 1);
                Map<String, String> sm = HttpRequestUtils.parseQueryString(params);
                User user = new User(URLDecoder.decode(sm.get("userId"), "UTF8"), URLDecoder.decode(sm.get("password"), "UTF8"), URLDecoder.decode(sm.get("name"), "UTF8"), URLDecoder.decode(sm.get("email"), "UTF8"));
                db.addUser(user);
                url = "/user/form.html";
                System.out.println(url);
                System.out.println(db.findUserById(user.getUserId()));
            }
            /*while (line != null && !line.equals("")) {
                System.out.println(line);
                line = br.readLine();
            }*/
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            /*System.out.println("---- body 길이 출력 시작 ----");
            System.out.println(body.length);
            System.out.println("---- body 길이 출력 끝 ----");
            System.out.println();*/
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
