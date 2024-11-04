package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper

class MainActivity : AppCompatActivity() {

  private lateinit var binding : ActivityMainBinding
  private var currentImageUri : Uri? = null


  override fun onCreate(savedInstanceState : Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.analyzeButton.setOnClickListener {
      if (currentImageUri != null) {
        analyzeImage()
      } else {
        showToast("Pilih gambar terlebih dahulu")
      }
    }
    binding.galleryButton.setOnClickListener {
      startGallery()
    }
  }


  private val launcherGallery = registerForActivityResult(
    ActivityResultContracts.PickVisualMedia()
  ) { uri : Uri? ->
    if (uri != null) {
      currentImageUri = uri
      showImage()
    } else {
      Log.d("Photo Picker", "No media selected")
    }
  }

  private fun startGallery() {
    launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
  }

  private fun showImage() {
    currentImageUri?.let {
      binding.previewImageView.setImageURI(it)
    }
  }

  private fun analyzeImage() {
    currentImageUri?.let { uri ->
      val imageClassifier = ImageClassifierHelper(
        context = this
      )
      imageClassifier.classifyStaticImage(uri) { result, error ->
        if (error != null) {
          showToast("Error: ${error.message}")
          Log.e("Image Classifier", error.message.toString())
        } else {
          val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, uri.toString())
            putExtra(ResultActivity.EXTRA_RESULT, result)
          }
          startActivity(intent)
        }
      }
    }
  }

  private fun showToast(message : String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }


}
