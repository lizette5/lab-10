package com.example.background


import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class BDPeriodicScheduler(context: Context, workerParameters: WorkerParameters) : Worker(context,workerParameters){

    override fun doWork(): Result {
        return try {
            for (i in 1..500) {
                //Toast.makeText(context,"Shrink data # $i",Toast.LENGTH_SHORT).show()
                Log.i("Periodic", "Mensaje:  $i")
            }
            Result.success()
        } catch (e:Exception){
            Result.failure()
        }
    }
}