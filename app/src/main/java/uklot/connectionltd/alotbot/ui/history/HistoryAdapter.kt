package uklot.connectionltd.alotbot.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import uklot.connectionltd.alotbot.databinding.ItemHistoryBinding
import uklot.connectionltd.alotbot.model.GenerationRecord

class HistoryAdapter(
    private val onItemClick: (GenerationRecord) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private val items = mutableListOf<GenerationRecord>()

    fun submit(list: List<GenerationRecord>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: GenerationRecord,
            onItemClick: (GenerationRecord) -> Unit
        ) {
            binding.image.load(item.imagePath)
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
