package com.example.lasbetalk

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.lasbetalk.model.Book
import com.example.lasbetalk.model.ChatModel
import com.example.lasbetalk.model.ChatModel.Comment
import com.example.lasbetalk.model.Friend
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageActivity : AppCompatActivity() {

    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomIsbn : String? = null
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val imageView = findViewById<ImageView>(R.id.messageActivity_ImageView)
        val editText = findViewById<TextView>(R.id.messageActivity_editText)

        // 메세지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        // 토론방 ISBN 초기화
        chatRoomIsbn = intent.getStringExtra("chatroomISBN")
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.messageActivity_recyclerview)

        recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
        recyclerView?.adapter = RecyclerViewAdapter()


        // 전송버튼을 눌렀을 때
        imageView.setOnClickListener{
            messageActivity_ImageView.isEnabled = true

            // 토론방 users에 내 uid 추가
            fireDatabase.child("chatrooms").child(chatRoomIsbn.toString()).child("users").child(uid.toString()).setValue(true)

            val comment = Comment(uid, editText.text.toString(), curTime)

            // 내 comment 추가하기
            fireDatabase.child("chatrooms").child(chatRoomIsbn.toString()).child("comments").push().setValue(comment)
            messageActivity_editText.text = null
            messageActivity_ImageView.isEnabled = true

            // 토론방 내용 업데이트
            recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
            recyclerView?.adapter = RecyclerViewAdapter()
        }
    }

    // 리사이클러뷰 어뎁터
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>()
    {
        private val comments = ArrayList<Comment>()
        private var friend : Friend? = null
        private var book : Book? = null

        init{
            // 토론방의 책 정보를 받아오기
            fireDatabase.child("BOOK").child("BESTSELLER").child(chatRoomIsbn.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Log.d("book","fail")
                }
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    // 토론방 상단에 책 title 출력
                    book = snapshot.getValue<Book>()
                    messageActivity_textView_topName.text = book?.title
                    Log.d("book","success")
                    getMessageList()
                }
            })
        }

        // 메세지 리스트 받아오기
        fun getMessageList(){
            fireDatabase.child("chatrooms").child(chatRoomIsbn.toString()).child("comments").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError)
                {
                    Log.d("book","fail")
                }
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    comments.clear()
                    for(data in snapshot.children)
                    {
                        // Comment 객체를 받와와서 comments 배열에 저장
                        val item = data.getValue<Comment>()
                        comments.add(item!!)
                        println(comments)
                    }
                    notifyDataSetChanged()

                    //메세지를 보낼 시 화면을 맨 밑으로 내림
                    recyclerView?.scrollToPosition(comments.size - 1)
                    Log.d("book","success")
                }
            })
        }

        // 뷰 홀더 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        // 뷰 홀더 속성
        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int)
        {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time

            // 본인 채팅
            if(comments[position].uid.equals(uid))
            {
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
            }
            // 상대방 채팅
            else
            {
                fireDatabase.child("users").child(comments[position].uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("viewww","fail")
                    }
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        // comment를 보낸 상대방 정보 friend에 저장
                        friend = snapshot.getValue<Friend>()
                        Log.d("viewww","success")
                    }
                })
                // friend의 프로필 사진 및 기타 정보 출력
                GlideApp.with(holder.itemView.context)
                        .load(friend?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(holder.imageView_profile)
                holder.textView_name.text = friend?.name
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT
            }
        }

        // 메세지 뷰 홀더 속성
        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView_message: TextView = view.findViewById(R.id.messageItem_textView_message)
            val textView_name: TextView = view.findViewById(R.id.messageItem_textview_name)
            val imageView_profile: ImageView = view.findViewById(R.id.messageItem_imageview_profile)
            val layout_destination: LinearLayout = view.findViewById(R.id.messageItem_layout_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time : TextView = view.findViewById(R.id.messageItem_textView_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }
}