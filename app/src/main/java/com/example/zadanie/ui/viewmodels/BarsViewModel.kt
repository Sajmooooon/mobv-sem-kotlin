package com.example.zadanie.ui.viewmodels

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

//    pri stlaceni loc a ziskani permissi na automaticku navigaciu
    private var _locationBtn = false
    val locationBtn: Boolean
        get() = _locationBtn

    //    pri stlaceni loc a ziskani permissi na automaticku navigaciu
//    private var _distanceBtn = false
//    val distanceBtn: Boolean
//        get() = _distanceBtn

//    na kontrolu aby nedoslo k nekonecnemu cyklu
    private var _alreadySorted = false
    val alreadySorted: Boolean
        get() = _alreadySorted

    val myLocation = MutableLiveData<MyLocation>(null)
    val sortType: MutableLiveData<String> = MutableLiveData("barAsc")
    val loading = MutableLiveData(false)
    val bars = MutableLiveData<List<BarItem>?>(null)

//    inicializacny blok
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
//        nastavi sa true aby nedoslo k zacykleniu pri observeri
        _alreadySorted = true
//        ziska sa vzdialenost ak nie je bar null a aj mylocation
        getDistance()

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

    fun switchSorted(){
        _alreadySorted = false
    }

    //    kliknutie tlacidla na prepnutie lokacii - aby automaticky preplo po prijati permissi
    fun switchToLocation(value: Boolean) {
        _locationBtn = value
    }

//    pri prijati permissi a kliknuti na distance automaticky sort aby nemusel 2krat klikat
//    fun switchSort(value: Boolean){
//        _distanceBtn = value
//    }


//    ak bolo predtym Asc zmen na Desc toho isteho typu - vice versa
    fun sortBy(cond1: String, cond2:String) {
        if (sortType.value == cond1) sortType.value = cond2 else sortType.value = cond1
    }

//    ziska vzdialenost od nas ku podnikom
    fun getDistance() {
        myLocation.value?.let{ loc->
            bars.value?.let { bar ->
                val tmp = bar.map {
                    it.distance = it.distanceTo(loc)
                    it
                }
                bars.postValue(tmp)
            }
        }
    }

//    refresh dat
    fun refreshData() {
        viewModelScope.launch {
            loading.postValue(true)
            repository.apiBarList { _message.postValue(Evento(it)) }
            loading.postValue(false)
        }
    }


    fun show(msg: String) {
        _message.postValue(Evento(msg))
    }
}