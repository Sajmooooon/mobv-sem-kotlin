package com.example.zadanie.ui.widget.barlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.helpers.autoNotify
import com.google.android.material.chip.Chip
import kotlin.properties.Delegates

class BarsAdapter(val events: BarsEvents? = null) :
    RecyclerView.Adapter<BarsAdapter.BarItemViewHolder>() {
    var items: List<BarItem> by Delegates.observable(emptyList()) { _, old, new ->
        autoNotify(old, new) { o, n -> o.id.compareTo(n.id) == 0 }
    }

//    create new view holder for recycler  view
//    represent single list item
//    parent - view groupt that new list item view will attached as child
//    viewType- importatn when miltiple item view types in same recycler
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarItemViewHolder {
        return BarItemViewHolder(parent)
    }

//    velkost datasetu
    override fun getItemCount(): Int {
        return items.size
    }
// called by layout managaer in order to replace contents of list item view
//  holder vytvoreny itemviewholderom a position - reprezentuje poziciu itemu v liste
    override fun onBindViewHolder(holder: BarItemViewHolder, position: Int) {
//    binduje udaje cez bind
        holder.bind(items[position], events)
    }

    class BarItemViewHolder(
        private val parent: ViewGroup,
//        LayoutInflater vie ako inflate xml layout into hierarchy of objects
//        pridame xml layout resource ID a parent view
//        nakoniec false, lebo recycler prid=a item do view hierarchy ked je cas
        itemView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.bar_item,
            parent,
            false)
        ) : RecyclerView.ViewHolder(itemView){

        fun bind(item: BarItem, events: BarsEvents?) {
            itemView.findViewById<TextView>(R.id.name).text = item.name
            itemView.findViewById<TextView>(R.id.count).text = item.users.toString()
            itemView.findViewById<Chip>(R.id.type).text = item.type
//            var img = getImage(item.type)
            itemView.findViewById<ImageView>(R.id.bar_img).setImageResource(getImage(item.type))
            itemView.setOnClickListener { events?.onBarClick(item) }
        }

        private fun getImage(type: String): Int{
            var img = R.drawable.ic_baseline_fastfood_24
            when(type){
                "bar" -> img = R.drawable.ic_baseline_local_bar_24
                "pub" -> img = R.drawable.ic_baseline_table_bar
                "nightclub" -> img = R.drawable.ic_baseline_nightlife_24
                "restaurant" -> img = R.drawable.ic_baseline_restaurant_24
                "cafe" -> img = R.drawable.ic_baseline_local_cafe_24
            }
            return img
        }
    }


}