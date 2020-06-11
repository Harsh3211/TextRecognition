package com.example.text

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
//import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.text.Graphic.GraphicOverlay
import com.example.text.Graphic.TextGraphic
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector


class MainActivity : AppCompatActivity() {
    private var snapBtn: Button? = null
    private var detectBtn: Button? = null
    private var imageView: ImageView? = null
    private var txtView: TextView? = null
    private var imageBitmap: Bitmap? = null
    private val REQUEST_TAKE_PHOTO = 1

    //@BindView(R.id.graphic_overlay)
    var mGraphicOverlay: GraphicOverlay? = null



    private var filename : String = "photo"
    private var currentPath : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mGraphicOverlay = findViewById(R.id.graphic_overlay)
        snapBtn = findViewById(R.id.btncamera)
        detectBtn = findViewById(R.id.btndetect)
        imageView = findViewById(R.id.imageView)
        txtView = findViewById(R.id.txtview)
//        if (supportActionBar != null)
//            supportActionBar?.hide()
        btncamera.setOnClickListener(View.OnClickListener { dispatchTakePictureIntent() })
        btndetect.setOnClickListener(View.OnClickListener { detectTxt() })
    }

    private fun dispatchTakePictureIntent() {
        mGraphicOverlay?.clear();
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.text.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    //Creates a temp file for the image to store it in orignal resolution.

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPath = absolutePath
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imageBitmap = BitmapFactory.decodeFile(currentPath)

            //val extras = data!!.extras
            //val imageBitmap = data.extras?.get("data") as Bitmap
            imageView?.setImageBitmap(imageBitmap)
        }
    }

    private fun detectTxt() {
        val image = FirebaseVisionImage.fromBitmap(imageBitmap!!)
        val detector = FirebaseVision.getInstance()
            .onDeviceTextRecognizer

        detector.processImage(image)
            .addOnSuccessListener(OnSuccessListener<FirebaseVisionText> { firebaseVisionText ->
                Log.d("Main","Success Listener for Firebase!")
                processTxt(firebaseVisionText)

            }).addOnFailureListener(OnFailureListener {
                    Log.d("Main","Error in Firebase")
                })
    }


    private fun processTxt(text: FirebaseVisionText) {
        val resultText = text.text

        if (resultText.isEmpty()) {
            Toast.makeText(this@MainActivity, "No Text :(", Toast.LENGTH_LONG).show()
            return
        }
        //mGraphicOverlay?.clear()
        for (block in text.textBlocks) {
            val blockText = block.text
            val blockConfidence = block.confidence
            val blockLanguages = block.recognizedLanguages
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                val lineConfidence = line.confidence
                val lineLanguages = line.recognizedLanguages
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (element in line.elements) {
                    val elementText = element.text
                    val elementConfidence = element.confidence
                    val elementLanguages = element.recognizedLanguages
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox

                    /*
                    val textGraphic: TextGraphic? =
                        mGraphicOverlay?.let { TextGraphic(it, element) }
                    if (textGraphic != null) {
                        mGraphicOverlay!!.add(textGraphic)
                    }

                     */

                }
            }
        }
        Log.d("Main","${text.text} \n")
        Log.d("Main","${text.textBlocks}")
        txtView?.text = text.text
    }



    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}
