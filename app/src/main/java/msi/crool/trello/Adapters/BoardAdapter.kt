package msi.crool.trello.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import msi.crool.trello.Model.Board
import msi.crool.trello.R
import msi.crool.trello.databinding.ItemBoardBinding

// Adapter class for Board Items in the MainActivity.
open class BoardItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
) : RecyclerView.Adapter<BoardItemsAdapter.MyViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the item layout using View Binding
        val binding = ItemBoardBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        holder.bind(model)

        // Alternate background color based on the item's position
        val backgroundColor = if (position % 2 == 0) {
            ContextCompat.getColor(context, R.color.even_color) // replace with your color resource
        } else {
            ContextCompat.getColor(context, R.color.odd_color) // replace with your color resource
        }

        holder.itemView.setBackgroundColor(backgroundColor)

        // Set the click listener for the item view
        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position, model)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    // ViewHolder class using View Binding
    inner class MyViewHolder(private val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(board: Board) {
            Glide
                .with(context)
                .load(board.image)
                .centerCrop()
                .placeholder(R.drawable.r_person)
                .into(binding.ivBoardImage)

            binding.tvName.text = board.name
            binding.tvCreatedBy.text = "Created By: ${board.createdBy}"
        }
    }
}
