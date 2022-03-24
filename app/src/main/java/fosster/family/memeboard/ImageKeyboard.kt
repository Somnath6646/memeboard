package fosster.family.memeboard

import androidx.annotation.NonNull

import androidx.annotation.RawRes

import android.view.inputmethod.EditorInfo

import android.content.pm.PackageManager

import androidx.core.content.ContextCompat.getSystemService

import android.app.AppOpsManager

import android.os.Build

import android.view.inputmethod.InputBinding

import androidx.core.view.inputmethod.InputConnectionCompat

import android.content.ClipDescription
import android.content.Context

import androidx.core.view.inputmethod.InputContentInfoCompat

import android.content.Intent

import androidx.core.content.FileProvider

import androidx.core.view.inputmethod.EditorInfoCompat

import android.view.inputmethod.InputConnection

import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.annotation.Nullable
import java.io.*
import java.lang.Exception

import android.widget.LinearLayout

import android.widget.RelativeLayout
import android.widget.Toast
import fosster.family.memeboard.datamodels.Memelist
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.widget.ImageButton
import com.squareup.picasso.Picasso
import android.graphics.Bitmap

import android.os.Environment
import kotlin.coroutines.suspendCoroutine
import android.graphics.BitmapFactory
import kotlinx.coroutines.*
import java.net.URL
import java.util.*


class ImageKeyboard : InputMethodService() {

    var pngSupported: Boolean =false


    private fun isCommitContentSupported(
        @Nullable editorInfo: EditorInfo?, mimeType: String
    ): Boolean {
        if (editorInfo == null) {
            return false
        }
        val ic = currentInputConnection ?: return false
        if (!validatePackageName(editorInfo)) {
            return false
        }
        val supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo)
        println(editorInfo)
        for (supportedMimeType in supportedMimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                return true
            }
        }

        return false
    }

    private fun doCommitContent(
        description: String, mimeType: String,
        file: File
    ) {
        val editorInfo = currentInputEditorInfo
        val contentUri: Uri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
            BuildConfig.APPLICATION_ID + ".provider", file);
        val flag: Int
        if (Build.VERSION.SDK_INT >= 25) {
            // On API 25 and later devices, as an analogy of Intent.FLAG_GRANT_READ_URI_PERMISSION,
            // you can specify InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION to give
            // a temporary read access to the recipient application without exporting your content
            // provider.
            flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
        } else {
            // On API 24 and prior devices, we cannot rely on
            // InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION. You as an IME author
            // need to decide what access control is needed (or not needed) for content URIs that
            // you are going to expose. This sample uses Context.grantUriPermission(), but you can
            // implement your own mechanism that satisfies your own requirements.
            flag = 0
            try {
                grantUriPermission(
                    editorInfo.packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e(
                    TAG, "grantUriPermission failed packageName=" + editorInfo.packageName
                            + " contentUri=" + contentUri, e
                )
            }
        }
        val inputContentInfoCompat = InputContentInfoCompat(
            contentUri,
            ClipDescription(description, arrayOf(mimeType)),null
        )
        InputConnectionCompat.commitContent(
            currentInputConnection, currentInputEditorInfo, inputContentInfoCompat,
            flag, null
        )
    }

    private fun validatePackageName(@Nullable editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) {
            return false
        }
        val packageName = editorInfo.packageName ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true
        }
        val inputBinding = currentInputBinding
        if (inputBinding == null) {
            // Due to b.android.com/225029, it is possible that getCurrentInputBinding() returns
            // null even after onStartInputView() is called.
            // TODO: Come up with a way to work around this bug....
            Log.e(
                TAG, "inputBinding should not be null here. "
                        + "You are likely to be hitting b.android.com/225029"
            )
            return false
        }
        val packageUid = inputBinding.uid
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                appOpsManager.checkPackage(packageUid, packageName)
            } catch (e: Exception) {
                return false
            }
            return true
        }
        val packageManager = packageManager
        val possiblePackageNames = packageManager.getPackagesForUid(packageUid)
        for (possiblePackageName in possiblePackageNames!!) {
            if (packageName == possiblePackageName) {
                return true
            }
        }
        return false
    }

    private val repoRetriever = MemesRetriever()
    private var resultList: Memelist = Memelist(listOf())

    //2
    private val callback = object : Callback<Memelist> {
        override fun onFailure(call: Call<Memelist>?, t:Throwable?) {
            Log.e("MainActivity", "Problem calling Github API {${t?.message}}")
        }

        override fun onResponse(call: Call<Memelist>?, response: Response<Memelist>?) {
            response?.isSuccessful.let {
                Log.i("12447 Meme: ", "${response?.body()?.posts}")
                resultList = Memelist(response?.body()?.posts ?: emptyList())
                //do something with the list
                onCreateInputView()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        try {
            repoRetriever.getMemes(callback)
        }catch (e: Exception){
            Log.i("12447 memes: ", e.localizedMessage)
        }


    }

    override fun onCreateInputView(): View {

        Log.i("12447 Meme", "onCreateInputView: aya h $resultList")
        val KeyboardLayout =
            layoutInflater.inflate(fosster.family.memeboard.R.layout.keyboard_layout, null) as LinearLayout
        val ImageContainer = KeyboardLayout.findViewById<View>(R.id.imageContainer) as LinearLayout

        var ImageContainerColumn = layoutInflater.inflate(
            R.layout.image_container_column,
            ImageContainer,
            false
        ) as LinearLayout

        for (i in 0 until resultList.posts.size) {
            println(i)
                    ImageContainerColumn = layoutInflater.inflate(
                        R.layout.image_container_column,
                        ImageContainer,
                        false
                    ) as LinearLayout


            // Creating button
            val ImgButton = layoutInflater.inflate(
                R.layout.image_button,
                ImageContainerColumn,
                false
            ) as ImageButton

            Picasso.get().load(resultList.posts[i].memeUrl).into(ImgButton);

            ImgButton.tag = resultList.posts[i].memeUrl
            ImgButton.setOnClickListener { view ->
                val emojiName = view.tag.toString().replace("_".toRegex(), "-")
                GlobalScope.launch {
                    try {
                        val url = URL(resultList.posts[i].memeUrl)
                        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        val file: File? = bitmapToFile(bitmap = bitmap, context = this@ImageKeyboard, fileNameToSave = "image$i.png")
                        doCommitContent("A Funny meme", MIME_TYPE_PNG, file!!)

                    } catch (e: IOException) {
                        System.out.println(e)
                    }
                }





            }
            ImageContainerColumn.addView(ImgButton)
                ImageContainer.addView(ImageContainerColumn)

        }

        return KeyboardLayout
    }

    fun bitmapToFile(
        context: Context?,
        bitmap: Bitmap,
        fileNameToSave: String
    ): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file =
                File( this.getFilesDir(), File.separator.toString() + fileNameToSave)
            file!!.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // In full-screen mode the inserted content is likely to be hidden by the IME. Hence in this
        // sample we simply disable full-screen mode.
        return false
    }

    override fun onWindowShown() {
        super.onWindowShown()
        try {
            repoRetriever.getMemes(callback)
        }catch (e: Exception){
            Log.i("12447 memes: ", e.localizedMessage)
        }
/*
        Toast.makeText(this, "Hey dada", Toast.LENGTH_SHORT).show()
*/
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        pngSupported = isCommitContentSupported(info, MIME_TYPE_PNG);
        if(!pngSupported) {
            Toast.makeText(getApplicationContext(),
                "Images not supported here. Please change to another keyboard.",
                Toast.LENGTH_SHORT).show();
        }else{
            try {
                repoRetriever.getMemes(callback)
            }catch (e: Exception){
                Log.i("12447 memes: ", e.localizedMessage)
            }
        }
    }

    companion object {
        private const val TAG = "ImageKeyboard"
        private const val AUTHORITY = "Add you authoritiy here"
        private const val MIME_TYPE_PNG = "image/png"
        private fun getFileForResource(
            context: Context, @RawRes res: Int, outputDir: File,
            filename: String
        ): File? {
            val outputFile = File(outputDir, filename)
            val buffer = ByteArray(4096)
            var resourceReader: InputStream? = null
            return try {
                try {
                    resourceReader = context.getResources().openRawResource(res)
                    var dataWriter: OutputStream? = null
                    try {
                        dataWriter = FileOutputStream(outputFile)
                        while (true) {
                            val numRead: Int = resourceReader.read(buffer)
                            if (numRead <= 0) {
                                break
                            }
                            dataWriter.write(buffer, 0, numRead)
                        }
                        outputFile
                    } finally {
                        if (dataWriter != null) {
                            dataWriter.flush()
                            dataWriter.close()
                        }
                    }
                } finally {
                    if (resourceReader != null) {
                        resourceReader.close()
                    }
                }
            } catch (e: IOException) {
                null
            }
        }
    }
}