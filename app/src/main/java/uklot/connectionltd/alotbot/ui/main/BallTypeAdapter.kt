package uklot.connectionltd.alotbot.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uklot.connectionltd.alotbot.databinding.ItemBallTypeBinding
import uklot.connectionltd.alotbot.model.BallType

class BallTypeAdapter(
    private val onClick: (BallType) -> Unit
) : RecyclerView.Adapter<BallTypeAdapter.BallTypeViewHolder>() {

    private val items = BallType.entries.toList()
    private var selectedType: BallType? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BallTypeViewHolder {
        val binding = ItemBallTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BallTypeViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BallTypeViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, selected = selectedType == item, onClick = {
            selectedType = item
            notifyDataSetChanged()
            onClick(item)
        })
    }

    class BallTypeViewHolder(
        private val binding: ItemBallTypeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BallType, selected: Boolean, onClick: () -> Unit) {
            binding.ballIcon.setImageResource(item.iconRes)
            binding.ballTitle.setText(item.titleRes)
            binding.root.isSelected = selected
            binding.root.setOnClickListener { onClick() }
        }
    }
}
