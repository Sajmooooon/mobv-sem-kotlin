package com.example.zadanie.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.helpers.Evento
import com.example.zadanie.ui.viewmodels.data.NearbyBar
import com.example.zadanie.ui.widget.detailList.BarDetailItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: DataRepository) : ViewModel() {
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>>
        get() = _message

    val loading = MutableLiveData(false)


    val bar = MutableLiveData<NearbyBar>(null)
    val users = MutableLiveData<Int?>(null)

    val type = bar.map { it?.tags?.getOrDefault("amenity", "") ?: "" }
//    ziskanie id baru
//    val id = bar.map { it?.id }

    val details: LiveData<List<BarDetailItem>> = bar.switchMap {
        liveData {
            it?.let {
                emit(it.tags.map {
                    BarDetailItem(it.key, it.value)
                })
            } ?: emit(emptyList<BarDetailItem>())
        }
    }

//nacitaj bar
    fun loadBar(id: String) {
        if (id.isBlank())
            return
        viewModelScope.launch {
            loading.postValue(true)
            bar.postValue(repository.apiBarDetail(id) { _message.postValue(Evento(it)) })
            loading.postValue(false)
        }
        loadUsers(id)
    }

//  nacitaj userov nie v hlavnom procese
    fun loadUsers(id:String){
        if (id.isBlank())
            return
        CoroutineScope(Dispatchers.IO).launch{
            users.postValue(repository.getDbUsers(id){ _message.postValue(Evento(it)) })
        }
    }
}