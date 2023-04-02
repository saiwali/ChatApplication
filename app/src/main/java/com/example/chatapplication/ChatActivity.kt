package com.example.chatapplication

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList : ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    var recieverRoom : String?  = null
    var senderRoom : String?  = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)




        val name = intent.getStringExtra("name")
        val recieverUid = intent.getStringExtra("uid")

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()

        senderRoom = recieverUid + senderUid
        recieverRoom = senderUid + recieverUid


         supportActionBar?.title= name

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sentButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this,messageList)



        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()


                    for(postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message ::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        sendButton.setOnClickListener {

            val message = messageBox.text.toString()
            val messageObject = senderUid?.let { it1 -> Message(message, it1) }

           mDbRef.child("chats").child(senderRoom!!).child("messages").push()
               .setValue(messageObject).addOnSuccessListener {
                   mDbRef.child("chats").child(recieverRoom!!).child("messages").push()
                       .setValue(messageObject)
               }

            messageBox.setText("")
        }



    }
}