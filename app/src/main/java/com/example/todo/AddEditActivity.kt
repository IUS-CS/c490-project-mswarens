package com.example.todo

import android.app.*
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.FirebaseDatabase
import java.util.*


class AddEditActivity : AppCompatActivity(){

    private val todoText: EditText by lazy { findViewById<EditText>(R.id.todoTextEntry) }
    private val timeText: EditText by lazy { findViewById<EditText>(R.id.timeSelection) }
    private val dateText: EditText by lazy { findViewById<EditText>(R.id.dateSelection) }
    private val submitButton: Button by lazy { findViewById<Button>(R.id.submit) }
    private val returnToItemList: Button by lazy { findViewById<Button>(R.id.returnToItemList) }

    var hour = 0
    var min = 0
    var y = 0
    var m = 0
    var d = 0

    private val database = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_edit_activity_view)
        timeDialog(timeText)
        dateDialog(dateText)
        val listName = intent.getStringExtra("CURRENTLIST")
        val uID = intent.getStringExtra("UID")
        var itemID = ""

        if(intent.getBooleanExtra("EDITITEMMODE", false)) {
            val timedateData = intent.getStringExtra("TIMEDATEDATA")
            val todoData = intent.getStringExtra("TODODATA")
            var timedateList = timedateData!!.split(" ")
            itemID = intent.getStringExtra("ITEMID") as String
            timeText.setText(timedateList[1])
            editTimeData(timedateList[1])
            dateText.setText(timedateList[0])
            editDateData(timedateList[0])
            todoText.setText(todoData)
        }

        submitButton.setOnClickListener(){
            if(intent.getBooleanExtra("EDITITEMMODE",false)){
                val r = database.reference.child(uID as String).child(listName!!).child(itemID)
                val timedateString = stringDate(y, m, d) + " " + stringTime(hour, min)
                r.child("timedate").setValue(timedateString)
                r.child("todo").setValue(todoText.text.toString())
                finish()

                }else{
                val r = database.reference.child(uID as String).child(listName!!).push()
                val timedateString = stringDate(y, m, d) + " " + stringTime(hour, min)
                r.child("timedate").setValue(timedateString)
                r.child("todo").setValue(todoText.text.toString())
                finish()
            }
        }

        returnToItemList.setOnClickListener(){
            finish()
        }

    }

    private fun stringTime(h: Int, m: Int): String{
        var hourFix = h.toString()
        var minuteFix = m.toString()
        if(h < 10){
            hourFix = "0$h"
        }
        if(m < 10){
            minuteFix = "0$h"
        }

        return "$hourFix:$minuteFix"
    }

    private fun editTimeData(str: String){
        var strArr = str.split(":")
        hour = strArr[0].toInt()
        min = strArr[1].toInt()
    }

    private fun stringDate(y: Int, m: Int, d: Int): String{
        return "$y/$m/$d"
    }

    private fun editDateData(str: String){
        var strArr = str.split("/")
        y = strArr[0].toInt()
        m = strArr[1].toInt()
        d = strArr[2].toInt()
    }
    private fun timeDialog(editText: EditText){
        editText.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val c = Calendar.getInstance()
                val currentH = c.get(Calendar.HOUR_OF_DAY)
                val currentM = c.get(Calendar.MINUTE)
                val tpd = TimePickerDialog(this@AddEditActivity, object : OnTimeSetListener {
                        override fun onTimeSet(view: android.widget.TimePicker, hourOfDay: Int, minute: Int) {
                            hour = hourOfDay
                            min = minute
                            editText.setText(stringTime(hourOfDay,minute))
                        }
                    }, currentH, currentM, false)
                tpd.show()
            }
        })

    }

    private fun dateDialog(editText: EditText){
        editText.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val c = Calendar.getInstance()
                val currentY = c.get(Calendar.YEAR)
                val currentM = c.get(Calendar.MONTH)
                val currentD = c.get(Calendar.DAY_OF_MONTH)
                val dpd = DatePickerDialog(this@AddEditActivity, object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(view: android.widget.DatePicker, year: Int, month: Int, day: Int) {
                        y = year
                        m = month
                        d = day
                        editText.setText(stringDate(y,m,d))
                    }
                }, currentY,currentM,currentD)
                dpd.show()
            }
        })
    }

}