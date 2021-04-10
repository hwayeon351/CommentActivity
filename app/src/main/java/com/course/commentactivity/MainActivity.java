package com.course.commentactivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button more_btn, save_btn, reload_btn;
    RecyclerView comment_rv;
    EditText comment_et;
    TextView textView;

    ArrayList<CommentAdapter.CommentItem> all_comment_data = new ArrayList<>();
    CommentAdapter commentAdapter;
    InputMethodManager inputMethodManager;

    String journal_id = "1";
    String user_id = "6"; // user_nickname = 화요밍

    //journal_id = n인 저널에 해당하는 모든 데이터를 JSON 형식으로 응답하는 API -> 여기서 해당 저널(해당 프로젝트에서 임의 저널 아이디 = 1)에 남겨진 모든 댓글 아이디들을 JSON Parsing하여 받아올 수 있다.(comments)
    String journal_id_request_url = "http://211.174.237.197/request_journal_info_by_id/";
    //모든 댓글 아이디들을 저장하는 변수
    String[] journalCommentIds;
    //전체 응답 댓글 수 카운팅하는 변수 -> sendPOSTComment_idRequest 함수에서 카운팅 진행
    int all_cmt = 0;

    //comment_id = n인 댓글 데이터를 응답하는 API -> user_id(작성자 아이디), comment(댓글 내용), date(댓글 작성 시각)를 JSON Parsing하여 받아올 수 있다.
    //sendPOSTComment_idRequest 함수의 매개변수
    String comment_id_request_url = "http://211.174.237.197/request_comment_info_by_id/";

    //user_id = n인 유저 정보 데이터를 응답하는 API -> user_nickname(유저 닉네임, 해당 프로젝트에서 임의 사용자 닉네임 = "화요밍")을 JSON Parsing하여 받아올 수 있다.
    //sendPOSTComment_idRequest 함수의 매개변수
    String user_id_request_url = "http://211.174.237.197/request_user_pic_nickname_by_id/";

    //새로운 댓글 데이터를 전송하는 API -> user_id(유저 아이디, 해당 프로젝트에서 임의 사용자 아이디 = 6), comment(댓글 내용), date(현재 시각)을 JSON 형식으로 파라미터에 담아 요청한다.
    //sendPOSTCommentRequest 함수의 매개변수
    String send_comment_url = "http://211.174.237.197/request_save_comment/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.textView);
        more_btn = (Button)findViewById(R.id.more_comment_btn);
        save_btn = (Button)findViewById(R.id.comment_save_btn);
        reload_btn = (Button)findViewById(R.id.reload_btn);
        comment_rv = (RecyclerView)findViewById(R.id.comment_rv);
        comment_et = (EditText)findViewById(R.id.write_comment_et);
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        commentAdapter = new CommentAdapter(all_comment_data);
        comment_rv.setAdapter(commentAdapter);
        comment_rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        //메인 Activity가 메모리에서 만들어질 때, RequestQueue를 하나만 생성한다.
        if(AppHelper.requestQueue == null){
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        //1. 댓글 데이터를 서버에 요청하여 로드하고 RecyclerView를 갱신한다.
        sendPOSTJournal_idRequest(journal_id_request_url, journal_id);

        //2. 댓글 더보기 버튼 클릭하면 visible_cmt(RecyclerView에 출력되는 아이템 개수)를 증가시켜 RecyclerView를 갱신한다.
        more_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "+  n개의 댓글 더보기" 버튼 텍스트에서 n을 파싱하여 그만큼 visible_cmt를 증가시킨다.
                String btn_text = (String)((Button)more_btn).getText();
                btn_text = btn_text.replace("+  ", "");
                btn_text = btn_text.substring(0, 1);
                int visible_cmt = Integer.parseInt(btn_text) + commentAdapter.getVisible_cmt();
                commentAdapter.setVisible_cmt(visible_cmt);
                commentAdapter.notifyDataSetChanged();

                //댓글 더보기 버튼을 업데이트한다.
                updateMore_btn();
            }
        });

        //3. 새로운 댓글이 등록되면 서버에 데이터를 전송하고 RecyclerView를 갱신한다.
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //댓글 작성 여부를 확인한다.
                if(comment_et.getText().toString().equals("")) Toast.makeText(getApplicationContext(), "댓글을 작성해 주세요.", Toast.LENGTH_SHORT).show();
                else{
                    //등록 버튼이 눌린 시각을 반영하여 댓글을 서버에 등록한다.
                    SimpleDateFormat formatter = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss", Locale.KOREA );
                    Date date = Calendar.getInstance().getTime();
                    String now_date = formatter.format (date);

                    //댓글 등록 요청
                    sendPOSTCommentRequest(send_comment_url, user_id, comment_et.getText().toString(), now_date);
                }
            }
        });

        //4. 새로고침 버튼이 클릭되면 서버에 전체 댓글 데이터를 요청하여 RecyclerView를 다시 갱신해준다.
        reload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPOSTJournal_idRequest(journal_id_request_url, journal_id);
            }
        });

    }

    //sendPOSTJournal_idRequest 함수
    //1. Journal_id = journal_id인 서버에 저장된 data를 요청한다.
    // 1.1 해당 저널(해당 프로젝트에서 임의 저널 아이디 = 1)에 남겨진 모든 댓글 아이디들을 JSON Parsing하여 받아온다. -> comments
    //    1.1.1 sendPOSTComment_idRequest -> comment_id를 전달하여 각각의 댓글 데이터를 받아온다.
    //      1.1.1.1 해당 댓글의 데이터를 JSON Parsing하여 받아온다. -> comment, comment_date, user_id
    //      1.1.1.2 sendPOSTUser_idRequest -> user_id를 전달하여 댓글을 작성한 유저의 정보를 받아온다.
    //        1.1.1.2.1 해당 유저의 데이터를 JSON Parsing하여 받아온다. -> nickname
    //        1.1.1.2.2 all_comment_data에 댓글 데이터(nickname, comment, comment_date)를 넣어준다.
    public void sendPOSTJournal_idRequest(String url, String journal_id) {
        //전체 댓글 데이터를 초기화한다.
        all_comment_data.clear();
        all_cmt = 0;

        //StringRequest
        //매개변수: action(GET, POST), URL, 응답 성공 리스너, 응답 실패 리스너
        //위의 4개의 파라미터를 포함한 StringRequest를 객체를 생성한다.(RequestQueue에 넣어 줄 Request)
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                //Response.Listener 성공적으로 응답을 받으면 실행된다. -> 비동기
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String all_journal_data_response) {
                        Log.d("Journal_idRequest", "all_journal_data_result = " + all_journal_data_response);
                        //setTextView(all_journal_data_response + "\n");

                        String journalCommentIdResult = "";
                        try {
                            //Journal_id = journal_id의 모든 comment_id를 가진 jouranl_comments를 파싱한다.
                            journalCommentIdResult = new JSONObject(all_journal_data_response).getJSONObject("journal").getString("journal_comments");
                            Log.d("Journal_idRequest", "journalCommentIdResult = " + journalCommentIdResult);
                            journalCommentIds = journalCommentIdResult.split(", ");

                            for(int i=0; i < journalCommentIds.length; i++){
                                //sendPOSTComment_idRequest에 각각의 comment_id를 전달하여 Comment_id = comment_id의 데이터를 요청한다.
                                Log.d("Journal_idRequest", "journalCommentIds " + i + journalCommentIds[i]);
                                sendPOSTComment_idRequest(comment_id_request_url, journalCommentIds[i]);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                //Response.ErrorListener 응답을 받아오는데 에러가 발생하면 실행된다. -> 비동기
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("CommentRequest", "Error = " + error.toString());

                    }
                })
        {
            //POST 요청 파라미터를 넣어준다. -> journal_id
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("journal_id", journal_id);
                return params;
            }
        };

        //Volley는 내부에서 캐싱을 해주므로, 이전에 같은 요청이 있었을 경우, 그대로 받아오게 된다.
        //요청 때마다 새로운 응답을 받아오기 위해 false로 설정한다.
        stringRequest.setShouldCache(false);

        //RequestQueue에 stringRequest를 넣어준다. -> 이때가 요청을 보낸 시점이 된다.
        AppHelper.requestQueue.add(stringRequest);
        //setTextView("Journal_idRequest 요청 보냄\n");
    }

    //sendPOSTComment_idRequest
    //    1.1.1 sendPOSTComment_idRequest -> comment_id를 전달하여 각각의 댓글 데이터를 받아온다.
    //      1.1.1.1 해당 댓글의 데이터를 JSON Parsing하여 받아온다. -> comment, comment_date, user_id
    //      1.1.1.2 sendPOSTUser_idRequest -> user_id를 전달하여 댓글을 작성한 유저의 정보를 받아온다.
    //        1.1.1.2.1 해당 유저의 데이터를 JSON Parsing하여 받아온다. -> nickname
    //        1.1.1.2.2 all_comment_data에 댓글 데이터(nickname, comment, comment_date)를 넣어준다.
    public void sendPOSTComment_idRequest(String url, String comment_id){
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String all_comment_data_response) {
                        //Comment_id = comment_id인 데이터가 없는 경우 서버에서 "-1"을 response한다.
                        //"-1"이 아닌 경우 댓글 데이터를 Json 파싱하여 받아오고 해당 댓글을 남긴 유저 정보를 요청한다.
                        if(!all_comment_data_response.equals("-1")){
                            //댓글이 성공적으로 불러와지면 카운팅한다.
                            all_cmt++;
                            Log.d("Comment_idRequest", all_comment_data_response);

                            try {
                                //setTextView("comment_id = " + comment_id + " " + all_comment_data_response + "\n");
                                // 댓글 데이터를 파싱한다.
                                JSONObject comment_object = new JSONObject(all_comment_data_response).getJSONObject("comment");
                                String comment = comment_object.getString("comment");
                                String user_id = comment_object.getString("user_id");
                                String comment_date = comment_object.getString("comment_date");
                                String user_pic = "";
                                String user_name = "";
                                String user_img_path = "";
                                Log.d("Comment_idRequest", "comment_id = " + comment_id + " comment = " + comment + " comment_date =  " + comment_date  + " user_id = " + user_id);

                                //sendPOSTUser_idRequest -> 유저 정보 요청
                                //sendPOSTUser_idRequest에서 댓글을 최종적으로 all_comment_data(RecyclerView에 적용할 댓글 데이터)에 넣어주기 위해 comment, comment_date를 매개변수로 보내준다.
                                sendPOSTUser_idRequest(user_id_request_url, user_id, comment, comment_date);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Comment_idRequest", error.toString());
                    }
                })
        {
            //POST 요청 파라미터를 넣어준다. -> comment_id
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("comment_id", comment_id);
                return params;
            }
        };
        Log.d("Comment_idRequest", "comment_id = " + comment_id);

        //Volley는 내부에서 캐싱을 해주므로, 이전에 같은 요청이 있었을 경우, 그대로 받아오게 된다.
        //요청 때마다 새로운 응답을 받아오기 위해 false로 설정한다.
        stringRequest.setShouldCache(false);

        //RequestQueue에 stringRequest를 넣어준다. -> 이때가 요청을 보낸 시점이 된다.
        AppHelper.requestQueue.add(stringRequest);
        //setTextView("Comment_id = " + comment_id + " 요청 보냄\n");
    }

    //sendPOSTUser_idRequest
    //        1.1.1.2.1 해당 유저의 데이터를 JSON Parsing하여 받아온다. -> nickname
    //        1.1.1.2.2 all_comment_data에 댓글 데이터(nickname, comment, comment_date)를 넣어준다.
    //                  전체 댓글 데이터 중 마지막 댓글 데이터인 경우(모든 댓글 데이터를 받아온 경우) RecyclerView를 갱신한다.
    public void sendPOSTUser_idRequest(String url, String user_id, String comment, String comment_date){
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //User_id = user_id인 데이터가 없는 경우 서버에서 "-1"을 response한다.
                        //"-1"이 아닌 경우 Json 파싱하여 nickname을 받아온다.
                        if(!response.equals("-1")){
                            Log.d("User_idRequest", "user_id = " + user_id + "response = " + response);
                            //setTextView("user_id = " + user_id + " " + response + " comment = " + comment + " comment_date = " + comment_date + "\n");
                            try {
                                //유저 정보를 파싱한다.
                                JSONObject jsonObject = new JSONObject(response).getJSONObject("user");
                                String user_name = jsonObject.getString("nickname");
                                Log.d("User_idRequest", "user_id = " + user_id + " user_name = " + user_name );

                                //하나의 댓글 정보를 모두 갖췄으므로 all_comment_data에 넣어준다.
                                all_comment_data.add(new CommentAdapter.CommentItem(user_name, comment_date, comment));

                                Log.d("User_idRequest", "all_comment_data = " + all_comment_data.size());
                                Log.d("User_idRequest", "journal_comments_id length = " + journalCommentIds.length);

                                //전체 댓글 수와 all_comment_data.size()가 같으면 모든 댓글 데이터를 불러온 것이므로 RecyclerView를 갱신한다.
                                if(all_cmt == all_comment_data.size()){
                                    Log.d("User_idRequest", "Done to get All comments data !! | all_cmt = " + all_cmt + " all_comment_data = " + all_comment_data.size());
                                    //맨 처음 RecyclerView에 보여지는 항목은 3개로 설정한다.
                                    commentAdapter.setVisible_cmt(3);

                                    //전체 댓글 데이터를 최신순으로 정렬한다.
                                    all_comment_data = commentSort(all_comment_data);

                                    //RecyclerView를 갱신한다.
                                    commentAdapter.notifyDataSetChanged();

                                    //댓글 더보기 버튼을 업데이트한다.
                                    updateMore_btn();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("User_idRequest", error.toString());
                    }
                })
        {
            //POST 요청 파라미터를 넣어준다. -> user_id
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("user_id", user_id);
                return params;
            }
        };
        //Volley는 내부에서 캐싱을 해주므로, 이전에 같은 요청이 있었을 경우, 그대로 받아오게 된다.
        //요청 때마다 새로운 응답을 받아오기 위해 false로 설정한다.
        stringRequest.setShouldCache(false);

        //RequestQueue에 stringRequest를 넣어준다. -> 이때가 요청을 보낸 시점이 된다.
        AppHelper.requestQueue.add(stringRequest);
        //setTextView("user_id = " + user_id + " 요청 보냄\n");
    }

    //sendPOSTCommentRequest - 새로운 댓글 데이터를 요청에 담아 서버에 전송한다.
    public void sendPOSTCommentRequest(String url, String user_id, String comment, String comment_date){
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("CommentRequest", "response = " + response);
                        //댓글 데이터 전송에 오류가 있는 경우 서버에서 "-1"을 response한다.
                        //"-1"이 아닌 경우 새 댓글 데이터를 all_comment_data에 넣어주고 RecyclerView를 갱신한다.
                        if(!response.equals("1")){
                            Toast.makeText(getApplicationContext(), "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();

                            //EditText를 초기화하고 키보드를 닫아준다.
                            comment_et.setText("");
                            inputMethodManager.hideSoftInputFromWindow(comment_et.getWindowToken(), 0);

                            //전송한 새 댓글 데이터를 all_comment_data에 넣어주고 최신순으로 정렬한 후, RecyclerView를 갱신한다.
                            all_comment_data.add(new CommentAdapter.CommentItem("화요밍", comment_date, comment));
                            all_comment_data = commentSort(all_comment_data);
                            commentAdapter.notifyDataSetChanged();

                            //댓글 더보기 버튼을 업데이트한다.
                            updateMore_btn();
                        }
                        else Log.d("CommentRequest", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("CommentRequest", error.toString());
                    }
                })
        {
            //POST 요청 파라미터를 넣어준다. -> user_id, comment_date, comment, comment_source, comment_source_id
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("user_id", user_id);
                params.put("comment_date", comment_date);
                params.put("comment", comment);
                params.put("comment_source", "journal");
                params.put("comment_source_id", journal_id);
                return params;
            }
        };
        //Volley는 내부에서 캐싱을 해주므로, 이전에 같은 요청이 있었을 경우, 그대로 받아오게 된다.
        //요청 때마다 새로운 응답을 받아오기 위해 false로 설정한다.
        stringRequest.setShouldCache(false);

        //RequestQueue에 stringRequest를 넣어준다. -> 이때가 요청을 보낸 시점이 된다.
        AppHelper.requestQueue.add(stringRequest);
        //setTextView("user_id = " + user_id + " comment_date = " + comment_date + " comment = " + comment + " 요청 보냄\n");
    }

    //최신 순으로 댓글을 정렬하는 함수
    public ArrayList<CommentAdapter.CommentItem> commentSort(ArrayList<CommentAdapter.CommentItem> comments){
        ArrayList<CommentAdapter.CommentItem> comment_list = comments;

        Collections.sort(comment_list, new Comparator<CommentAdapter.CommentItem>() {
            @Override
            public int compare(CommentAdapter.CommentItem o1, CommentAdapter.CommentItem o2) {
                return o2.getDate().compareTo(o1.getDate());
            }
        });

        return comment_list;
    }

    //더보기 가능한 댓글 수를 파악하여 댓글 더보기 버튼을 업데이트하는 함수
    public void updateMore_btn(){
        int remain_cmt = commentAdapter.getDataSize() - commentAdapter.getVisible_cmt();
        if(remain_cmt >= 3){
            more_btn.setText("+  3개의 댓글 더보기");
            more_btn.setVisibility(View.VISIBLE);
        }
        else if(remain_cmt > 0){
            more_btn.setText("+  " + remain_cmt + "개의 댓글 더보기");
            more_btn.setVisibility(View.VISIBLE);
        }
        else more_btn.setVisibility(View.GONE);
    }

    //전체적인 요청 흐름을 살펴보기 위한 출력 코드
    public void setTextView(String response){
        textView.append(response);
    }
}

