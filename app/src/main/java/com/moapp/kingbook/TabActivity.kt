package com.moapp.kingbook

// firebase func import


//import com.google.firebase.referencecode.database.kotlin.models.Post
//import com.google.firebase.referencecode.database.models.User
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class TabActivity : AppCompatActivity() {
    val database = Firebase.database
    val myRef = database.getReference("book")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)


//        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
//        val viewPager: ViewPager = findViewById(R.id.view_pager)
//        viewPager.adapter = sectionsPagerAdapter
//        val tabs: TabLayout = findViewById(R.id.tabs)
//        tabs.setupWithViewPager(viewPager)
//        val fab: FloatingActionButton = findViewById(R.id.fab)
//
//        val database = Firebase.database
//        val myRef = database.getReference("message")
//        myRef.setValue("Hello, World!")
//
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
    }

    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_layout, container, false)

        myRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val test = snapshot.child("bestseller")
                for(ds in test.children){
                    println("SDFSFD")
                    Log.e("snap", ds.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 읽어오기를 실패했을 때
            }
        })

        return view
    }
}