package com.example.todo

import AppViewModel
import TodoItemDataAdapter
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.list_contents_activity.*


class ListItemActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewModel: AppViewModel
    private val itemListTitle: TextView by lazy {findViewById<TextView>(R.id.listItemTitle)}
    private val addListButton: FloatingActionButton by lazy {findViewById<FloatingActionButton>(R.id.addListItem)}
    private val nav: NavigationView by lazy {findViewById<NavigationView>(R.id.landscapeNavigationViewListContents)}
    private val database = FirebaseDatabase.getInstance()
    var list: ArrayList<ListInteriorDataFormat>? = null
    var keyList: ArrayList<String?> = arrayListOf()
    private var listName: String? = ""
    private var listCurrentName: String? = ""
    private var uID: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.list_contents_activity)
        setSupportActionBar(listContentsAppBar)

        listName = intent.getStringExtra("LISTNAME")
        listCurrentName = intent.getStringExtra("LISTCURRENTNAME")
        itemListTitle.setText(listCurrentName)
        uID = intent.getStringExtra("UID")
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewContents)
        recyclerView.layoutManager = viewManager
        viewModel = ViewModelProviders.of(this).get(AppViewModel::class.java)
        myAdapter = TodoItemDataAdapter(this, arrayListOf(), viewModel, keyList, uID as String)
        recyclerView.adapter = myAdapter
        viewModel.listTrueName = listName!!
        viewModel.uID = uID as String

        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            nav.setNavigationItemSelectedListener(this);
        }

        var ref = database.reference.child(uID as String).child(listName as String)

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list = ArrayList<ListInteriorDataFormat>()
                for (data in dataSnapshot.children) {
                    if(data.key == "listTrueName" || data.key == "listCurrentName"){
                        continue
                    }
                    var alarm = false
                    var notification = false
                    var timedate = ""
                    var todo = ""
                    for(data2 in data.children) {
                        when(data2.key){
                            "alarm" -> alarm = data2.getValue() as Boolean
                            "notification" -> notification = data2.getValue() as Boolean
                            "timedate" -> timedate = data2.getValue() as String
                            "todo" -> todo = data2.getValue() as String
                        }
                    }
                    keyList!!.add(data.key)
                    list!!.add(ListInteriorDataFormat(alarm,notification,timedate,todo))
                }
                myAdapter = TodoItemDataAdapter(this@ListItemActivity, list!!, viewModel, keyList, uID as String)
                recyclerView.adapter = myAdapter
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        ref.addValueEventListener(listener)

        addListButton.setOnClickListener(){
            val intent = Intent(application, AddEditActivity::class.java).putExtra("CURRENTLIST", listName).putExtra("UID", uID)
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(navItem: MenuItem): Boolean {
        when(navItem!!.itemId) {
            R.id.editListItem -> {
                viewModel.editItemMode = true; viewModel.deleteItemMode = false; Toast.makeText(
                    this, "Edit Mode Enabled - Select a list item to edit.",
                    Toast.LENGTH_LONG
                ).show()
            }
            R.id.deleteItem -> {
                viewModel.deleteItemMode = true; viewModel.editItemMode = false; Toast.makeText(
                    this, "Delete Mode Enabled - Select a list item to delete.",
                    Toast.LENGTH_LONG
                ).show()
            }
            R.id.deleteList -> {
                database.reference.child(viewModel.uID).child(viewModel.listTrueName)
                    .removeValue(); viewModel.editItemMode = false; viewModel.deleteItemMode =
                    false; this@ListItemActivity.finish();
            }
            R.id.backToLists -> this@ListItemActivity.finish()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bottom_app_bar_list_contents, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val bottomNavDrawerFragment = BottomFragmentHandlerListItem()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
            when (item!!.itemId) {
                R.id.exit -> endActivity()
            }
        }
        return true
    }

    fun endActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("EXIT", true)
        startActivity(intent)
    }


}