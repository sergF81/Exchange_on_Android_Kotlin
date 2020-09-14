package com.example.kotlinmanyexchange

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

import java.util.ArrayList

class LogActivity : AppCompatActivity() {
    //создаем массив, в котором будут храниться данные из массива в главной активности
    var logList: ArrayList<String>? = ArrayList()

    // создаем список для отображения данных из массива logList
    private var listViewLog: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        listViewLog = findViewById(R.id.ListViewLog)


        //получаем массив log из главной активности и присваиваем его значения массиву logList
        logList = intent.getStringArrayListExtra("log")

        //создаем объект, который создает список с данными из массива logList
        addLogInArray()

    }

    //объявляем объект для создания и отображения списака с данными из массива LogList
    fun addLogInArray() {


        // создаем адаптер списка с данными массива logList
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1, logList!!
        ) {

            // меняем размер размер текста в ListView
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (view is TextView) {
                    view.textSize = 18f
                }
                return view
            }
        }

        //присваем элементам списка выше созданый адаптер
        listViewLog?.adapter = adapter

    }
}