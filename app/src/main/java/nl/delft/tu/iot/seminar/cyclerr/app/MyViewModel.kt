package nl.delft.tu.iot.seminar.cyclerr.app

import android.util.Log
import androidx.lifecycle.ViewModel

private const val TAG = "MyViewModel"

class MyViewModel : ViewModel() {

    var currentIndex: Int =0
        private set

    init {
        Log.d(TAG, "ViewModel instance created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel instance about to be destroyed")
    }

    fun next(){
        currentIndex++
    }

}