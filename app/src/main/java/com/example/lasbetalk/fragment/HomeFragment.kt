package com.example.lasbetalk.fragment

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.lasbetalk.MessageActivity
import com.example.lasbetalk.model.Friend
import com.example.lasbetalk.R
import com.example.lasbetalk.model.Book
import com.example.lasbetalk.model.ChatModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_message.*

class HomeFragment : Fragment() {
    companion object{
        fun newInstance() : HomeFragment {
            return HomeFragment()
        }
    }

    private lateinit var database: DatabaseReference
    private var friend : ArrayList<Friend> = arrayListOf()
    private var books : ArrayList<Book> = arrayListOf()

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
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        database = Firebase.database.reference
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler)

        // view 업데이트??
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()

        return view
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>()
    {
        init {
            // 랭크 정보 받아오기 (※여기서 날짜 조정 해야함)
            FirebaseDatabase.getInstance().reference.child("RANK/1208").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    books.clear()

                    // rank에서 책 정보 받아오기
                    for(data in snapshot.children)
                    {
                        val item = data.getValue<Book>()
                        books.add(item!!)
                    }
                    notifyDataSetChanged()
                }
            })
        }

        // 뷰 홀더 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_home, parent, false))
        }

        // 뷰 홀더 속성
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.home_item_iv)
            val textView : TextView = itemView.findViewById(R.id.home_item_tv)
            val textViewEmail : TextView = itemView.findViewById(R.id.home_item_email)
        }

        // 뷰 홀더 내용
        override fun onBindViewHolder(holder: CustomViewHolder, position: Int)
        {
            // 책 사진 출력
            Glide.with(holder.itemView.context).load(books[position].image)
                    .apply(RequestOptions().circleCrop())
                    .into(holder.imageView)

            // 책 타이틀
            holder.textView.text = books[position].title

            // 작가 목록
            holder.textViewEmail.text = books[position].author

            // 클릭 시 bookISBN을 담아서 MessageActivity로 넘어감
            holder.itemView.setOnClickListener{
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("chatroomISBN", books[position].ISBN)
                context?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return books.size
        }
    }
}