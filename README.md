# ⭐ 프로젝트 개요 

## 🧾 **프로젝트 명**

### TSE (Trello So Easy)

## 🪕 **프로잭트 소개**

### 관리 도구인 Trello를 토대로 Trello의 핵심 기능을 Spring을 이용하여 개발자 협업 툴을 만드는 프로젝트 입니다.
---

⭐ **주요 기술 스택**

![인텔리제이](https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![깃허브](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)

***Development***
    
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![자바](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![스프링](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![스프링부트](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![mysql](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white) <p>
***Communication***

![슬랙](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![노션](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)

---

⭐ **주요기능**

<details>
<summary>회원가입/로그인</summary>

* 회원가입
* 회원 정보 수정
* 회원 탈퇴
* 멤버 등록
* 유저 권한/멤버 역할: 유저의 권한을 USER/ADMIN 으로 나누어 ADMIN 만 워크스페이스를 생성할 수 있게 구현했고,
  워크스페이스 내부에서는 멤버역할을 설정해
  (USER:읽기 전용/ADMIN:워크스페이스 삭제 제외 모든기능 사용가능/OWNER:워크스페이스를 생성한 사람으로 이 사람만 워크스페이스 삭제가 가능하고 OWNER는 워크스페이스 내에서 한명만 가능)
  멤버역할과 유저 권한을 구현 했습니다.

* 

</details>

<details>
<summary>Trello 구성요소 CRUD</summary>

* 워크스페이스 생성 : 권한이 있는 유저만 워크스페이스 생성 및 삭제가 가능하다.
* 워크스페이스 삭제 : 권한이 있는 유저만 워크스페이스 생성 및 삭제가 가능하다.
* 보드 생성 : 보드를 생성해 리스트와 카드를 생성 할 수 있습니다.
* 보드 닫기 : 보드를 닫아 아카이브 기능을 사용할 수 있습니다 업데이트 불가
* 보드 닫기취소 : 보드를 닫는 작업을 취소할 수 있습니다
* 보드 조회하기 : 보드에 존재하는 리스트와 그 하위에 존재하는 카드를 리스트로 확인 할 수 있습니다.
* 리스트 생성 : 리스트를 생성해 하위에 카드를 작성할 수 있고 순서를 변경할 수 있습니다.
* 카드 기능 : 리스트에 카드를 생성해 내용,첨부파일,댓글을 작성하고 이모지를 달 수 있습니다. 카드의 순서를 변경할 때 업데이트 쿼리를 사용해 데이터베이스의 쿼리 수를 최소화 했습니다.
* 댓글 달기 : 카드에 댓글을 생성하고 삭제 할 수 있습니다.
* 이모지 달기 : 댓글에 이모지를 달고 삭제 할 수 있습니다.

</details>

<details>
<summary>첨부파일</summary>

* ## AWS를 이용한 첨부파일 저장
* AWS S3는 안정적이고 확장 가능한 클라우드 스토리지 서비스이다.
* 대용량 데이터를 쉽게 저장하고 관리할 수 있으며, 높은 가용성과 보안성 덕분에 많은 기업에서 파일 저장소로 사용한다.

* ## AWS 장 · 단점

### 장점

확장성: 사용량이 증가해도 성능 저하 없이 확장이 가능하다.  
가용성: S3는 높은 가용성을 제공하며, 데이터를 안전하게 백업하고 보관할 수 있다.  
비용 효율성: 사용한 만큼만 요금을 지불하는 Pay-as-you-go 모델로 운영된다.  
보안: 다양한 암호화 및 접근 제어 기능을 통해 데이터를 안전하게 보호한다.  
통합성: 다른 AWS 서비스와 쉽게 통합되어 기능 확장이 용이하다.

### 단점

초기 설정 복잡성: IAM(Identity and Access Management) 등 보안 설정이 복잡할 수 있다.  
비용 관리 어려움: 사용량이 많아질수록 비용이 증가할 수 있어 관리가 필요하다.  

### AWS를 선택한 이유  
가용성과 확장성이 뛰어나 대규모 파일 저장에 적합하다. 또한, AWS SDK를 사용하여 Spring Boot 애플리케이션과 쉽게 통합할 수 있어 개발 시간과 유지보수에 효율적이다. 높은 보안성을 제공해 사용자가
안심하고 데이터를 저장할 수 있다.

### 구현코드
S3에 파일을 업로드하기 위해 AmazonS3 객체를 사용하여 파일을 저장하고, 업로드된 파일의 URL을 데이터베이스에 저장하는 구조로 구현했다. 파일 확장자와 크기를 체크하는 로직을 통해 보안성을 높였고, 최대
파일 크기를 5MB로 제한했다.
```
 public File uploadFiles(Long sourceId, List<MultipartFile> files, FileEnum fileFolder) throws IOException {

        // 지원되는 파일 확장자 리스트
        List<String> allowedFileTypes = Arrays.asList("image/jpeg", "image/png", "application/pdf", "text/csv");
        // 최대 파일 크기: 5MB
        long maxFileSize = 5 * 1024 * 1024;

        for (MultipartFile file : files) {


            if (file.getSize() > maxFileSize) {
                throw new ApiException(ErrorStatus._FILE_SIZE_OVER_ERROR);
            }

            // 파일 형식 체크
            if (!allowedFileTypes.contains(file.getContentType())) {
                throw new ApiException(ErrorStatus._FILE_TYPE_MISS_MATCH);
            }

            String fileName = generateFileName(file);
            String fileKey = fileFolder + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileKey, inputStream, metadata);
                amazonS3.putObject(putObjectRequest);
            }
            String url = amazonS3.getUrl(bucketName, fileKey).toString();
            File image = File.of(url, sourceId, fileFolder);
            fileRepository.save(image);
        }
        return null;
    }


    private String generateFileName(MultipartFile file) {
        return UUID.randomUUID().toString() + "-" + file.getOriginalFilename().replace(" ", "_");
    }
```

### 설명:
uploadFiles 메서드는 파일을 업로드하는 핵심 기능이다.  
파일 형식과 크기를 먼저 확인한다.  
파일 이름은 UUID로 고유하게 생성한다.  
AWS S3에 파일을 업로드한 후, 파일의 URL을 받아온다.  
최종적으로 파일 정보를 데이터베이스에 저장한다.  
이 과정에서 S3 버킷의 이름과 업로드할 파일 폴더를 조합하여 fileKey를 생성하며, 이 키를 통해 나중에 파일을 검색하거나 삭제할 수 있다.  


</details>

<details>
<summary>알림</summary>




    public void notifyMemberAdded(MemberAddedNotificationRequestDto memberAddedNotificationRequestDto) {
        String message = String.format("%s님이 워크스페이스에 입장하셨습니다.. WorkSpace ID : %d", memberAddedNotificationRequestDto.getNickname(),
                memberAddedNotificationRequestDto.getWorkSpaceId()
        );
        discordSender.sendNotification(message);
    }
    

    
```

@Component
@AllArgsConstructor
public class DiscordNotificationSender implements NotificationSender {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String discordWebhookUrl =
            "https://discordapp.com/api/webhooks/1295553870671646791/ETFI6_Nw2-87wzm7iXl1-tG36OBOgXbO5fV1E6EwXXriNMGJ0ky92rajvpRfwCmA4PyC";
//    private static final Logger logger = LoggerFactory.getLogger(DiscordNotificationSender.class);



    public ApiResponse sendNotification(String message) {

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("content", message);
        String payload;
        try {
            // ObjectMapper를 사용하여 Map을 JSON 문자열로 변환
            payload = objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            return ApiResponse.createError("메시지 변환에 실패하였습니다.", 500);
        }
        // http 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // http 요청본문, 헤더를 포함한  httpEntity 생성
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.postForObject(discordWebhookUrl, request, String.class);
//            logger.info("알림이 성공적으로 전송되었습니다: {}", message);
            return ApiResponse.createSuccess("알림이 성공적으로 전송되었습니다.", 200, message);
        } catch (RestClientException e) {
//            logger.error("알림 전송 실패 : {}", e.getMessage());
            return ApiResponse.createError("알림 전송에 실패하였습니다.", 500);
        }
    }
}
```


### 설명 :
중요 이벤트에 대한 변경이 이루어진 서비스 로직에 알림을 호출하도록 로직을 통해서, NotificationService의 로직의 메서드를 통해 해당 URL로 실시간 알림이 전송되는 기능

</details>




----
#### 역할분담:
|                 이길환                  |                 홍정기                  |                   이민혁                    |                     황우석                      |                       이보성                        |
|:------------------------------------:|:------------------------------------:|:----------------------------------------:|:--------------------------------------------:|:------------------------------------------------:|
| 워크스페이스, 보드 | 첨부파일, 카드 | 인증/인가 & Auth,User | 리스트,댓글 | 알림,검색 |


</details>


----
#### 와이어프레임:https://www.figma.com/design/qdFt6G1oR034aak1aoa4r1/Untitled?node-id=0-1&node-type=canvas&t=53IaJlYrk9pQSZBo-0
![image](https://github.com/user-attachments/assets/1dc14442-9824-4f50-bec5-6f8420bce43a)
![image](https://github.com/user-attachments/assets/02e4dd06-f83a-4c13-9ef3-274e89f1eaa8)
![image](https://github.com/user-attachments/assets/9910e226-0296-4ad4-9c3b-7cb993a45f0a)

----

#### API 명세서 : https://www.notion.so/teamsparta/495a50a2203149bf95c2b557ab894925?v=e52e39d8056b401196f0339d53366bad&pvs=4


----
#### ERD : ![image](https://github.com/user-attachments/assets/49523ca7-c988-43b1-a4c8-1152e45018cc)



----
#### 📢 트러블슈팅:
***1. JSON 문자열 생성 방법 - 이스케이프, 가독성 향상***

   ![image](https://github.com/user-attachments/assets/c0c32fd2-6108-4bd5-a6a9-140b94ec9b70)
   
   위와 같은 방법으로 payload를 수동 생성할시 이스케이프 처리를 수동으로 해야 하므로 실수가 발생하기 떄문에,

   ![image](https://github.com/user-attachments/assets/a10368c6-777c-448c-b618-df1020c129da)

   ObjectMapper를 사용하여 이스케이프 처리가 자동으로 처리되도록 하였다.
   ObjectMapper를 사용하여 직렬화를 한다면, 코드의 안정성과 유지보수 측면에서 도움이 되고 오류 가능성을 줄일 수 있다.

#### QueryDSL을 사용하다 생긴 문제 : [WIKI보기](https://github.com/TrelloSoEasy/Project_TSE/wiki/queryDSL%EC%9D%84-%EC%9E%91%EC%84%B1%ED%95%98%EB%8B%A4%EA%B0%80-%EC%83%9D%EA%B8%B4%EB%AC%B8%EC%A0%9C)

----
#### ✔️ 소감과 아쉬웠던 점:
**이길환** : 역할 분담에 들어가기전 프로젝트의 상세 내용을 확인하고 예상해 역할의 분담을 더 잘했으면 조금 더 역할분배가 잘 되었을 것 같은데 아쉽습니다.

**홍정기** :

**이민혁** : 

**황우석** : 

**이보성** :








