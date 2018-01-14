package kr.ac.hs.hdtalk;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private Button login;
    private Button signup;
    private EditText id;
    private EditText password;
    //FirebaseAuth가 로그인 관리해줌
    private FirebaseAuth firebaseAuth;
    //로그인 시도하면 발생하는 리스너
    private FirebaseAuth.AuthStateListener authStateListener;
    //Firebase에서 원격으로 조정받기 위해서 필요함 = 모바일 파이어베이스 원격구성
    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        //로그아웃
        firebaseAuth.signOut();

      /*  FirebaseRemoteConfig의 진입 점. 호출자는 먼저 싱글 톤 객체를 사용 getInstance()하고 그 싱글 톤 객체에 대한 연산을 호출해야합니다.
   싱글 톤 객체는 액티브 구성 (Active Config) 및 기본 구성 (Default Config)을 포함하여 응용 프로그램에서 사용할 수있는 전체 원격 구성 매개 변수 값을 포함합니다.
    */
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString("splash_background");

        //디바이스 맨위에 상태표시 바 색상 변경함 ... setStatusBarColor가 롤리팝 버전부터 사용가능해서 이렇게됨
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        id = findViewById(R.id.loginActivity_edittext_id);
        password = findViewById(R.id.loginActivity_edittext_password);
        login = findViewById(R.id.loginActivity_button_login);
        signup = findViewById(R.id.loginActivity_button_signup);

        //버튼색 변경을 이렇게 하는이유는 Firebase를 이용해서 원격으로 배경과 버튼색까지 한번에 바꿀 수 있는 장점이 있기때문
        login.setBackgroundColor(Color.parseColor(splash_background));
        signup.setBackgroundColor(Color.parseColor(splash_background));

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEvent();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(a);
            }
        });

        //로그인 인터페이스 리스너
        //만들었으면 로그인이 됬는지 확인해주는애라서 액티비티에 붙여줘야함
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            //상태가 바꼈을때 넘어가는거임 예를 들어, 로그인이 됬거나 로그아웃 됬거나
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    //로그아웃
                }

            }
        };
    }
    //db안에 아이디랑 패스워드 비교해줌
    void loginEvent() {
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //로그인 실패시 작동
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //시작할때 로그인리스너 붙여주고
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //멈추면 다시 로그인리스너 때줌
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
