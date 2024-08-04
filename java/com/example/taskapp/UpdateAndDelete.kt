package com.example.taskapp

interface UpdateAndDelete{


    fun modifyItem(itemUID:String, isDone:Boolean)

    fun onItemDelete(itemUID: String)
}