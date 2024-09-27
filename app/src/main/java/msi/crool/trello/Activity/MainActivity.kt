package msi.crool.trello.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import msi.crool.trello.Adapters.BoardItemsAdapter
import msi.crool.trello.FireStore.FireStoreClass
import msi.crool.trello.Model.Board
import msi.crool.trello.Model.User
import msi.crool.trello.R
import msi.crool.trello.Util.Constants
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mUserName: String
    private lateinit var navView: NavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var drawer: DrawerLayout
    private lateinit var toolBar: Toolbar
    private lateinit var mSharedPrefrences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Toolbar
        toolBar = findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolBar)

        setupActionBar()

        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        drawer = findViewById(R.id.drawer_layout)
        mSharedPrefrences=this.getSharedPreferences(Constants.TRELLO_PREFRENCES,Context.MODE_PRIVATE)
        val tokenUpdated=mSharedPrefrences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)
        if(tokenUpdated) {
            showProgressDialog()
            FireStoreClass().loadUserData(this,true)
        }else {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener(this@MainActivity) {
                    updateFCMToken(it)
                }
        }
        FireStoreClass().loadUserData(this@MainActivity, true)

        fab = findViewById(R.id.fab_create_board)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.Name, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_item1 -> {
                startActivityForResult(
                    Intent(this@MainActivity, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }
            R.id.menu_item2 -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPrefrences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                MY_PROFILE_REQUEST_CODE -> FireStoreClass().loadUserData(this@MainActivity)
                CREATE_BOARD_REQUEST_CODE -> FireStoreClass().getBoardList(this@MainActivity)
            }
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Trello"

        toolBar.setNavigationIcon(R.drawable.r_menu)
        toolBar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }


    private fun toggleDrawer() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()

        mUserName = user.name.toString()

        val headerView = navView.getHeaderView(0)
        val navUserImage = headerView.findViewById<ImageView>(R.id.iv_user_image)

        Glide.with(this@MainActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.r_person)
            .into(navUserImage)

        val navUsername = headerView.findViewById<TextView>(R.id.tv_username)
        navUsername.text = user.name

        if (readBoardsList) {
            showProgressDialog()
            FireStoreClass().getBoardList(this@MainActivity)
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        val rvBoardsList = findViewById<RecyclerView>(R.id.rv_boards_list)
        val tvNoBoardsAvailable = findViewById<TextView>(R.id.tv_no_boards_available)
        val imageoops=findViewById<ImageView>(R.id.Oops_image)

        if (boardsList.isNotEmpty()) {
            rvBoardsList.visibility = View.VISIBLE
            tvNoBoardsAvailable.visibility = View.GONE
            imageoops.visibility=View.GONE


            rvBoardsList.layoutManager = LinearLayoutManager(this@MainActivity)
            rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this@MainActivity, boardsList)
            rvBoardsList.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.documentID, model.documentID)
                    startActivity(intent)
                }
            })
        } else {
            imageoops.visibility=View.VISIBLE
            rvBoardsList.visibility = View.GONE
            tvNoBoardsAvailable.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {

        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPrefrences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog()
        FireStoreClass().loadUserData(this@MainActivity, true)
    }
    private fun updateFCMToken(token:String)
    {
        val userHasMap= HashMap<String,Any>()
        userHasMap[Constants.FCM_TOKEN]=token
        showProgressDialog()
        FireStoreClass().updateUserProfileData(this,userHasMap)
    }

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }
}
