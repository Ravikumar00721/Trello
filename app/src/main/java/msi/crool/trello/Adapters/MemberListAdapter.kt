package msi.crool.trello.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import msi.crool.trello.Model.User
import msi.crool.trello.R
import msi.crool.trello.Util.Constants

class MemberListAdapter(private val context: Context,private var list:ArrayList<User>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>()
  {
      private var onClickListener: OnClickListener? = null

      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
          return MyViewHolder(
              LayoutInflater.from(context).inflate(
                  R.layout.item_memebers,
                  parent,
                  false
              )
          )
      }

      fun setOnClickListener(onClickListener: OnClickListener) {
          this.onClickListener = onClickListener
      }
      override fun getItemCount(): Int {
          return list.size
      }

      override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
          val model=list[position]

          if(holder is MyViewHolder)
          {
              Glide
                  .with(context)
                  .load(model.image)
                  .centerCrop()
                  .placeholder(R.drawable.r_person)
                  .into(holder.itemView.findViewById(R.id.iv_member_image))
              holder.itemView.findViewById<TextView>(R.id.tv_member_name).text=model.name
              holder.itemView.findViewById<TextView>(R.id.tv_member_email).text=model.email

              if (model.selected == true) {
                  holder.itemView.findViewById<View>(R.id.iv_selected_member).visibility = View.VISIBLE
              } else {
                  holder.itemView.findViewById<View>(R.id.iv_selected_member).visibility = View.GONE
              }

              holder.itemView.setOnClickListener {

                  if (onClickListener != null) {
                      // TODO (Step 3: Pass the constants here according to the selection.)
                      // START
                      if (model.selected == true) {
                          onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                      } else {
                          onClickListener!!.onClick(position, model, Constants.SELECT)
                      }
                      // END
                  }
              }
          }


      }
      class MyViewHolder(view: View):RecyclerView.ViewHolder(view)

      interface OnClickListener {
          fun onClick(position: Int, user: User, action: String)
      }
  }