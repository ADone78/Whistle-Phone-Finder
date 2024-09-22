package com.whistle.phonefinder.tool.whistle

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.whistle.phonefinder.tool.R
import com.whistle.phonefinder.tool.LanguageChanged
import com.whistle.phonefinder.tool.currentAppLanguage
import com.whistle.phonefinder.tool.delayHandler
import com.whistle.phonefinder.tool.setAppLanguage
import com.whistle.phonefinder.tool.triggerRebirth


import java.util.*
import kotlin.collections.ArrayList

class LanguagesAdapter(
    private val list: ArrayList<LanguageModel>

) : RecyclerView.Adapter<LanguagesAdapter.LangViewHolder>(), Filterable
{
    var filterList = ArrayList<LanguageModel>(list)
    private var currentPosition = 0
    lateinit var context: Context

    class LangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var mCategoryTitle: TextView = itemView.findViewById(R.id.tv_Name)
        var mImageView: androidx.appcompat.widget.AppCompatImageView=
            itemView.findViewById(R.id.flagImage)
        var mTickImageView: ImageView = itemView.findViewById(R.id.tick_img)
        var mCardView: CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): LangViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.language_item_layout, parent, false)
        return LangViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: LangViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val obj: LanguageModel = filterList[position]


        if (obj.name != null) {
            try {
                holder.mCategoryTitle.text = obj.name
            } catch (e: Exception) {
                println(e.toString())
            }
        }
        holder.mCardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.cardbgColor))


        holder.mTickImageView.isVisible =
            holder.itemView.context.currentAppLanguage(holder.itemView.context) == obj.name.toString()

        holder.itemView.context.delayHandler(100) {
            if (holder.mTickImageView.isVisible) holder.mCardView.setCardBackgroundColor(
                holder.itemView.context.getColor(
                    R.color.cardBgLanguage
                )
            )
        }

        try {
            holder.mImageView.setImageResource(obj.flag)
        } catch (e: Exception) {
            println(e.toString())
        }

        holder.itemView.setOnClickListener {
            currentPosition = 0
            currentPosition = position
            notifyDataSetChanged()

            LanguageChanged.changeLanguage(holder.itemView.context, obj.name.toString())
           holder.itemView.context.setAppLanguage(holder.itemView.context, obj.name.toString())
            holder.itemView.context.triggerRebirth(holder.itemView.context)
        }
    }

    override fun getItemCount(): Int {
        Log.d("Filter", "Size:" + filterList.size)
        return filterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = ArrayList<LanguageModel>()
                if (charSearch.isEmpty()) {
                    resultList.addAll(list)
                } else {
                    filterList.forEach {
                        if (it.name!!.contains(charSearch, true)) {
                            resultList.add(it)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = resultList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterList = results?.values as ArrayList<LanguageModel>
                notifyDataSetChanged()
            }

        }
    }


}

