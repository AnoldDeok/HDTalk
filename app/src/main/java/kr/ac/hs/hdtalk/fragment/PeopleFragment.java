package kr.ac.hs.hdtalk.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import kr.ac.hs.hdtalk.R;
import kr.ac.hs.hdtalk.model.UserModel;

/**
 * Created by dksgh on 2018-01-13.
 */

//친구목록 프래그먼트
public class PeopleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //친구목록 레이아웃 포함하는 뷰
        //fragment_peolple 레이아웃을 팽창시킨다 inflate는 부불게하다란 뜻
        View view = inflater.inflate(R.layout.fragment_people,container,false);
        RecyclerView  recyclerView = view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

        return view;
    }

        // 친구목록을 리사이클로 나타냄
    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        //UserModel클래스를 이용해서 userModels이라는 리스트객체를 만듬 이름,이미지주소,패스워드를 포함하고있음
        List<UserModel> userModels;

        //DB에 접속해야해서 퍼블릭으로 컨스트럭터 하나 만들어줌
        public PeopleFragmentRecyclerViewAdapter(){
            userModels = new ArrayList<>();
            //데이터베이스에 users태그 참조해서 값을 더해주는 이벤트리스너를 연결
            FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                @Override
                //서버에서 넘어오는 데이터를 뜻함
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //친구가 추가되면 onDataChange가 불러지는데 clear가 없으면 123이던게 1231234로 쌓일수있음
                    userModels.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        userModels.add(snapshot.getValue(UserModel.class));
                    }

                    //데이터를 업데이트한뒤 새로고침를 해줘야해서 붙임
                    notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        //리사이클뷰 안에 들어가는 아이템들 로드해주는부분
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            //이미지부분 넣어주는 부분
            Glide.with
                    (holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUri)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)holder).imageView);
            //텍스트부분 넣어주는 부분
            ((CustomViewHolder)holder).textView.setText(userModels.get(position).userName);
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }
            private class CustomViewHolder extends RecyclerView.ViewHolder {
            //itme_friend 레이아웃
            public ImageView imageView;
            public TextView textView;
                public CustomViewHolder(View view) {
                    super(view);
                    imageView = view.findViewById(R.id.frienditem_imageview);
                    textView = view.findViewById(R.id.frienditem_textview);
                }
            }
        }
}
