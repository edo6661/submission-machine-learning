package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


class ImageClassifierHelper(private val context: Context) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        try {
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setMaxResults(1)
                .build()
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                "cancer_classification.tflite",
                options
            )
        } catch (e: Exception) {
            imageClassifier = null
            e.printStackTrace()
        }
    }

    fun classifyStaticImage(imageUri: Uri, callback: (String?, Exception?) -> Unit) {
        try {
            var bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, imageUri)
                )
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            if (bitmap.config != Bitmap.Config.ARGB_8888) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            }

            val imageProcessor = ImageProcessor.Builder().build()
            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

            val results = imageClassifier?.classify(tensorImage)

            if (results != null && results.isNotEmpty()) {
                val firstResult = results[0].categories[0]
                val confidence = firstResult.score * 100
                val prediction = "${firstResult.label} (${String.format("%.1f", confidence)}%)"
                callback(prediction, null)
            } else {
                callback(null, Exception("Tidak dapat mengklasifikasikan gambar"))
            }
        } catch (e: Exception) {
            callback(null, e)
        }
    }

}
