
@file:JvmName("WorkerUtils")

package com.example.background.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import com.example.background.CHANNEL_ID
import com.example.background.DELAY_TIME_MILLIS
import com.example.background.NOTIFICATION_ID
import com.example.background.NOTIFICATION_TITLE
import com.example.background.OUTPUT_PATH
import com.example.background.R
import com.example.background.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import com.example.background.VERBOSE_NOTIFICATION_CHANNEL_NAME
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Cree una notificación que se muestre como una notificación de advertencia si es posible.
 *
 * Para este laboratorio de código, esto se usa para mostrar una notificación para que sepa cuándo se realizan los diferentes pasos.
 * de la cadena de trabajo de fondo están comenzando
 *
 */
fun makeStatusNotification(message: String, context: Context) {

    // Hacer un canal si es necesario
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Cree el canal de notificación, pero solo en API 26+ porque
        // la clase NotificationChannel es nueva y no está en la biblioteca de soporte
        val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        // Agregar el canal
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    // Crea la notificación
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

    //Mostrando la notificación
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

/**
 * Método para dormir durante un tiempo fijo para emular un trabajo más lento
 */
fun sleep() {
    try {
        Thread.sleep(DELAY_TIME_MILLIS, 0)
    } catch (e: InterruptedException) {
        Timber.e(e.message)
    }

}

@WorkerThread
fun blurBitmap(bitmap: Bitmap, applicationContext: Context): Bitmap {
    lateinit var rsContext: RenderScript
    try {

        // Crea el mapa de bits de salida
        val output = Bitmap.createBitmap(
                bitmap.width, bitmap.height, bitmap.config)

        // Desenfocar la imagen
        rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val inAlloc = Allocation.createFromBitmap(rsContext, bitmap)
        val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)
        val theIntrinsic = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))
        theIntrinsic.apply {
            setRadius(10f)
            theIntrinsic.setInput(inAlloc)
            theIntrinsic.forEach(outAlloc)
        }
        outAlloc.copyTo(output)

        return output
    } finally {
        rsContext.finish()
    }
}


@Throws(FileNotFoundException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    val name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString())
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    val outputFile = File(outputDir, name)
    var out: FileOutputStream? = null
    try {
        out = FileOutputStream(outputFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
    } finally {
        out?.let {
            try {
                it.close()
            } catch (ignore: IOException) {
            }

        }
    }
    return Uri.fromFile(outputFile)
}
