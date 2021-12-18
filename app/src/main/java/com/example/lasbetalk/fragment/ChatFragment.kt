package com.example.lasbetalk.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.lasbetalk.GlideApp
import com.example.lasbetalk.MessageActivity
import com.example.lasbetalk.R
import com.example.lasbetalk.model.Book
import com.example.lasbetalk.model.ChatModel
import com.example.lasbetalk.model.Friend
import com.google.common.collect.Iterables.size
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_registration.*
import java.nio.file.Files.size
import java.util.*
import java.util.Collections.reverse
import java.util.Collections.reverseOrder
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatFragment : Fragment() {
    companion object{
        fun newInstance() : ChatFragment {
            return ChatFragment()
        }
    }
    private val fireDatabase = FirebaseDatabase.getInstance().reference

    //메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //프레그먼트를 포함하고 있는 액티비티에 붙었을 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.chatfragment_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()

        return view
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        private var uid : String? = null

        // 토론방의 bookISBN 값을 저장할 books 배열 선언
        private val books : ArrayList<String> = arrayListOf()

        init {
            // 사용자 id
            uid = Firebase.auth.currentUser?.uid.toString()
            println(uid)

            // 사용자가 참여한 토론방의 key인 ISBN을 찾아서 books 배열에 저장
            fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    books.clear()
                    for(data in snapshot.children){
                        // books 스트링 배열에 ISBN 추가
                        books.add(data.key!!)
                        println(data)
                    }
                    notifyDataSetChanged()
                }
            })
        }
        
        // 뷰 홀더 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false))
        }

        // 뷰 홀더 속성
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.chat_item_imageview)
            val textView_title : TextView = itemView.findViewById(R.id.chat_textview_title)
            val textView_lastMessage : TextView = itemView.findViewById(R.id.chat_item_textview_lastmessage)
        }

        // 뷰 홀더에 들어갈 내용
        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            lateinit var chatModel: ChatModel

            // 현재 선택한 position의 bookISBN을 저장함.
            val bookISBN: String = books[position]

            // bookISBN을 통해 책 정보를 가지고 옴
            fireDatabase.child("BOOK").child("BESTSELLER").child(bookISBN).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    val book = snapshot.getValue<Book>()

                    // 토론방 이미지 출력
                    GlideApp.with(holder.itemView.context).load(book?.image)
                            .apply(RequestOptions().circleCrop())
                            .into(holder.imageView)
                    
                    // 토론방 이름(책 타이틀) 출력
                    holder.textView_title.text = book?.title
                }
            })

            // 토론방의 마지막 메세지를 표시하는 부분
            fireDatabase.child("chatrooms").child(bookISBN).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.d("zzzzzzzzzzzzzzzzz", "faild")
                }
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    // 해당 채팅방의 chatModel 가져오기
                    for(data in snapshot.children){
                        chatModel = snapshot.getValue<ChatModel>()!!
                        println(chatModel)
                    }

                    // 메세지 내림차순 정렬 후 마지막 메세지의 키값을 가져옴
                    val commentMap = TreeMap<String, ChatModel.Comment>(reverseOrder())
                    commentMap.putAll(chatModel.comments)
                    val lastMessageKey = commentMap.keys.toTypedArray()[0]
                    holder.textView_lastMessage.text = chatModel.comments[lastMessageKey]?.message
                }
            })


            // 토론방 선택시 ISBN 값을 넘기면서 MessageActivity로 이동
            holder.itemView.setOnClickListener {
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("chatroomISBN", books[position])
                context?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return books.size
        }
    }
}