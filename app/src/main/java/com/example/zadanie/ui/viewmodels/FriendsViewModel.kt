package com.example.zadanie.ui.viewmodels

import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Contact
import com.example.zadanie.helpers.Evento
import kotlinx.coroutines.launch

class FriendsViewModel(private val repository: DataRepository): ViewModel() {
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>>
        get() = _message

    val loading = MutableLiveData(false)


    val friends: LiveData<List<Contact>?> =
        liveData {
            loading.postValue(true)
//            nacita data z webservisu a ulozi do db  cez cache
            repository.apiFriendsList { _message.postValue(Evento(it)) }
            loading.postValue(false)
//            vytiahne data z db cez cache
            emitSource(repository.dbFriends())
        }

//    refresh
    fun refreshData(){
        viewModelScope.launch {
            loading.postValue(true)
            repository.apiFriendsList { _message.postValue(Evento(it)) }
            loading.postValue(false)
        }
    }

    fun show(msg: String){ _message.postValue(Evento(msg))}
}