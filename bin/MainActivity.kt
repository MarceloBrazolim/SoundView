package com.example.testtest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.testtest.util.checkSelfPermissionCompat
import com.example.testtest.util.requestPermissionsCompat
import com.example.testtest.util.shouldShowRequestPermissionRationaleCompat
import com.example.testtest.util.showSnackbar
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import kotlin.Exception
import kotlin.math.log10

const val PERMISSION_REQUEST_RECORD_AUDIO = 0

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var layout: View
    private lateinit var mediaRecorder: MediaRecorder

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        layout = findViewById(R.id.main_layout)

        // Layout elements
        val amplitudeTxt = findViewById<TextView>(R.id.amplitudeTxt)
        val intensityTxt = findViewById<TextView>(R.id.intensityTxt)
        val decibelTxt = findViewById<TextView>(R.id.decibelTxt)
        val thresholdSld = findViewById<Slider>(R.id.threshold)
        val vibrateBtn = findViewById<Button>(R.id.vibrateBtn)

        val duration = 500L

        // Coroutine vars
        val handler = CoroutineExceptionHandler { _, throwable ->
            println("Caught exception: $throwable")
        }
        val vib = run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
        }

        vibrateBtn.setOnClickListener {
            if (vibrateBtn.text == "Vibrate") {
                vibrateBtn.text = "Cancel"
                try {
                    mediaRecorder.resume()
                } catch (e: Exception) { e.printStackTrace() }
                sendToast("Vibration Started")
            } else if (vibrateBtn.text == "Cancel") {
                vibrateBtn.text = "Vibrate"
                try {
                    mediaRecorder.pause()
                } catch (e: Exception) { e.printStackTrace() }
                sendToast("Vibration Canceled")
            }
        }

        // Multi-processing scope
        CoroutineScope(Dispatchers.Default + handler).launch {
            // Compat req
            val compatible = isCompatible()
            if (!hasMicrophone()) sendToast("Microphone not detected, some features may not work.") else {
                println("Microphone detected")
            }
            var strength: Int
            var decibel: Double
            var sensibility: Float
            var intensity: Int

            var decibelH = 60

            val rgbH = arrayOf(255, 255, 255)
            val rgb = arrayOf(255, 255, 255)
            var rgbRaw: Array<Int>
            var transparency: Int

            val fps = 50
            var start = System.currentTimeMillis()
            initRecorder()

            while (true) {
                if (vibrateBtn.text == "Cancel") {
                    strength = mediaRecorder.maxAmplitude
                    /**
                     * If the units are amplitudes the multiplier is 20. If the units are power the multiplier is 10.
                     * <a href="en.wikipedia.org/wiki/Line_level">Line level reference</a>
                     */
                    decibel = 20 * log10(/*amp*/strength.toDouble() / /*amp_ref*/ 1.736)
                    // Applies a curve-graph like sensibility multiplier based on common dB perception
                    sensibility = when (decibelH) {
                        in 1..10 -> 1.5F
                        in 11..20 -> 1.5F
                        in 21..30 -> 2.0F
                        in 31..40 -> 2.0F
                        in 41..50 -> 2.0F
                        in 51..60 -> 2.0F
                        in 61..70 -> 2.0F
                        in 71..80 -> 2.5F
                        in 81..90 -> 2.5F
                        in 91..100 -> 2.5F
                        else -> 1.5F
                    }
                    // Applies fps limiter for processing capture history
                    if ((System.currentTimeMillis() - start) >= duration) decibelH = decibel.toInt()
                    intensity = (decibel * /*sensibility*/ sensibility).toInt()
                    transparency = 255

                    // TODO add translation to notes (review necessary)
                    withContext(Dispatchers.Main) {
                        amplitudeTxt.text = "Amplitude: $strength"
                        intensityTxt.text = "Intensity: $intensity"
                        decibelTxt.text = "Decibel: ${decibel.toInt()}"
                    }
                } else {
                    intensity = 35
                    transparency = 0
                }

                intensity = limitValue(intensity)

                /**
                 * Multi-processing method for rendering the background color
                 * on the layout.
                 */
                launch {
                    /*
                    // Applies different RGB values depending on the intensity value
                    rgbRaw = when (intensity) {
                        in 1..40 ->    arrayOf(255, 255, 255) // white
                        in 41..80 ->   arrayOf(0  , 0  , 255) // blue
                        in 81..120 ->  arrayOf(0  , 255, 255) // blue-green
                        in 121..160 -> arrayOf(0  , 255, 0  ) // green
                        in 161..200 -> arrayOf(255, 255, 0  ) // yellow
                        in 201..255 -> arrayOf(255, 0  , 0  ) // red
                        else -> arrayOf(255, 255, 255)
                    }*/
                    rgbRaw = arrayOf(2, 122, 82) // aquamarine
                    // Applies fps limiter for processing capture history
                    if ((System.currentTimeMillis() - start) >= 50) {
                        for (i in 0..2) {
                            // The closer the intensity value is to 255, the more vivid the color
                            rgb[i] = limitValue((rgbRaw[i] + intensity + rgbH[i]) / 3)
                            rgbH[i] = rgb[i]
                        }

                        withContext(Dispatchers.Main) {
                            // Modifies the background color of the layout
                            layout.setBackgroundColor(Color.argb(transparency, rgb[0], rgb[1], rgb[2]))
                        }
                    }
                }

                /**
                 * Multi-processing method for parsing information into the
                 * vibration hardware of the device.
                 */
                launch {
                    // Commit cellphone go brr
                    if (vibrateBtn.text == "Cancel" && System.currentTimeMillis() - start >= duration) {
                        println("repeated")
                        if (intensity >= thresholdSld.value) {
                            if (compatible) {
                                // Vibrate method for API Level 26 or higher
                                try {
                                    vib.vibrate(
                                        // VibrationEffect.createOneShot(duration, intensity)
                                        VibrationEffect.createWaveform(longArrayOf(duration), intArrayOf(intensity), -1)
                                    )
                                } catch (e: Exception) { e.printStackTrace() }
                                println("debug: vibrated")
                            } else {
                                // Vibrate method for below API Level 26
                                @Suppress("DEPRECATION")
                                vib.vibrate(duration)
                            }
                        }
                    }
                }
                // Resets the fps limiter counter
                if ((System.currentTimeMillis() - start) >= duration + 100) start = System.currentTimeMillis()
                delay(fps.toLong())
            }
        }
    }

    /**
     * Triggers after the user is prompted to grant permission.
     * If the permission is denied, a rationale is shown, explaining
     * why the permission is important.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_RECORD_AUDIO -> {
                // Request for RECORD_AUDIO permission.
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted.
                    layout.showSnackbar(R.string.record_audio_granted, Snackbar.LENGTH_SHORT)
                } else {
                    // Permission request was denied.
                    layout.showSnackbar(R.string.record_audio_denied, Snackbar.LENGTH_SHORT)
                }
            }
        }
    }

    /**
     * Prepares to initialize the recording function.
     * If permission has been granted already, it will start the
     * preparation method right away. If not, permission will be requested
     * to the user.
     */
    private fun initRecorder() {
        // Check if the permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED) {
            // Permission is already available
            layout.showSnackbar(R.string.record_audio_available, Snackbar.LENGTH_SHORT)

            prepareRecorder()

        } else {
            // Permission is missing and must be requested.
            requestPermission(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * Spawns a windows to the user requesting permission.
     * If the user denies the permission requested, the app will
     * ignore any further attempts of requiring the permission.
     * If the permission is due a core function, however, the app
     * will need to be restarted in order to function properly.
     * @param permission Permission to be requested.
     */
    @Suppress("SameParameterValue")
    private fun requestPermission(permission: String) {
        // Permission has not been granted and must be requested.
        when (permission) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Manifest.permission.RECORD_AUDIO -> {
                if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.RECORD_AUDIO)) {
                    layout.showSnackbar(R.string.record_audio_access_required, Snackbar.LENGTH_INDEFINITE, R.string.ok) {
                        requestPermissionsCompat(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_RECORD_AUDIO) }
                } else {
                    layout.showSnackbar(R.string.record_audio_not_available, Snackbar.LENGTH_SHORT)
                    // Request the permission. The result will be received in onRequestPermissionResult().
                    requestPermissionsCompat(arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_RECORD_AUDIO)
                }
            }

        }
    }

    /**
     * Prepares the recorder to begin capturing and encoding data.
     * This method requires previous permission authentication via
     * application Compat.
     */
    private fun prepareRecorder() {
        // TODO add compatibility for android < 10 (api < 29) (scoped storage -> cache) (optional)
        val output = File(applicationContext.cacheDir.path)

        if (!output.exists()) output.mkdirs()

        @Suppress("SameParameterValue")
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setOutputFile("${output}/667555112326.mpeg4")
        try {
            mediaRecorder.prepare()
            mediaRecorder.start()
            mediaRecorder.pause()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Simple util to manage excessive input into compatible value.
     * @param value The value to be limited.
     * @return Limited value into predefined range.
     */
    private fun limitValue(value: Int): Int {
        return if (value > 255) {
            255
        } else if (value < 1) {
            1
        } else {
            value
        }
    }

    /**
     * Simple util to auto generate a Toast for a short period with a
     * specified message.
     * @param text Text to be displayed withing a Toast.
     */
    private fun sendToast(text: String) {
        Toast.makeText( this, text, Toast.LENGTH_SHORT ).show()
    }

    /**
     * Compatibility check for core function (require android > 8).
     * @return Boolean.
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun isCompatible(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * Compatibility check for core function (require microphone).
     * @return Boolean.
     */
    private fun hasMicrophone(): Boolean {
        val pmanager = this.packageManager
        return pmanager.hasSystemFeature(
            PackageManager.FEATURE_MICROPHONE)
    }
}
