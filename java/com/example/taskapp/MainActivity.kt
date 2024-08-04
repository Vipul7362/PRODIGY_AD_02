package com.example.taskapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), UpdateAndDelete {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: ToDoAdapter
    private lateinit var listViewItem: ListView
    private var toDOList: MutableList<ToDoModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        listViewItem = findViewById(R.id.item_list_view)

        database = FirebaseDatabase.getInstance().reference

        fab.setOnClickListener { view ->
            showAddTaskDialog()
        }

        adapter = ToDoAdapter(this, toDOList)
        listViewItem.adapter = adapter

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                toDOList.clear()
                addItemToList(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "No Item Added", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showAddTaskDialog() {
        val alertDialog = AlertDialog.Builder(this)
        val editText = EditText(this)
        alertDialog.setMessage("Add Task")
        alertDialog.setTitle("Enter Your task")
        alertDialog.setView(editText)
        alertDialog.setPositiveButton("Add") { dialog, i ->
            val todoItemData = ToDoModel.createList()
            todoItemData.itemDataText = editText.text.toString()
            todoItemData.done = false

            val newItemData = database.child("todo").push()
            todoItemData.UID = newItemData.key

            newItemData.setValue(todoItemData)
            dialog.dismiss()
            Toast.makeText(this, "TASK SAVED", Toast.LENGTH_LONG).show()
        }
        alertDialog.show()
    }

    private fun addItemToList(snapshot: DataSnapshot) {
        snapshot.children.forEach { toDoIndexedValue ->
            toDoIndexedValue.children.forEach { currentItem ->
                val toDoItemData = ToDoModel.createList()
                val map = currentItem.value as HashMap<String, Any>

                toDoItemData.UID = currentItem.key
                toDoItemData.done = map["done"] as Boolean?
                toDoItemData.itemDataText = map["itemDataText"] as String?
                toDOList.add(toDoItemData)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun modifyItem(itemUID: String, isDone: Boolean) {
        database.child("todo").child(itemUID).child("done").setValue(isDone)
    }

    override fun onItemDelete(itemUID: String) {
        database.child("todo").child(itemUID).removeValue()
        adapter.notifyDataSetChanged()
    }
}