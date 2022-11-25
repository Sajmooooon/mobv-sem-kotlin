package com.example.zadanie.ui.viewmodels

import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.helpers.Evento
import com.example.zadanie.ui.viewmodels.data.NearbyBar
import com.example.zadanie.ui.widget.detailList.BarDetailItem
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
    val id = bar.map { it?.id }

//    ziskanie userov
//    test id: 2192480912
//    fun text(){
//    val users : MutableLiveData<Int?> =
//        liveData {
//            loading.postValue(true)
//            repository.apiBarList { _message.postValue(Evento(it)) }
//            loading.postValue(false)
//            print("3195103137")
//            emitSource(repository.getDbUsers("3195103137"))
//        }
//    }




    val details: LiveData<List<BarDetailItem>> = bar.switchMap {
        liveData {
            it?.let {
                emit(it.tags.map {
                    BarDetailItem(it.key, it.value)
                })
            } ?: emit(emptyList<BarDetailItem>())
        }
    }
//    val users: LiveData<Int>  = liveData {
//        repository.apiBarList { _message.postValue(Evento(it)) }
//
//    }

//    fun loadUseers(id: String){
//        if (id.isBlank())
//            return
//        viewModelScope.launch {
//            loading.postValue(true)
//
//            emit(repository.getDbUsers(id))
//            loading.postValue(false)
//        }
//    }

   suspend fun loadUser(id: String) {
        if (id.isBlank())
            return
        viewModelScope.launch {
            loading.postValue(true)
            users.postValue(repository.getDbUsers(id))
//            users.postValue(repository.getDbUsers(id){ _message.postValue(Evento(it)) })
            loading.postValue(false)
        }


    }

    fun loadBar(id: String) {
        if (id.isBlank())
            return
        viewModelScope.launch {
            loading.postValue(true)
            bar.postValue(repository.apiBarDetail(id) { _message.postValue(Evento(it)) })
//            users.postValue(repository.getDbUsers(id){ _message.postValue(Evento(it)) })
            loading.postValue(false)
        }
//        text()
//        loadUsers(id)

    }

    fun loadUsers(id:String){
        if (id.isBlank())
            return
//        users.postValue(5)
//        viewModelScope.launch {
//            loading.postValue(true)
//            users.postValue(repository.getDbUsers(id){ _message.postValue(Evento(it)) })
//            loading.postValue(false)

//        }
    }
}