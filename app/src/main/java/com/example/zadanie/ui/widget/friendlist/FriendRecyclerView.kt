package com.example.zadanie.ui.widget.friendlist

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Contact
import com.example.zadanie.ui.fragments.BarDetailFragmentDirections
import com.example.zadanie.ui.fragments.BarsFragmentDirections

class FriendRecyclerView : RecyclerView {
    private lateinit var friendsAdapter: FriendsAdapter
    /**
     * Default constructor
     *
     * @param context context for the activity
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * Constructor for XML layout
     *
     * @param context activity context
     * @param attrs   xml attributes
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        friendsAdapter = FriendsAdapter(object : FriendsEvents {
            override fun onFriendClick(contact: Contact) {
                if  (!contact.bar_id.isNullOrEmpty()){
                    this@FriendRecyclerView.findNavController().navigate(
                        BarDetailFragmentDirections.actionToDetail(contact.bar_id)

                    )
                }
//                else{
//                    this@FriendRecyclerView.findNavController().navigate(
//                        BarDetailFragmentDirections.actionToDetail(contact.bar_id)
//                    )
//                }

            }
        })
        adapter = friendsAdapter
    }
}

@BindingAdapter("friendItems")
fun FriendRecyclerView.applyItems(
    friends: List<Contact>?
) {
    (adapter as FriendsAdapter).items = friends ?: emptyList()
}