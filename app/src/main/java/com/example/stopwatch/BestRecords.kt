package com.example.stopwatch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView

class BestRecords : AppCompatActivity() {
    lateinit var records : ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_best_records)
        records = findViewById(R.id.records)
    }//onCreate
}//BestRecords