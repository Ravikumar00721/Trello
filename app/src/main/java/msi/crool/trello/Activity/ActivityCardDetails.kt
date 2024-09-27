package msi.crool.trello.Activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import msi.crool.trello.Adapters.CardMemberListItemsAdapter
import msi.crool.trello.FireStore.FireStoreClass
import msi.crool.trello.Model.Board
import msi.crool.trello.Model.Card
import msi.crool.trello.Model.SelectedMembers
import msi.crool.trello.Model.Task
import msi.crool.trello.Model.User
import msi.crool.trello.R
import msi.crool.trello.Util.Constants
import msi.crool.trello.databinding.ActivityCardDetailsBinding
import msi.crool.trello.dialogs.MembersListDialog
import msi.crool.trello.dialogs.colorDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ActivityCardDetails : BaseActivity() {
    private var binding:ActivityCardDetailsBinding?=null
    private var mTaskListPosition=-1
    private var mCardPosition=-1
    private var mBoardDetail:Board?=null
    private var mSelectedColor=""
    private lateinit var mAssignedMemberList:ArrayList<User>
    private  var mSelectedDueDateMilliSec:Long=0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TaskListItemAdapter3", "Context is not an instance of TaskListActivity")
        super.onCreate(savedInstanceState)
        binding=ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getintentData()
        setupActionBar()
        binding?.etNameCardDetails?.setText(mBoardDetail?.tasklist?.get(mTaskListPosition)?.cards?.get(mCardPosition)?.name)
        binding?.etNameCardDetails?.setSelection(binding?.etNameCardDetails!!.text.toString().length)
        mSelectedColor=mBoardDetail?.tasklist!![mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty())
        {
            setColor()
        }

        binding?.btnUpdateCardDetails?.setOnClickListener {
            if(binding?.etNameCardDetails?.text!!.toString().isNotEmpty())
            {
                UpdateCArdDetails()
            }else{
                Toast.makeText(this,"Please Enter Card Name",Toast.LENGTH_SHORT).show()
            }
        }

        binding?.tvSelectLabelColor?.setOnClickListener {
            labelColorsListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliSec= mBoardDetail!!.tasklist!![mTaskListPosition].cards[mCardPosition].dueDate

        if (mSelectedDueDateMilliSec > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSec))
            findViewById<TextView>(R.id.tv_select_due_date).text = selectedDate
        }

        findViewById<TextView>(R.id.tv_select_due_date).setOnClickListener {
            showDataPicker()
        }
    }

    private fun deleteCards()
    {
        val cardList:ArrayList<Card> =mBoardDetail?.tasklist!![mTaskListPosition].cards
        cardList.removeAt(mCardPosition)
        val taskList:ArrayList<Task> = mBoardDetail!!.tasklist!!
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards=cardList

        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this,mBoardDetail!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_delete_member->
            {
                alertDialogForDeleteCard(mBoardDetail?.tasklist!![mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun colorsList():ArrayList<String>{
        val colorsList:ArrayList<String> = ArrayList()
        colorsList.add("#FF5733")  // Red-Orange
        colorsList.add("#33FF57")  // Green
        colorsList.add("#3357FF")  // Blue
        colorsList.add("#FF33A1")  // Pink
        colorsList.add("#FFD700")  // Gold
        colorsList.add("#8A2BE2")  // Blue-Violet
        colorsList.add("#00FFFF")  // Cyan

        return colorsList
    }

    private fun setColor()
    {
        binding?.tvSelectLabelColor?.text=""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun alertDialogForDeleteCard(cardName:String)
    {
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Delete Card")
        builder.setMessage("Are You Sure wnt to Delete! $cardName")
        builder.setIcon(R.drawable.r_warning)
        builder.setPositiveButton("YES")
        {
            dialog,which->
            deleteCards()
            dialog.dismiss()
        }
        builder.setNegativeButton("NO")
        {
           dialog,which->
            dialog.dismiss()
        }
        val alaertD:AlertDialog=builder.create()
        alaertD.setCancelable(false)
        alaertD.show()

    }

    fun addUpdateTaskListSuccessor()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarCardDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true) // Show the back arrow icon
            actionBar.setHomeAsUpIndicator(R.drawable.r_arrow) // Optionally set a custom icon

            val taskList = mBoardDetail?.tasklist
            if (taskList != null && mTaskListPosition in taskList.indices) {
                val task = taskList[mTaskListPosition]
                if (mCardPosition in task.cards.indices) {
                    actionBar.title = task.cards[mCardPosition].name
                } else {
                    Log.e("ActivityCardDetails", "Card position $mCardPosition is out of bounds")
                    actionBar.title = "Invalid Card"
                }
            } else {
                Log.e("ActivityCardDetails", "Task list position $mTaskListPosition is out of bounds")
                actionBar.title = "Invalid Task List"
            }
        }

        binding?.toolbarCardDetailsActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun getintentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetail = intent.getParcelableExtra(Constants.BOARD_DETAIL)
            Log.d("ActivityCardDetails", "Board Detail: $mBoardDetail")
        } else {
            Log.e("ActivityCardDetails", "No BOARD_DETAIL extra found")
        }

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
            Log.d("ActivityCardDetails", "Task List Position: $mTaskListPosition")
        } else {
            Log.e("ActivityCardDetails", "No TASK_LIST_ITEM_POSITION extra found")
        }

        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
            Log.d("ActivityCardDetails", "Card Position: $mCardPosition")
        } else {
            Log.e("ActivityCardDetails", "No CARD_LIST_ITEM_POSITION extra found")
        }

        if (intent.hasExtra(Constants.BOARD_MEMBER_LIST)) {
            mAssignedMemberList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBER_LIST)!!
        }
    }

    private fun UpdateCArdDetails()
    {
        val card= Card(
            binding?.etNameCardDetails?.text.toString(),
            mBoardDetail?.tasklist!![mTaskListPosition].cards[mCardPosition].createdby,
            mBoardDetail?.tasklist!![mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSec
        )

        val taskList:ArrayList<Task> = mBoardDetail!!.tasklist!!
        taskList.removeAt(taskList.size-1)

        mBoardDetail?.tasklist!![mTaskListPosition].cards[mCardPosition]=card

        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this,mBoardDetail!!)
    }

    private fun labelColorsListDialog()
    {
        val colors:ArrayList<String> =colorsList()
        val listDialog =object :colorDialog(
            this,
            colors,
            mSelectedColor,
            resources.getString(R.string.str_select_label_color)){
            override fun onItemSelected(color: String) {
                mSelectedColor=color
                setColor()
            }
            }
        listDialog.show()

    }

    private fun membersListDialog() {

        // Here we get the updated assigned members list
        val cardAssignedMembersList =
           mBoardDetail?.tasklist!![mTaskListPosition].cards[mCardPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            // Here we got the details of assigned members list from the global members list which is passed from the Task List screen.
            for (i in mAssignedMemberList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mAssignedMemberList[i].id == j) {
                        mAssignedMemberList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mAssignedMemberList.indices) {
                mAssignedMemberList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this@ActivityCardDetails,
            mAssignedMemberList,
            "SELECT MEMBER"
        ) {
            override fun onItemSelected(user: User, action: String) {
                if(action== Constants.SELECT)
                {
                    if(!mBoardDetail!!.tasklist!![mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id))
                    {
                        mBoardDetail!!.tasklist!![mTaskListPosition].cards[mCardPosition].assignedTo.add(
                            user.id!!
                        )
                    }
                }else
                {
                    mBoardDetail!!.tasklist!![mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)

                    for (i  in mAssignedMemberList.indices)
                    {
                        if(mAssignedMemberList[i].id==user.id)
                        {
                            mAssignedMemberList[i].selected=false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }
    private fun setupSelectedMembersList() {

        // Assigned members of the Card.
        val cardAssignedMembersList =
            mBoardDetail?.tasklist?.get(mTaskListPosition)?.cards!![mCardPosition].assignedTo

        // A instance of selected members list.
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        // Here we got the detail list of members and add it to the selected members list as required.
        for (i in mAssignedMemberList.indices) {
            for (j in cardAssignedMembersList) {
                if (mAssignedMemberList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mAssignedMemberList[i].id!!,
                        mAssignedMemberList[i].image!!
                    )

                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {

            // This is for the last item to show.
            selectedMembersList.add(SelectedMembers("", ""))

            binding?.tvSelectMembers?.visibility = View.GONE
            binding?.rvSelectedMembersList?.visibility = View.VISIBLE

            binding?.rvSelectedMembersList?.layoutManager = GridLayoutManager(this@ActivityCardDetails, 6)
            val adapter = CardMemberListItemsAdapter(this@ActivityCardDetails, selectedMembersList,true)
            binding?.rvSelectedMembersList?.adapter = adapter
            adapter.setOnClickListener(object :
                CardMemberListItemsAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }
            })
        } else {
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.visibility = View.GONE
        }
    }

    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                // Selected date it set to the TextView to make it visible to user.
                findViewById<TextView>(R.id.tv_select_due_date).text = selectedDate
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliSec = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show() // It is used to show the datePicker Dialog.
    }

}