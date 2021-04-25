package com.raywenderlich.android.creatures.ui

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.creatures.R
import com.raywenderlich.android.creatures.app.inflate
import com.raywenderlich.android.creatures.model.Creature
import com.raywenderlich.android.creatures.model.Favorites
import kotlinx.android.synthetic.main.list_item_creature.view.*
import java.util.*

class CreatureAdapter(
    private val creatures: MutableList<Creature>,
    private val itemDragListener: ItemDragListener
) :
    RecyclerView.Adapter<CreatureAdapter.ViewHolder>(), ItemTouchHelperListener {

    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(itemView: View) : View.OnClickListener,
        RecyclerView.ViewHolder(itemView), ItemSelectedListener {
        private lateinit var creature: Creature

        init {
            itemView.setOnClickListener(this)
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = absoluteAdapterPosition
                override fun getSelectionKey(): Long? = itemId
            }

        @SuppressLint("ClickableViewAccessibility")
        fun bind(creature: Creature, isSelected: Boolean) {
            this.creature = creature
            val context = itemView.context
            itemView.creatureImage.setImageResource(
                context.resources.getIdentifier(creature.uri, null, context.packageName)
            )
            itemView.fullName.text = creature.fullName
            itemView.nickname.text = creature.nickname
            if (isSelected) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.selectedItem)
                )
            } else {
                itemView.setBackgroundColor(0)
            }
            itemView.handle.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    itemDragListener.onItemDrag(this)
                }
                false
            }
            animateView(itemView)
        }

        override fun onClick(view: View?) {
            view?.let {
                val context = it.context
                val intent = CreatureActivity.newIntent(context, creature.id)
                context.startActivity(intent)
            }
        }

        override fun onItemSelected() {
            itemView.listItemContainer.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.selectedItem
                )
            )
        }

        override fun onItemCleared() {
            itemView.listItemContainer.setBackgroundColor(0)
        }

        private fun animateView(viewToAnimate: View) {
            if (viewToAnimate.animation == null) {
                val animation = AnimationUtils.loadAnimation(viewToAnimate.context, R.anim.scale_xy)
                viewToAnimate.animation = animation
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.list_item_creature))
    }

    override fun getItemCount() = creatures.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isSelected = tracker?.isSelected(position.toLong()) ?: false
        holder.bind(creatures[position], isSelected)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("NotifyDataSetChanged")
    fun updateCreatures(creatures: List<Creature>) {
        this.creatures.clear()
        this.creatures.addAll(creatures)
        notifyDataSetChanged()
    }

    override fun onItemMove(
        recyclerView: RecyclerView,
        fromPosition: Int,
        toPosition: Int
    ): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(creatures, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(creatures, i, i - 1)
            }
        }
        Favorites.saveFavorites(creatures.map { it.id }, recyclerView.context)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(viewHolder: RecyclerView.ViewHolder, position: Int) {
        Favorites.removeFavorite(creatures[position], viewHolder.itemView.context)
        creatures.removeAt(position)
        notifyItemRemoved(position)
    }

}