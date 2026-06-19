package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class TravelClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    // 모델의 출력 레이블 (순서는 일반적인 모델 학습 순서를 따르며, 필요 시 수정 가능합니다)
    private val labels = listOf("음식", "자연경관", "관광지")

    init {
        try {
            // assets 폴더의 모델 파일을 로드합니다.
            val model = FileUtil.loadMappedFile(context, "travel_classifier.tflite")
            interpreter = Interpreter(model)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun classify(uri: Uri): String {
        val interpreter = this.interpreter ?: return "관광지"
        val bitmap = getBitmapFromUri(uri) ?: return "관광지"
        
        // 모델 입력 규격(224x224)에 맞춰 이미지 전처리
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // 0.0 ~ 1.0 정규화
            .build()

        var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(tensorImage.buffer, output)

        // 가장 높은 확률을 가진 인덱스 추출
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
}
