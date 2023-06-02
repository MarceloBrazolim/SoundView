package com.example.testtest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.google.android.material.slider.Slider

private val MainActivity.v
    get() = run {
        // Uses compatible module for feature usage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Defines button behavior with listeners
        val btn1 = findViewById<Button>(R.id.btn1)
        val btn2 = findViewById<Button>(R.id.btn2)
        var strength: Float

        btn1.setOnClickListener {
            // Update strength with every button click
            strength = findViewById<Slider>(R.id.slider).value
            sendToast("Vibration Started")
            // Commit cellphone go brr
            doTheWiggle(true, strength)
            println("debug: Vibration Ended")
        }
        btn2.setOnClickListener {
            sendToast("Vibration Canceled")
            // Cancels the phone go brr
            doTheWiggle(false)
        }
    }
    private fun doTheWiggle(vibrating: Boolean, strength: Float=100.0F, duration: Int=500, delay: Int=duration+500) {
        // Until cancelled, repeat
        while (vibrating) {
            println("debug: repeated")
            // Check compatibility with hardware to determine method usage
            if (isCompatible()) {
                // Vibrate method for API Level 26 or higher
                this.v.vibrate(VibrationEffect.createOneShot(duration.toLong(), strength.toInt()))
            } else {
                // Vibrate method for below API Level 26
                @Suppress("DEPRECATION")
                v.vibrate(duration.toLong())
            }
            // Applies delay for each iteration
            Thread.sleep(delay.toLong())
        }
    }

    private fun sendToast(text: String) {
        // Sends a little message to inform user everything's alright
        Toast.makeText( this, text, Toast.LENGTH_SHORT ).show()
        println("debug: $text")
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun isCompatible() : Boolean {
        // Check compatibility with hardware
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}