package com.example.stopwatch

import android.app.AlertDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Time
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.stopwatch.databinding.ActivityMainBinding
import kotlin.math.roundToInt


var timeList = ArrayList<Double?>()

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0.0
    lateinit var start : Button
    lateinit var save : Button
    lateinit var reset : Button
    lateinit var timeView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences("com.example.stopwatch", MODE_PRIVATE)
        timeList = ObjectSerializer.deserialize(sharedPreferences
            .getString("time", ObjectSerializer.serialize(ArrayList<String>()))) as ArrayList<Double?>

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.start.setOnClickListener { startStopTimer() }
        binding.reset.setOnClickListener { resetTimer() }

        save = findViewById(R.id.save)
        timeView = findViewById(R.id.timeView)

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
    }//onCreate

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater : MenuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }//onCreateOptionsMenu

    //https://stackoverflow.com/questions/53582783/go-to-another-activity-on-menu-item-selection-in-kotlin
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bestRecords -> {
                val intent = Intent(this, BestRecords::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }//onOptionsItemSelected

    fun save(view: View) {

        val sharedPreferences = getSharedPreferences("com.example.stopwatch", MODE_PRIVATE)
        sharedPreferences.edit().putString("time", ObjectSerializer.serialize(timeList)).apply()
        //Loop to find the min
        var min = timeList[0]
        for (i in 1 until timeList.size) {
            if (timeList[i]!! < min!!) {
                min = timeList.get(i)
            }
        }
        timeList.add(time)
        if (time < min!!){
            //Congratulations
            AlertDialog.Builder(this)
                .setTitle("🎉 Congratulations!")
                .setMessage("You just beat your highest mile record. Go, you!")
                .setNegativeButton("Great!", null)
                .setPositiveButton("Check Records",DialogInterface.OnClickListener(){
                        dialogInterface: DialogInterface?, j: Int ->
                    val intent = Intent(this, BestRecords::class.java)
                    startActivity(intent)
                })
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("😇 You finished!")
                .setMessage("Maybe you didn’t beat the highest record this time, but hey, you finished! Great effort!")
                .setNegativeButton("Okay", null)
                .setPositiveButton("Check Records",DialogInterface.OnClickListener(){
                        dialogInterface: DialogInterface?, j: Int ->
                    val intent = Intent(this, BestRecords::class.java)
                    startActivity(intent)
                })
                .show()
        }
    }

    private fun resetTimer() {
        stopTimer()
        time = 0.0
        binding.timeView.text = getTimeStringFromDouble(time)
    }

    private fun startStopTimer() {
        if (timerStarted)
            stopTimer()
        else
            startTimer()
    }

    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
        binding.start.text = "Stop"
        timerStarted = true
    }

    private fun stopTimer() {
        stopService(serviceIntent)
        binding.start.text = "Start"
        timerStarted = false
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            time = intent.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.timeView.text = getTimeStringFromDouble(time)
        }
    }

    private fun getTimeStringFromDouble(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt % 86400 / 3600
        val minutes = resultInt % 86400 % 3600 / 60
        val seconds = resultInt % 86400 % 3600 % 60

        return makeTimeString(hours, minutes, seconds)
    }

    private fun makeTimeString(hour: Int, min: Int, sec: Int): String =
        String.format("%02d:%02d:%02d", hour, min, sec)
}//MainActivity