package msi.crool.trello.FireStore

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import msi.crool.trello.Activity.ActivityCardDetails
import msi.crool.trello.Activity.CreateBoardActivity
import msi.crool.trello.Activity.MainActivity
import msi.crool.trello.Activity.MembersActivity
import msi.crool.trello.Activity.MyProfileActivity
import msi.crool.trello.Activity.SignInActivity
import msi.crool.trello.Activity.SignUpActivity
import msi.crool.trello.Activity.TaskListActivity
import msi.crool.trello.Model.Board
import msi.crool.trello.Model.User
import msi.crool.trello.Util.Constants

class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, user: User) {
        mFireStore.collection(Constants.Users)
            .document(getCurrentUserID()).set(user, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener { }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        // Ensure the document ID is correctly set
        if (board.documentID.isNotEmpty()) {
            val taskListHashMap = HashMap<String, Any>()
            taskListHashMap[Constants.TASK_LIST] = board.tasklist!!

            mFireStore.collection(Constants.Boards)
                .document(board.documentID)
                .update(taskListHashMap)
                .addOnSuccessListener {
                    if(activity is TaskListActivity) {
                        activity.addUpdateTaskListSuccessor()
                    }
                    else if(activity is ActivityCardDetails){
                        activity.addUpdateTaskListSuccessor()
                    }
                }.addOnFailureListener { e ->
                    if(activity is TaskListActivity) {
                        activity.hideProgressDialog()
                    }
                    else if(activity is ActivityCardDetails){
                        activity.hideProgressDialog()
                    }
                    Log.e("Firestore Error", "Error updating task list", e)
                }
        } else {
            // Handle the case where the document ID is not set
            Log.e("Firestore Error", "Document ID is not set, cannot update task list")
        }
    }


    fun getBoardDetails(activity: TaskListActivity, documentID: String) {
        mFireStore.collection(Constants.Boards)
            .document(documentID)
            .get()
            .addOnSuccessListener { document ->
                val board = document.toObject(Board::class.java)
                board?.documentID = document.id
                activity.boardDetails(board!!)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
            }
    }

    fun getBoardList(activity: MainActivity) {
        mFireStore.collection(Constants.Boards)
            .whereArrayContains(Constants.assignedTo, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val boardList: ArrayList<Board> = ArrayList()
                for (i in document.documents) {
                    val board = i.toObject(Board::class.java)
                    board?.documentID = i.id
                    boardList.add(board!!)
                }
                activity.populateBoardsListToUI(boardList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
            }
    }

    fun updateUserProfileData(activity: Activity ,userHasMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.Users)
            .document(getCurrentUserID())
            .update(userHasMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Profile Data Updated Successfully", Toast.LENGTH_SHORT).show()
                when(activity)
                {
                    is MainActivity->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity->{
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(activity, "Error!!", Toast.LENGTH_SHORT).show()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        val documentReference = mFireStore.collection(Constants.Boards).document()
        board.documentID = documentReference.id

        documentReference
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board Created Successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Toast.makeText(activity, "Error!!", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false) {
        val userId = getCurrentUserID()
        if (userId.isNotEmpty()) {
            mFireStore.collection(Constants.Users)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)
                    if (loggedInUser != null) {
                        // Handle different activity cases
                        when (activity) {
                            is SignInActivity -> {
                                activity.signInSuccess(loggedInUser)
                            }
                            is MainActivity -> {
                                activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                            }
                            is MyProfileActivity -> {
                                activity.setUserDataInUI(loggedInUser)
                            }
                        }
                    } else {
                        // Handle the case where the user is null or document does not exist
                        Log.e("Firestore Error", "User data not found")
                        if (activity is MainActivity) {
                            activity.hideProgressDialog()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure to retrieve data
                    Log.e("Firestore Error", "Error loading user data", e)
                    when (activity) {
                        is SignInActivity -> {
                            activity.hideProgressDialog()
                        }
                        is MainActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                }
        } else {
            Log.e("Firestore Error", "User ID is empty")
        }
    }


    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: ""
    }

    fun getAssignedMembersListDetails(
        activity:Activity,assignedTo:ArrayList<String>)
    {
        mFireStore.collection(Constants.Users)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                document->
                val userList:ArrayList<User> = ArrayList()
                for(i in document.documents) {
                    val user=i.toObject(User::class.java)
                    userList.add(user!!)
                }

                if (activity is MembersActivity) {
                    activity.setupMembersList(userList)
                }else if(activity is TaskListActivity) {
                    activity.boardMemberDetailList(userList)
                }
            }
            .addOnFailureListener {
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                }else if(activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
            }
    }

    fun getMemberDetails(activity:MembersActivity,email:String)
    {
        mFireStore.collection(Constants.Users)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                document->
                if(document.documents.size>0)
                {
                    val user=document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }
                else
                {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No Such Member Found")
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun assignedMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.assignedTo] = board.assignedTo

        mFireStore.collection(Constants.Boards)
            .document(board.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e("Firestore Error", "Error assigning member to board", it)
            }
    }



}
