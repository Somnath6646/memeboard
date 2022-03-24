package fosster.family.memeboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import fosster.family.memeboard.datamodels.Memelist
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun switchKeyboard(view: View): View {
        val imeManager: InputMethodManager = applicationContext.getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imeManager.showInputMethodPicker()
        return view
    }

    
}