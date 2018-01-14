package kr.ac.hs.hdtalk;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import kr.ac.hs.hdtalk.model.UserModel;

public class SignupActivity extends AppCompatActivity {

    private EditText email;
    private EditText name;
    private EditText password;
    private Button signup;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private ImageView profile;
    private Uri imageUri;
    public static final int PICK_FROM_ALBUM = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = mFirebaseRemoteConfig.getString("splash_background");

        //디바이스 맨위에 상태표시 바 색상 변경함 ... setStatusBarColor가 롤리팝 버전부터 사용가능해서 이렇게됨
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        profile = (ImageView) findViewById(R.id.signupActivity_imageview_profile);
        email = (EditText) findViewById(R.id.signupActivity_edittext_email);
        name = (EditText) findViewById(R.id.signupActivity_edittext_name);
        password = (EditText) findViewById(R.id.signupActivity_edittext_password);
        signup = (Button) findViewById(R.id.signupActivity_button_signup);

        profile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,PICK_FROM_ALBUM);
            }
        });

        //버튼색 변경 , 리스너추가
        signup.setBackgroundColor(Color.parseColor(splash_background));
        signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                //빈칸이면 실행되지않게 만듬
                /*이거 실행이안됌
                if(email.getText().toString() == null || name.getText().toString() == null || password.getText().toString() == null){
                    Toast.makeText(getApplicationContext(),"빈 칸 없이 모두 입력해주세요",Toast.LENGTH_LONG).show();
                    return;
                }*/

                //사진 안넣으면 진행불가
                if(null == imageUri){
                    Toast.makeText(getApplicationContext(),"이미지를 넣어주세요",Toast.LENGTH_LONG).show();
                    return;
                }

                //회원가입할때 패스워드 길이가 6이상이여야 오류가 안나더라
                //패스워드 글자제한
                if(password.length() < 6){
                    Toast.makeText(getApplicationContext(),"패스워드는 6자 이상입니다.",Toast.LENGTH_LONG).show();
                    return;
                }
                    //사용자입증 정보를 만드는과정이다. new에서는 아예 새로운 객체를 만들지만, getInstance는 객체를 하나만 생성함.
                    FirebaseAuth.getInstance()
                            //이메일과 비밀번호를 받아서 만듬
                            .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            //완료가 되면 이벤트리스너를 통해서 온컴플리트로 넘어감
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    //입증정보 표 결과에서 유저정보와 주민번호와 같은 uid를 얻음
                                    final String uid = task.getResult().getUser().getUid();

                                    //addOnCompleteListener 는 성공여부를 알게하는 리스너임
                                    FirebaseStorage.getInstance().getReference().child("userImages").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            String ImageUri = task.getResult().getDownloadUrl().toString();

                                            //유저모델 객체를 만듬
                                            UserModel userModel = new UserModel();
                                            userModel.userName = name.getText().toString();
                                            userModel.profileImageUri = ImageUri;
                                            userModel.userPassword = password.getText().toString();
                                            //데이터베이스안에 users목록안에 uid목록안에 유저이름을 저장한다.
                                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //DB에 저장이 성공하면 끝내기
                                                    Toast.makeText(SignupActivity.this, "회원가입 성공!", Toast.LENGTH_LONG).show();
                                                    SignupActivity.this.finish();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
            }}
        );
    }
    @Override
    protected void  onActivityResult(int requestCode, int resultCode , Intent data){
        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){
            profile.setImageURI(data.getData()); // 가운데 뷰를 바꿈
            imageUri = data.getData(); // 이미지 원본 경로
        }

    }
}
