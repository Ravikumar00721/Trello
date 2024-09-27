package msi.crool.trello.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import msi.crool.trello.Activity.TaskListActivity
import msi.crool.trello.Model.Card
import msi.crool.trello.Model.SelectedMembers
import msi.crool.trello.R

open class CardListItemAdapter(
    private val context: Context,
    private val list: ArrayList<Card>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {

            val labelColorView = holder.itemView.findViewById<View>(R.id.view_label_color)
            if (model.labelColor.isNotEmpty()) {
                labelColorView?.visibility = View.VISIBLE
                labelColorView?.setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                labelColorView?.visibility = View.GONE
            }

            holder.tvCardName.text = model.name

            if ((context as TaskListActivity).mAssignedMemberDetailList.size > 0) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                for (i in context.mAssignedMemberDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.mAssignedMemberDetailList[i].id == j) {
                            val selectedMember = SelectedMembers(
                                context.mAssignedMemberDetailList[i].id!!,
                                context.mAssignedMemberDetailList[i].image!!
                            )
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {

                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdby) {
                        holder.itemView.findViewById<View>(R.id.rv_card_selected_members).visibility = View.GONE
                    } else {
                        holder.itemView.findViewById<View>(R.id.rv_card_selected_members).visibility = View.VISIBLE

                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members).layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter = CardMemberListItemsAdapter(context, selectedMembersList, false)
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members).adapter = adapter
                        adapter.setOnClickListener(object :
                            CardMemberListItemsAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                } else {
                    holder.itemView.findViewById<View>(R.id.rv_selected_members_list).visibility = View.GONE
                }

            }

            holder.itemView.setOnClickListener {
                onClickListener?.onClick(position)
            }
        }
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCardName: TextView = view.findViewById(R.id.tv_card_name)
    }
}

