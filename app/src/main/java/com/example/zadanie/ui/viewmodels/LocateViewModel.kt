package com.example.zadanie.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.helpers.Evento
import com.example.zadanie.ui.viewmodels.data.MyLocation
import com.example.zadanie.ui.viewmodels.data.NearbyBar
import kotlinx.coroutines.launch

class LocateViewModel(private val repository: DataRepository): ViewModel() {
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>>
        get() = _message

    val loading = MutableLiveData(false)
    val emptyBar:MutableLiveData<Boolean?> = MutableLiveData(null)
    val startRest = MutableLiveData(false)
    val myLocation = MutableLiveData<MyLocation>(null)
    val myBar= MutableLiveData<NearbyBar>(null)

    private val _checkedIn = MutableLiveData<Evento<Boolean>>()
    val checkedIn: LiveData<Evento<Boolean>>
        get() = _checkedIn


    val bars : LiveData<List<NearbyBar>> = myLocation.switchMap {
        liveData {
            loading.postValue(true)
            it?.let {
                startRest.postValue(true)
                val b = repository.apiNearbyBars(it.lat,it.lon,{_message.postValue(Evento(it))})
                emit(b)
                startRest.postValue(false)
//                if b empty set
                if (b.isEmpty()){
                    emptyBar.postValue(true)
                }
                else{
                    emptyBar.postValue(false)
                }

                if (myBar.value==null){
                    myBar.postValue(b.firstOrNull())
                }
            } ?: emit(listOf())

            loading.postValue(false)
        }
    }

//    zapisanie sa do baru
    fun checkMe(){
        viewModelScope.launch {
            loading.postValue(true)
            myBar.value?.let {
                repository.apiBarCheckin(
                    it,
                    {_message.postValue(Evento(it))},
                    {_checkedIn.postValue(Evento(it))})
            }
            loading.postValue(false)
        }
    }

    fun show(msg: String){ _message.postValue(Evento(msg))}
}