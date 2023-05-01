# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
* https request는 inputstream 방식을 통해 가져올 수 있다. 이후 inputstreamreader와 bufferedreader로 한줄씩
읽어내고, url을 바이트 코드로 변환해 파일 위치를 찾아 html을 뿌려준다. 

### 요구사항 2 - get 방식으로 회원가입
* get 방식의 쿼리 파라미터는 http header에 text 형태로 담겨서 온다. url이 그대로 오기때문에 HttpRequestUtils 클래스의 함수로 파라미터만 떼어내서
사용할 수 있다.

### 요구사항 3 - post 방식으로 회원가입
* post 방식의 요청값은 http body에 text / json 형태로 담겨서 온다. header를 미리 다 읽어내고, 한줄 뛰어 넘은 뒤 body를 읽으면 된다.

### 요구사항 4 - redirect 방식으로 이동
* http 302 status는 redirect(Found)이다. 200으로 status를 보낼 경우 url이 html에서 보낸 form 태그 혹은 button 등의 url값인데,
302로 응답을 보낼 경우 redirect해서 url을 응답하는 html과 별개로 다른 url을 응답할 수 있다.

### 요구사항 5 - cookie
* 쿠키는 set-cookie를 http header에 담아서 응답해주면 된다.

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 