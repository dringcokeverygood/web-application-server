# 🐢 5장 웹 서버 리팩토링, 서블릿 컨테이너와 서블릿의 관계
> #### 저자가 작성한 브랜치의 최종 파일로 진행 했습니다. 이번 챕터에선 리팩토링을 메인으로 다뤄봅시다.
- - -
## 5.1 HTTP 웹서버 리팩토링 실습

### 5.1.1 리팩토링할 부분 찾기
> 냄새나는 코드를 찾아내 수정하자!!

### 5.1.2 리팩토링 1단계 힌트
#### 5.1.2.1 요청 데이터를 처리하는 로직을 별도의 클래스로 분리한다(HttpRequest)
> 1. 클라이언트 요청 데이터를 담고 있는 InputStream을 생성자로 받아 HTTP 메소드, URL, 헤더, 본문을 분리하는 작업을 한다.
> 2. 헤더는 Map<String, String>에 저장해 관리하고 getHeader("필드 이름") 메소드를 통해 접근 가능하도록 구현한다.
> 3. GET과 POST 메소드에 따라 전달되는 인자를 Map<String, String>에 저장해 관리하고 getParameter("인자 이름") 메소드를 통해 접근 가능하도록 구현한다.  

--> 생성자 단계에서 모든 Http Request를 읽어들이고, GET방식과 POST방식에서 우리가 취해야할 차이점은 Parameter를 어떤 위치에서 받는가이다.  
private String path, method; private String Map<String, String> hedaerMap, parameterMap; 을 이용해서 request를 읽는 과정에서 미리 모든 정보를 기록했다가  
get~~ 메서드 요청이 들어오면 그 값을 리턴해주기만 하면 된다.
#### 내가 구현한 코드
```java
public class HttpRequest {

    private String method, path;
    private Map<String, String> headerMap = new HashMap<>();
    private Map<String, String> parameterMap = new HashMap<>();

    public HttpRequest(InputStream in) {
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
```
이상 없이 모든 테스트 케이스를 통과한다.
- - - 
#### 5.1.2.2 응답 데이터를 처리하는 로직을 별도의 클래스로 분리한다(HttpResponse)
> 1. RequestHandler 클래스를 보면 응답 데이터 처리를 위한 많은 중복이 있다. 이 중복을 제거해 본다.
> 2. 응답 헤더 정보를 Map<String, String>으로 관리한다.
> 3. 응답을 보낼 때 HTML, CSS, 자바스크립트 파일을 직접 읽어 응답으로 보내는 메소드는 forward(), 다른 URL로 리다이렉트하는 메소드는 sendRedirect() 메소드를 나누어 구현한다.  

--> 기존 RequestHandler의 많은 부분을 차지하던 코드는 바로 응답과 관련한 코드였다. 200응답이냐 302응답이냐에 따라 메서드를 달리했고, 
로그인 실패, 성공에 따라 분리되는 부분도 있었다. 이런 부분을 따로 클래스로 빼내서 리팩토링해보자.  
이 부분은 전혀 감이 잡히지 않아서 직접 하진 못하고 코드를 따라쳤다.
```java
package http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private DataOutputStream dos = null;

    private Map<String, String> headers = new HashMap<String, String>();

    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void forward(String url) {
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            if (url.endsWith(".css")) {
                headers.put("Content-Type", "text/css");
            } else if (url.endsWith(".js")) {
                headers.put("Content-Type", "application/javascript");
            } else {
                headers.put("Content-Type", "text/html;charset=utf-8");
            }
            headers.put("Content-Length", body.length + "");
            response200Header(body.length);
            responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void forwardBody(String body) {
        byte[] contents = body.getBytes();
        headers.put("Content-Type", "text/html;charset=utf-8");
        headers.put("Content-Length", contents.length + "");
        response200Header(contents.length);
        responseBody(contents);
    }

    private void response200Header(int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            processHeaders();
            dos.writeBytes("Location: " + redirectUrl + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHeaders() {
        try {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                dos.writeBytes(key + ": " + headers.get(key) + " \r\n");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}

```
forward, forwardBody에서 응답의 틀을 만들고 response~~와 sendRedriect에서 응답을 보낸다.
- - -
#### 5.1.2.3 다형성을 활용해 클라이언트 요청 URL에 대한 분기 처리를 제거한다.
RequestHandler 클래스를 보면 기능이 추가될 때마다 분기분 (if/else)이 하나씩 추가되기때문에 이런 분기문을 제거하기 위해 다형성을 활용한다.
> 1. 각 요청과 응답에 대한 처리를 담당하는 부분을 추상화해 인터페이스로 만든다.
> 2. 각 분기문을 Controller 인터페이스를 구현하는 클래스를 만들어 분리한다.
> 3. 이렇게 생성한 Controller 구현체를 Map<String, Controller>에 저장한다. Map의 key에 해당하는 String은 요청 URL, value에 해당하는 Controller는 Controller 구현체이다.
> 4. 클라이언트 요청 URL에 해당하는 Controller를 찾아 service() 메소드를 호출한다.
> 5. Controller 인터페이스를 구현하는 AbstractController 추상 클래스를 추가해 중복을 제거하고, service() 메소드에서 GET과 POST HTTP 메소드에 따라 doGet(), doPost() 메소드를 호출하도록 한다.

--> 이 부분도 혼자는 못하겠어서 따라쳤다. 파일이 너무 많아서 코드는 생략

## 5.2 웹 서버 리팩토링 구현 및 설명
4장에서 구현한 HTTP 웹 서버 코드의 핵심은 RequestHandler 클래스이다. RequestHandler 클래스는 클라이언트 요청에 대한 헤더와 본문 데이터 처리, 클라이언트
요청에 따른 로직 처리(회원가입, 로그인 등), 로직 처리 완료 후 클라이언트에 대한 응답 헤더와 본문 데이터 처리 작업으로 나뉜다.
클래스 하나가 너무 많은 일을 하므로 각 객체가 한가지 책임을 가지도록 설계를 개선하는 리팩토링을 진행한다.

### 최종 구현 코드(RequestHandler)
```java
package webserver;

import http.HttpRequest;
import http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.Controller;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            Controller controller = RequestMapping.getController(request.getPath());
            if (controller == null) {
                String path = getDefaultPath(request.getPath());
                response.forward(path);
            } else {
                controller.service(request, response);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getDefaultPath(String path) {
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
    }
}
```

## 요청 흐름 :
1. 웹페이지가 변경되면서 새로운 HttpRequest가 발생하면 먼저 RequestHandler에서 HttpRequest 객체를 생성한다. 동시에 response객체도 
생성해 응답을 대기한다.
2. HttpRequest와 HttpResponse 클래스에서는 in과 out 값에 따라 해야할 일을 수행한다(header, body값 분석, 응답 뼈대 만들기)
3. HttpRequest의 url을 토대로 RequestMapping에서 해당하는 url을 찾아 맞는 다형성으로 Controller를 찾아준다.
4. Controller 내부에서는 url의 요청이 get이냐 post냐에 따라 맞는 로직을 실행해준다.(여기서 응답값이 마무리된다.)

mvc 패턴에서 model부분과 controller부분이 RequestHandler와 Controller에 모두 포함되어있고, view 부분은 html파일로 진행된다.  
이 때 중요한 점은, HttpRequest와 HttpResponse 부분은 개발자가 중요하게 신경쓰지 않아야 할 부분이지만, 현재 코드는 이 부분을 구현하는 데에 많은 시간을 투자해야 한다.  

### 5.2.4 HTTP 웹 서버의 문제점
1. HTTP 요청과 응답 헤더, 본문 처리와 같은 데 시간을 투자함으로써 정작 중요한 로직을 구현하는 데 투자할 시간이 상대적으로 적다.
2. 동적인 HTML을 지원하는 데 한계가 있다. 동적으로 HTML을 생성할 수 있지만 많은 코딩량을 필요로 한다.
3. 사용자가 입력한 데이터가 서버를 재시작하면 사라진다. 사용자가 입력한 데이터를 유지하고 싶다.

### 내가 생각하는 각 문제를 해결하는 방법
1. HTTP 요청을 처리하는 데 필요한 노동은 지금으로썬 딱히 방법이 없다. 서블릿이나 스프링같은 것을 사용해 이미 만들어진 것을 사용하는  
게 좋을 것같다. 만약 직접 구현하는 것을 목표로 한다면 지금처럼 계속 해야할 듯 하다.
2. 동적 HTML을 구현하기 위해서는 JSP같은 스크립트용 뭔가가 필요하다. 만약 직접 구현하는 것을 목표로 한다면 지금처럼 계속 해야할 듯 하다.
3. 데이터가 서버가 켜진동안만 유지되는 문제는 어떤 데이터베이스를 연결해야할 듯 하다. 하지만 데이터베이스를 연결하는 로직을 
직접 짤 수 있을까? 아니라고 본다. 역시 jdbc, mybatis, jpa등이 필요할 듯하다.

## 5.3 서블릿 컨테이너, 서블릿/JSP를 활용한 문제 해결
서블릿은 앞서 구현한 웹 서버의 Controller, HttpRequest, HttpResponse를 추상화 해 인터페이스로 정의해 놓은 표준이다. 
즉, HTTP의 클라이언트 요청과 응답에 대한 표준을 정해 놓은 것을 서블릿이라 생각하면 된다. 서블릿 컨테이너는 이 서블릿 표준에
대한 구현을 담당하고 있으며 앞에서 구현한 웹 서버가 서블릿 컨테이너 역할과 같다고 생각하면 된다.
> 이하 서블릿 사용법에 대한 내용
