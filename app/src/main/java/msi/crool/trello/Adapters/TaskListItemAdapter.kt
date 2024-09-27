package msi.crool.trello.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import msi.crool.trello.Activity.TaskListActivity
import msi.crool.trello.Model.Task
import msi.crool.trello.R
import java.util.Collections

open class TaskListItemAdapter(
    private val context: Context,
    private val list: ArrayList<Task>
) : RecyclerView.Adapter<TaskListItemAdapter.MyViewHolder>() {

    private var mPositionDraggedFrom=-1
    private var mPositionDraggedTo=-1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(15.toPx(), 0, 40.toPx(), 0)
        view.layoutParams = layoutParams
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        with(holder) {
            if (position == list.size - 1) {
                tvAddTaskList.visibility = View.VISIBLE
                llTaskItem.visibility = View.GONE
            } else {
                tvAddTaskList.visibility = View.GONE
                llTaskItem.visibility = View.VISIBLE
                tvTaskListTitle.text = model.title
            }

            tvAddTaskList.setOnClickListener {
                tvAddTaskList.visibility = View.GONE
                cvAddTaskListName.visibility = View.VISIBLE
            }

            ibCloseListName.setOnClickListener {
                tvAddTaskList.visibility = View.VISIBLE
                cvAddTaskListName.visibility = View.GONE
            }

            ibDoneListName.setOnClickListener {
                val listName = etTaskListName.text.toString()
                if (listName.isNotEmpty()) {
                    (context as? TaskListActivity)?.createTaskList(listName)
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            ibEditListName.setOnClickListener {
                etEditTaskListName.setText(model.title)
                llTitleView.visibility = View.GONE
                cvEditTaskListName.visibility = View.VISIBLE
            }

            ibCloseEditableView.setOnClickListener {
                llTitleView.visibility = View.VISIBLE
                cvEditTaskListName.visibility = View.GONE
            }

            ibDoneEditListName.setOnClickListener {
                val listName = etEditTaskListName.text.toString()
                if (listName.isNotEmpty()) {
                    (context as? TaskListActivity)?.updateTaskList(position, listName, model)
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            ibDeleteList.setOnClickListener {
                alertDelDialog(position, model.title!!)
            }

            tvAddCard.setOnClickListener {
                tvAddCard.visibility = View.GONE
                cvAddCard.visibility = View.VISIBLE
            }

            ibCloseCardName.setOnClickListener {
                tvAddCard.visibility = View.VISIBLE
                cvAddCard.visibility = View.GONE
            }

            ibDoneCardName.setOnClickListener {
                val cardName = etCardName.text.toString()
                if (cardName.isNotEmpty()) {
                    (context as? TaskListActivity)?.addCardToTaskList(position, cardName)
                } else {
                    Toast.makeText(context, "Please Enter Card Name.", Toast.LENGTH_SHORT).show()
                }
            }

            rvCardList.layoutManager = LinearLayoutManager(context)
            rvCardList.setHasFixedSize(true)

            val adapter = CardListItemAdapter(context, model.cards)
            rvCardList.adapter = adapter

            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter = adapter

            adapter.setOnClickListener(object : CardListItemAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    Log.d("TaskListItemAdapter", "Context is not an instance of TaskListActivity")
                    (context as? TaskListActivity)?.cardDetails(position, cardPosition) ?: run {
                        Log.d("TaskListItemAdapter", "Context is not an instance of TaskListActivity")
                    }
                }
            })
            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).addItemDecoration(dividerItemDecoration)
            val helper = ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition = dragged.adapterPosition
                    val targetPosition = target.adapterPosition

                    if (mPositionDraggedFrom == -1) {
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition
                    Collections.swap(list[position].cards, draggedPosition, targetPosition)

                    // move item in `draggedPosition` to `targetPosition` in adapter.
                    adapter.notifyItemMoved(draggedPosition, targetPosition)

                    return false // true if moved, false otherwise
                }

                // Called when a ViewHolder is swiped by the user.
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) { // remove from adapter
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)

                    if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {

                        (context as TaskListActivity).updateCardsInTaskList(
                            position,
                            list[position].cards
                        )
                    }

                    // Reset the global variables
                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }
                // END
            })
            helper.attachToRecyclerView(holder.itemView.findViewById(R.id.rv_card_list))


        }
    }

    private fun alertDelDialog(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are You Sure you want to Delete $title?")
        builder.setIcon(R.drawable.r_warning)
        builder.setPositiveButton("YES") { dialogInterface, _ ->
            dialogInterface.dismiss()
            (context as? TaskListActivity)?.deleteTasks(position)
        }
        builder.setNegativeButton("NO") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertD: AlertDialog = builder.create()
        alertD.setCancelable(false)
        alertD.show()
    }

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAddTaskList: View = view.findViewById(R.id.tv_add_task_list)
        val llTaskItem: View = view.findViewById(R.id.ll_task_item)
        val tvTaskListTitle: TextView = view.findViewById(R.id.tv_task_list_title)
        val cvAddTaskListName: View = view.findViewById(R.id.cv_add_task_list_name)
        val etTaskListName: EditText = view.findViewById(R.id.et_task_list_name)
        val ibCloseListName: ImageButton = view.findViewById(R.id.ib_close_list_name)
        val ibDoneListName: ImageButton = view.findViewById(R.id.ib_done_list_name)
        val ibEditListName: ImageButton = view.findViewById(R.id.ib_edit_list_name)
        val etEditTaskListName: EditText = view.findViewById(R.id.et_edit_task_list_name)
        val llTitleView: View = view.findViewById(R.id.ll_title_view)
        val cvEditTaskListName: View = view.findViewById(R.id.cv_edit_task_list_name)
        val ibCloseEditableView: ImageButton = view.findViewById(R.id.ib_close_editable_view)
        val ibDoneEditListName: ImageButton = view.findViewById(R.id.ib_done_edit_list_name)
        val ibDeleteList: ImageButton = view.findViewById(R.id.ib_delete_list)
        val tvAddCard: View = view.findViewById(R.id.tv_add_card)
        val cvAddCard: View = view.findViewById(R.id.cv_add_card)
        val etCardName: EditText = view.findViewById(R.id.et_card_name)
        val ibCloseCardName: ImageButton = view.findViewById(R.id.ib_close_card_name)
        val ibDoneCardName: ImageButton = view.findViewById(R.id.ib_done_card_name)
        val rvCardList: RecyclerView = view.findViewById(R.id.rv_card_list)
    }
}
