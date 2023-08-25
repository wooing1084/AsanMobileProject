# AsanUserMobileProject
아산병원 사용자 어플리케이션 프로젝트

![](https://github.com/wooing1084/AsanMobileProject/blob/main/images/app%20architecture.png)

## 앱 사용 방법
<details>
  <summary>사용 메뉴얼</summary>
앱 시작 전, 워치와 휴대폰이 페어링 되어야 하고, 블루투스 연결되어야 한다.

1. 아이디, 비밀번호를 입력해 로그인을 한다. 
2. 먼저, 휴대폰 앱의 메인화면에서 start 버튼을 누른다.
3. 그 후, 워치 앱에서 추출할 센서를 선택한 후, start를 눌러 동작시킨다
4. 두 디바이스 모두 시작되면, 블루투스로 연결되어 센서 데이터가 전송 된다.
5. 현재 나의 센서 정보를 확인하려면 chart버튼을 통해 최근 10분간의 요약된 센서 데이터를 볼 수 있다.

</details>

## OS Dependency
Android OS환경에서만 사용 가능하다.

**사용가능 버전**
- 최소: Android 7(Nougat)
- 최대: Android 11
  
*(이후 버전은 현재 수정중에 있다.)*

### Android OS버전에 따른 갤럭시 기기

![](https://github.com/wooing1084/AsanMobileProject/blob/main/images/galaxy_os_version.png)


**검증완료 기기**
- 삼성 갤럭시 Note9/ Android 10(Quince Tart)
- 삼성 갤럭시 S8+/ Android 8(Oreo)
- LG Q6 / Android 8(Oreo)

## 사용된 라이브러리들
- okhttp3
- MPAndroidChart
- RoomDB
- openCSV
- greenbot
- coroutines

**라이브러리의 종속성 추가는 build.gradle파일에 정리되어 있다.**


## Activities

![](https://github.com/wooing1084/AsanMobileProject/blob/main/images/activity%20architecture.png)

- LoginActivity
- SensorActivity
- SensorChartActivity
- CsvPopupActivity

<details>
  <summary>기능 설명</summary>
  
  ## LoginActivity
  
  로그인 액티비티, 사용자의 아이디를 입력받아 서버에 로그인 요청을 보낸다.
  응답이 성공적일시 유저 정보를 저장하고 SensorActivty로 이동한다.
  한번 로그인 시 cache를 통해 바로 서버에 로그인 요청하여 로그인 과정을 넘어간다.

  ## SensorActivity

  주요 기능이 이루어지는 메인 액티비티이다. 워치와 소켓 연결을 하고, 워치와의 통신 및 서버에 데이터 전송하는
  백그라운드 서비스를 실행시킨다.

  **주요기능**
  - Start버튼 : 페어링된 워치와 블루투스 소켓 연결을 한다.
  - Stop버튼 : 소켓 연결을 끊고 서비스를 종료한다.
  - Chart버튼 : 사용자의 센서 정보를 출력하는 SensorChartActivity로 이동한다.
 
  ## SensorChartActivity

  사용자의 센서 정보를 출력하는 액티비티이다. 10초마다 차트를 갱신하며, 과거 10분동안의 데이터를 1분간격으로
  요약하여 보여준다. MPChart라이브러리를 사용하였다.

  ## CsvPopupActivity

  csv파일이 내부 저장소에 저장되는지 확인하기 위한 액티비티로, 테스트용 액티비티이다.

  </details>
  
---

## Common
- BTManager
- CacheManager
- CsvController
- CsvStatistics
- DeviceInfo
- RegexManager
- ServerConnection
- SocketState
- SocketStateEvent
- ThreadState
- ThreadStateEvent

<details>
  <summary>기능 설명</summary>

  ## BTManager
  블루투스 기능을 담당하는 싱글톤 클래스이다. 블루투스 permission check가 필요한 기능을 모아두었으며,
  주로 블루투스 연결된 워치 정보를 가져오는 기능을 한다.
  
  ## CacheManager
  캐시 파일을 저장하고 로드하는 클래스이다. 현재는 로그인기능에만 사용중이다.

  ## CsvController
  CSV 관련 처리 객체, 싱글톤 구조로 구현되어있다.
  
  **주요 기능**
  - getExternalPath : 파일이 저장된 저장소 경로 반환 (대부분의 파일이 이 경로를 통해 저장된다.)
  - fileExist : 파일이 존재하는지 확인하는 함수
  - getExistFileName : 센서명을 통해 존재하는 파일을 가져온다. (파일명에 존재하는 unixtime을 알 수 없기때문에 사용한다.)
  - csvFirst : 센서파일이 존재하지 않을때 사용한다. 센서파일 초기 설정 및 저장기능
  - csvSave : 센서파일이 존재하는 경우 사용한다. 이어쓰기 기능
  - getFile : 파일 읽어오기
  - moveFile : 파일 경로 변경기능. 서버에 센서파일 전송 후에 전송된 파일 목록으로 이동시키기 위해 사용한다.

  ## CsvStatistics
  저장된 Csv파일을 요약하는 클래스이다. 현재는 사용하지 않는다.

  ## DeviceInfo
  디바이스 정보를 저장하는 싱글톤 클래스이다. 디바이스 ID, 유저 ID, 배터리 잔량을 저장한다.

  ## RegexManager
  정규표현식 클래스이다. 워치의 센서 정보를 읽는데 사용한다.

  ## ServerConnection
  서버와의 통신을 담당하는 싱글톤 클래스이다.

  **주요기능**
  - Login : 서버에 로그인 요청을 보내고, 성공시 sensorActivity로 이동시킨다.
  - postFile : 서버에 csv파일을 전송한다.
 
  ## SoecketState & socketStateEvent 
  블루투스 상태 확인하는 클래스

  ## ThreadState & ThreadStateEvent
  스레드 상태 확인하는 클래스

</details>

---
## Sensor

![](https://github.com/wooing1084/AsanMobileProject/blob/main/images/sensor%20architecture.png)

- SensorController
- Dao
- model
- service
- AppDatabase

<details>
  <summary>기능 설명</summary>
  
  Controller <-> Service <-> Dao <-> RoomDB의 구조를 띄고 있다.

  ## SensorController
  입력 받은 센서를 핸들링하며 핸들링한 데이터를 서비스레이어로 전달하거나<br>
  서비스로부터 받은 데이터를 애플리케이션 레이어로 전달하는 클래스
  
  ## Dao
  RoomDB쿼리를 담당하는 클래스

  ## service
  입력 받은 센서를 저장하기 위해 사용하는 클래스
  여기서 알맞은 동작을 입력받아 DAO를 호출해 데이터를 관리한다
  ##

</details>

---
## Service
- AcceptService
- AcceptThread

<details>
  <summary>기능 설명</summary>

  ## AcceptService
  서버와 블루투스 연결 여부 확인 및 연결을 담당하고, 워치를 통해 수집된 데이터를 csv파일로 저장하하며, 서버에 주기적으로 csv파일을 전송하는 포그라운드 서비스이다.

  **주요 기능 요약**
  - onStartCommand : 포그라운드 시작을 위해 서비스 실행 알림을 띄운다. 블루투스 소켓을 연결하고, 서버 파일 전송 타이머를 실행시킨다.
  - csvWrite : csv를 작성 하는 메소드이다. 타이머를 통해 서비스가 살아있는동안 주기적으로 실행되며 일정 횟수마다(6번) sendCSV메소드를 실행하여 서버에 파일을 전송한다.
  - sendCSV : CSV파일을 서버로 전송하는 메소드이다. 전송할 파일을 전송된 파일 목록으로 이동시키고, 서버에 POST방식으로 전송한다.

  ## AcceptThread
  서비스에서 워치와의 블루투스 소켓 연결을 담당하는 스레드 클래스이다.
  연결된 소켓을 통해 워치로부터 센서 데이터를 받아오며, 해석하여 RoomDB에 저장한다.
  
  
</details>
