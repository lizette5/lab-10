
package com.example.background

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.background.databinding.ActivityBlurBinding
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.activity_blur.*

class DefenseActivity : AppCompatActivity() {

    private lateinit var viewModel: DifuminadoViewModel
    private lateinit var binding: ActivityBlurBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the ViewModel
        viewModel = ViewModelProviders.of(this).get(DifuminadoViewModel::class.java)
        buttonClick.setOnClickListener {
            setPeriodicWorkRequest()
        }

        // Image uri should be stored in the ViewModel; put it there then display
        val imageUriExtra = intent.getStringExtra(KEY_IMAGE_URI)
        viewModel.setImageUri(imageUriExtra)
        viewModel.imageUri?.let { imageUri ->
            Glide.with(this).load(imageUri).into(binding.imageView)
        }

        binding.goButton.setOnClickListener { viewModel.applyBlur(blurLevel) }

        // Setup view output image file button
        binding.seeFileButton.setOnClickListener {
            viewModel.outputUri?.let { currentUri ->
                val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                actionView.resolveActivity(packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }

        // Hookup the Cancel button
        binding.cancelButton.setOnClickListener { viewModel.cancelWork() }

        viewModel.outputWorkInfos.observe(this, workInfosObserver())

        // Show work progress
        viewModel.progressWorkInfoItems.observe(this, progressObserver())
    }

    private fun setPeriodicWorkRequest(){
        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            BDPeriodicScheduler::class.java,
            15, // minimum 15
            TimeUnit.MICROSECONDS
        ).build()

        WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
    }

    private fun progressObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            listOfWorkInfo.forEach { workInfo ->
                if (WorkInfo.State.RUNNING == workInfo.state) {
                    val progress = workInfo.progress.getInt(PROGRESS, 0)
                    binding.progressBar.progress = progress
                }
            }

        }
    }

    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->

            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showWorkFinished()

                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                    binding.seeFileButton.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    /**
     * Muestra y oculta vistas cuando la actividad está procesando una imagen
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    /**
     * Muestra y oculta vistas para cuando la Actividad termine de procesar una imagen
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.VISIBLE
            progressBar.progress = 0
        }
    }

    private val blurLevel: Int
        get() =
            when (binding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }

}
