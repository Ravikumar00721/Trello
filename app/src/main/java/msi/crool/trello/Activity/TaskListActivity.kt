package msi.crool.trello.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import msi.crool.trello.Adapters.TaskListItemAdapter
import msi.crool.trello.FireStore.FireStoreClass
import msi.crool.trello.Model.Board
import msi.crool.trello.Model.Card
import msi.crool.trello.Model.Task
import msi.crool.trello.Model.User
import msi.crool.trello.R
import msi.crool.trello.Util.Constants
import msi.crool.trello.databinding.ActivityTaskListBinding

class TaskListActivity : BaseActivity() {

    private var binding: ActivityTaskListBinding? = null
    private lateinit var mBoardDetails:Board
    private lateinit var mBoardDocumentID:String
     lateinit var mAssignedMemberDetailList:ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)  // Use binding root view

        if (intent.hasExtra(Constants.documentID)) {
            mBoardDocumentID = intent.getStringExtra(Constants.documentID)!!
        }

        showProgressDialog()
        FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentID)
    }

//    override fun onResume() {
//        super.onResume()
//        if (dataHasChanged) { // Replace with actual condition to check if data has changed
//            showProgressDialog()
//            FireStoreClass().getBoardDetails(this@TaskListActivity, mBoardDocumentID)
//        }
//    }


    fun cardDetails(taskListPosition:Int,cardPosition:Int)
    {
        val intent=Intent(this,ActivityCardDetails::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBER_LIST,mAssignedMemberDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun updateCardsInTaskList(taskListPosition: Int,cards:ArrayList<Card>)
    {
       mBoardDetails.tasklist?.removeAt(mBoardDetails.tasklist?.size!! -1)
        mBoardDetails.tasklist!![taskListPosition].cards=cards

        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarTaskListActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.r_arrow)
            actionBar.title = mBoardDetails.name
        }

        binding?.toolbarTaskListActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    fun createTaskList(tasklistName: String) {
        val task = Task(tasklistName, FireStoreClass().getCurrentUserID())
        mBoardDetails.tasklist?.add(0, task)
        mBoardDetails.tasklist?.removeAt(mBoardDetails.tasklist!!.size - 1)

        if (mBoardDetails.documentID.isNotEmpty()) {  // Check if documentID is valid
            showProgressDialog()
            FireStoreClass().addUpdateTaskList(this, mBoardDetails)
        } else {
            Toast.makeText(this, "Invalid Board ID", Toast.LENGTH_SHORT).show()
        }
    }


    fun boardDetails(board: Board) {
        hideProgressDialog()
        // Initialize mBoardDetails before calling setupActionBar
        mBoardDetails = board
        setupActionBar()
        showProgressDialog()
        FireStoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdby)
        mBoardDetails.tasklist!![position] = task

        val addTaskList = mBoardDetails.tasklist!!.removeAt(mBoardDetails.tasklist!!.size - 1)
        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
        mBoardDetails.tasklist!!.add(addTaskList)

        binding?.rvTaskList?.adapter?.notifyItemChanged(position)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_members->{
                val intent=Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                startActivityForResult(intent, MEMBER_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode== MEMBER_REQUEST_CODE || requestCode== CARD_DETAILS_REQUEST_CODE)
        {
            FireStoreClass().getBoardDetails(this,mBoardDocumentID)
        }else
        {
            //todo
        }

    }

    fun boardMemberDetailList(list:ArrayList<User>)
    {
        mAssignedMemberDetailList=list
        hideProgressDialog()
        val addTaskList = Task("Add List")
        mBoardDetails.tasklist?.add(addTaskList)
        binding?.rvTaskList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)
        val adapter = TaskListItemAdapter(this, mBoardDetails.tasklist!!)
        binding?.rvTaskList?.adapter = adapter
    }

    fun addCardToTaskList(position: Int,cardName:String)
    {
        mBoardDetails.tasklist!!.removeAt(mBoardDetails.tasklist!!.size-1)
        val cardAssignedUsersList:ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FireStoreClass().getCurrentUserID())

        val card = Card(cardName,FireStoreClass().getCurrentUserID(),cardAssignedUsersList)

        val cardsList= mBoardDetails.tasklist!![position].cards
        cardsList.add(card)

        val task=Task(
            mBoardDetails.tasklist!![position].title,
            mBoardDetails.tasklist!![position].createdby,
            cardsList
        )

        mBoardDetails.tasklist!![position]=task
        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun deleteTasks(position:Int)
    {
        mBoardDetails.tasklist?.removeAt(position)
        mBoardDetails.tasklist?.removeAt(mBoardDetails.tasklist!!.size-1)
        showProgressDialog()
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun addUpdateTaskListSuccessor(){
        hideProgressDialog()
        showProgressDialog()
        FireStoreClass().getBoardDetails(this,mBoardDetails.documentID)
    }

    companion object{
        const val MEMBER_REQUEST_CODE:Int=13
        const val CARD_DETAILS_REQUEST_CODE:Int=14
    }
}
