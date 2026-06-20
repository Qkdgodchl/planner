package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class TravelClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val labels = listOf("음식", "관광지", "자연경관")

    init {
        try {
            val model = FileUtil.loadMappedFile(context, "travel_classifier.tflite")
            interpreter = Interpreter(model)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load travel_classifier.tflite", e)
        }
    }

    fun classify(uri: Uri): String {
        val interpreter = this.interpreter ?: return "관광지"
        val bitmap = getBitmapFromUri(uri) ?: return "관광지"

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(tensorImage.buffer, output)

        val result = output[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1

        return if (maxIndex != -1) {
            labels[maxIndex]
        } else {
            "관광지"
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        interpreter?.close()
    }

    companion object {
        private const val TAG = "TravelClassifier"
    }
}
