package com.lux.ex089firebasefirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lux.ex089firebasefirestore.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Firebase Cloud Firestore Database - No-SQL 방식 - RDBMS 처럼 테이블 형식으로 저장되지 않는 DBMS

    //Firebase 와 앱을 연동

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSave.setOnClickListener(view -> clickSave());
        binding.btnLoad.setOnClickListener(view -> clickLoad());
        binding.btnRealtimeLoad.setOnClickListener(view -> clickRealTimeLoad());
        binding.btnSearch.setOnClickListener(view -> clickSearch());
    }

    void clickSave(){
        //저장할 데이터
        String name=binding.etName.getText().toString();
        int age=Integer.parseInt(binding.etAge.getText().toString());
        String address=binding.etAddress.getText().toString();

        //firebase DB 에 저장 [Map Collection 으로 저장]
        Map<String, Object> person=new HashMap<>();
        person.put("name",name);
        person.put("age",age);
        person.put("address",address);

        //Firebase Firestore 관리 객체 소환
        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
        
        //기존 DB의 테이블 이름처럼 사용되는 것이 Collection 이라는 용어
        //"people" 이라는 이름으로 collection 을 만들고 참조객체를 소환
        CollectionReference peopleRef=firebaseFirestore.collection("people");
        
        //Task task =peopleRef.document().set(person);    //자동으로 생성된 랜덤한 이름의 Document가 만들어지고 그 안에 person 데이터 저장
        //Task task=peopleRef.add(person); //위코드 줄여쓰기

//        task.addOnSuccessListener(new OnSuccessListener() {
//            @Override
//            public void onSuccess(Object o) {
//                Toast.makeText(MainActivity.this, "saved", Toast.LENGTH_SHORT).show();
//            }
//        });
        //add()를 사용하면 Document 명이 랜덤하게 만들어지고 DB에는 이름정렬 순으로 있기에
        //나중에 읽어올때 순서가 뒤집어 있을 수 있음.
        //만약 싫다면 직접 Document 명을 명시할 수 있음.

        //firebaseFirestore.collection("member").document("1").set(person);

        //날짜와 시간을 이용하여 순차적으로 저장되도록 할 수 있음.

        //System.currentTimeMillis(); : 1970년 1월 1일 0시 0분 0초 부터 1ms마다 카운트되는 값
        firebaseFirestore.collection("member").document(System.currentTimeMillis()+"").set(person);

        //날짜와 시간을 SimpleDateForemat() 객체 이용할 수도 있음.

        //저장할 데이터를 굳이 HashMap으로 만들지 않고
        //값들을 멤버로 가진 데이터 VO 객체를 한번에 set할 수 있음.
        PersonVO p=new PersonVO(name,age,address);
        firebaseFirestore.collection("user").add(p);

        //CollectionReference userRef=firebaseFirestore.collection("user");


    }
    void clickLoad(){

        binding.tv.setText("");


        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
        CollectionReference memberRef=firebaseFirestore.collection("member");

        Task<QuerySnapshot> task=memberRef.get();
        task.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot snapshots=task.getResult();
                    //결과 데이터가 여러개인 경우가 많음. (즉, Document 가 여러개)
                    for (DocumentSnapshot snapshot:snapshots){
                        //Document 별 로 그 순간의 데이터를 취득한 Document Snapshot 에서 필드값들 얻어오기
                        Map<String,Object> person =snapshot.getData();
                        String name=person.get("name").toString();
                        int age=Integer.parseInt(person.get("age").toString());
                        String address=person.get("address").toString();

                        binding.tv.append(name +","+age +","+address+"\n");
                    }
                }
            }
        });
    }
    void clickRealTimeLoad(){
        //"member" 컬렉션의 데이터 변화를 실시간 감지하기
        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
        CollectionReference memberRef=firebaseFirestore.collection("member");
        memberRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                StringBuffer buffer=new StringBuffer();
                for (DocumentSnapshot snapshot:value) {
                    Map<String, Object> person = snapshot.getData();
                    String name = person.get("name").toString();
                    String address = person.get("address").toString();
                    int age = Integer.parseInt(person.get("age").toString());

                    buffer.append(name + "," + age + "," + address + "\n");
                }
                binding.tv.setText(buffer.toString());
            }
        });
    }
    void clickSearch(){
        //"member" 컬렉션에서 특정 필드 값에 해당하는 데이터들만 가져오기
        String name=binding.etName.getText().toString();

        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
        CollectionReference memberRef=firebaseFirestore.collection("member");
        memberRef.whereEqualTo("name",name).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //같은 이름이 여러개일 수도 있기 때문에
                StringBuffer buffer=new StringBuffer();
                for (QueryDocumentSnapshot snapshot:value){
                    Map<String, Object> person=snapshot.getData();

                    String name=person.get("name").toString();
                    int age=Integer.parseInt(person.get("age").toString());
                    String address=person.get("address").toString();

                    buffer.append(name+","+age+","+address+"\n");
                }
                binding.tv.setText(buffer.toString());
            }
        });
    }
}