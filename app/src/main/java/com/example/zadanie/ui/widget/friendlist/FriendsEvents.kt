package com.example.zadanie.ui.widget.friendlist

import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Contact

interface FriendsEvents {
    fun onFriendClick(contact: Contact)
}