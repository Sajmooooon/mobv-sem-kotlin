package com.example.zadanie.ui.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.helpers.Evento
import com.example.zadanie.ui.viewmodels.data.MyLocation
import kotlinx.coroutines.launch

class BarsViewModel(private val repository: DataRepository) : ViewModel() {
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>>
        get() = _message

    val locationBtn = MutableLiveData<Boolean>(false)
    val myLocation = MutableLiveData<MyLocation>(null)
    var sortType: MutableLiveData<String> = MutableLiveData("barAsc")
    val loading = MutableLiveData(false)


    val bars = MutableLiveData<List<BarItem>?>(null)

    init {
        viewModelScope.launch {
            loading.postValue(true)
            repository.apiBarList { _message.postValue(Evento(it)) }
            loading.postValue(false)
            repository.dbBars().asFlow().collect {
                bars.postValue(it)
            }
        }

    }


//    val bars: LiveData<List<BarItem>> = Transformations.switchMap(_sortType) { sort ->
//        when (sort) {
//            "barAsc" -> repository.getBarsAsc()
//            "barDesc" -> repository.getBarsDesc()
//            "usersAsc" -> repository.getUsersAsc()
//            "usersDesc" -> repository.getUsersDesc()
//            "distanceDesc" -> repository.getDistanceDesc()
//            else -> {
//                repository.getBarsAsc()
//            }
//        }
//
//    }


    fun sort() {
        bars.value?.let { bar ->
            val tmp: List<BarItem>
            when (sortType.value) {
                "barAsc" -> {
                    tmp = bar.sortedBy { it.name }
                }
                "barDesc" -> {
                    tmp = bar.sortedByDescending { it.name }
                }
                "usersAsc" -> {
                    tmp = bar.sortedBy { it.users }
                }
                "usersDesc" -> {
                    tmp = bar.sortedByDescending { it.users }
                }
                "distanceAsc" -> {
                    tmp = bar.sortedBy { it.distance }
                }
                "distanceDesc" -> {
                    tmp = bar.sortedByDescending { it.distance }
                }

                else -> {
                    tmp = bar.sortedBy { it.name }
                }
            }
            bars.postValue(tmp)
        }

    }

    //    kliknutie tlacidla na prepnutie lokacii - aby automaticky preplo po prijati permissi
    fun switch() {
        locationBtn.postValue(true)
    }


    fun sortByBar() {
        if (sortType.value == "barAsc") sortType.value = "barDesc" else sortType.value = "barAsc"
    }

    fun sortByUsers() {
        if (sortType.value == "usersAsc") sortType.value = "usersDesc" else sortType.value = "usersAsc"
    }

    fun sortBy(cond1: String, cond2:String) {
        if (sortType.value == cond1) sortType.value = cond2 else sortType.value = cond1
    }

    fun getDistance() {
        myLocation.value?.let{ loc->
            bars.value?.let { bar ->
                val tmp = bar.map {
                    it.distance = it.distanceTo(loc)
                    it
                }

                bars.postValue(tmp)
                Log.d("tmp","tmp"+bars.value)
            }
        }
        sort()
    }

    fun refreshData() {
        viewModelScope.launch {
            loading.postValue(true)
//            repository.apiBarListSorted()
            repository.apiBarList { _message.postValue(Evento(it)) }
            loading.postValue(false)
        }
//        bars =
//        liveData {
//            loading.postValue(true)
//
//            loading.postValue(false)
//
//            emitSource(repository.getBarsDesc())
//        }

    }


    fun show(msg: String) {
        _message.postValue(Evento(msg))
    }
}