package com.example.linking_application_android.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import java.io.ByteArrayOutputStream
//import org.opencv.android.Utils
//import org.opencv.core.Mat
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

/**
 * This is to help with processing bitmaps in the app
 */

class BitmapHelper {
    companion object {

//        fun bitmapToMat(bitmap: Bitmap?): Mat {
//            val image = Mat()
//            Utils.bitmapToMat(bitmap, image)
//            return image
//        }

        fun readBitmapFromUri(c: Context, path: Uri?): Bitmap? {
            val stream = path?.let { c.contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(stream)
            stream!!.close()
            return bitmap
        }

        fun readBitmapFromFile(f: File?): Bitmap? {
            val stream = f?.inputStream()
            val bitmap = BitmapFactory.decodeStream(f?.inputStream())
            stream!!.close()
            return bitmap
        }

//        fun readBitmapFromAssetsRes(f: File?): Bitmap? {
//            val stream = f?.inputStream()
//            val bitmap = BitmapFactory.decodeStream(f?.inputStream())
//            stream!!.close()
//            return bitmap
//        }

        fun showBitmap(bitmap: Bitmap?, imageView: ImageView?) {
            imageView!!.setImageBitmap(bitmap)
        }

        fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray? {
            var baos: ByteArrayOutputStream? = null
            return try {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                baos.toByteArray()
            } finally {
                if (baos != null) {
                    try {
                        baos.close()
                    } catch (e: IOException) {
                        Log.e(
                            "BitmapHelper",
                            "ByteArrayOutputStream was not closed"
                        )
                    }
                }
            }
        }

        fun convertBitmapToByteArrayUncompressed(bitmap: Bitmap): ByteArray? {
            val byteBuffer: ByteBuffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(byteBuffer)
            byteBuffer.rewind()
            println("bitmap to byte array completed")
            return byteBuffer.array()
        }


    }
}
//DONE fun readBitmapFromPath
//DONE fun showBitmap
//DONE fun bitmapToMat