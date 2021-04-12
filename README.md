# CommentActivity
사용자가 댓글을 작성하고, 댓글 목록을 볼 수 있는 댓글 기능입니다. </br>
Android에서 공식적으로 지원하는 Volley 라이브러리를 사용하여 서버와의 통신을 구현했습니다.</br></br></br>

## 주 기능</br>

### 댓글 로드</br>
* Fragment가 화면에 띄워질 때, 최근에 작성된 3개의 댓글을 확인할 수 있습니다.</br></br>
<img src="./Image/image1.jpg" width="400px" height="600px" title="img" alt="img"></img></br>

* '+  n개의 댓글 더 보기' 버튼을 클릭할 때마다 n개의 댓글을 추가로 볼 수 있습니다.(n <= 3)</br></br>
<img src="./Image/image2.jpg" width="350px" height="600px" title="img" alt="img"></img>
<img src="./Image/image3.jpg" width="350px" height="600px" title="img" alt="img"></img></br>

* '새로 고침' 버튼을 클릭하면, 댓글이 갱신됩니다.</br></br>
<img src="./Image/image6.jpg" width="400px" height="600px" title="img" alt="img"></img></br>

### 댓글 등록</br>
* 새로운 댓글을 등록하고 확인할 수 있습니다.</br></br>
<img src="./Image/image4.jpg" width="350px" height="600px" title="img" alt="img"></img>
<img src="./Image/image5.jpg" width="350px" height="600px" title="img" alt="img"></img></br></br></br></br></br>


## 전체 설계도
Volley를 사용해 네트워크 작업을 실행하고 RecyclerView를 갱신하는 전체적인 동작 과정을 나타내는 블록 다이어그램은 다음과 같습니다.
1. Main Thread에서 StringRequest 객체를 생성하여 RequestQueue에 전달한다.</br>
2. RequestQueue에서 Thread를 실행시켜 서버에 요청한다.</br>
3. 받은 응답을 다시 Main Thread로 전달한다.</br>
통신을 수행하는 함수는 sendPOSTJournal_idRequest, sendPOSTComment_idRequest, sendPOSTUser_idRequest, sendPOSTCommentRequest로 총 4개로 구성되어 있습니다.
<img src="./Image/Architecture.jpg" title="img" alt="img"></img></br></br>


### sendPOSTJournal_idRequest 함수 동작 과정
<img src="./Image/request1.png" title="img" alt="img"></img>

### sendPOSTComment_idRequest 함수 동작 과정
<img src="./Image/request2.png" title="img" alt="img"></img>

### sendPOSTUser_idRequest 함수 동작 과정
<img src="./Image/request3.png" title="img" alt="img"></img>

### sendPOSTCommentRequest로 함수 동작 과정
<img src="./Image/request4.png" title="img" alt="img"></img>



