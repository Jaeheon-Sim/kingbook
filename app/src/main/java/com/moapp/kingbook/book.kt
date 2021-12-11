package com.moapp.kingbook.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moapp.kingbook.R

class book : Fragment() {
    val database = Firebase.database
    val myRef = database.getReference("book")



    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_layout, container, false)

        myRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val test = snapshot.child("bestseller")
                for(ds in test.children){
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