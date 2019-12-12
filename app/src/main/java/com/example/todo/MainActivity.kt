package com.example.todo

import AppViewModel
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.internal.NavigationMenu
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_drawer_main.*
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewModel: AppViewModel
    private val addListButton: FloatingActionButton by lazy { findViewById<FloatingActionButton>(R.id.addList) }
    private val database = FirebaseDatabase.getInstance()
    private val nav: NavigationView by lazy {findViewById<NavigationView>(R.id.landscapeNavigationViewMain)}
    private val RC_SIGN_IN = 999
    private lateinit var myAdapter: FirebaseRecyclerAdapter<*, *>
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(AppViewModel::class.java)
        setSupportActionBar(listsAppBar)
        viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMain)
        if(!viewModel.loggedIn) {
            startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setIsSmartLockEnabled(
                    false
                ).setAvailableProviders(providers).build(), RC_SIGN_IN
            )
        }else{
            mainFunctions()
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            nav.setNavigationItemSelectedListener(this);
        }
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }




    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.bottom_app_bar, menu)
        return true
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var text = itemView.findViewById<TextView>(R.id.list)
        fun setTxtTitle(string: String?) {
            text.text = string
        }
    }

    override fun onNavigationItemSelected(navItem: MenuItem): Boolean {
        when(navItem!!.itemId){
            R.id.changeListName -> {viewModel.editMode = true; viewModel.deleteMode = false; Toast.makeText(this,"Edit Mode Enabled - Select a list to edit.",
                Toast.LENGTH_LONG).show()}
            R.id.deleteListMain -> {viewModel.deleteMode = true; viewModel.editMode = false; Toast.makeText(this,"Delete Mode Enabled - Select a list to delete.",
                Toast.LENGTH_LONG).show()}}
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                mainFunctions()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                finish()
            }
        }
    }

    private fun mainFunctions(){

        var currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        val query: Query = FirebaseDatabase.getInstance().reference.child(currentUserID.toString())
        val options: FirebaseRecyclerOptions<ListDataFormat> = FirebaseRecyclerOptions.Builder<ListDataFormat>().setQuery(query, ListDataFormat::class.java).build()

        addListButton.setOnClickListener() {
            viewModel.editMode = false
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("List Name")
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            var holder = ""
            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    holder = input.text.toString(); database.reference.child(currentUserID.toString()).child(holder)
                    .child("listTrueName").setValue(holder);
                    database.reference.child(currentUserID.toString()).child(holder)
                        .child("listCurrentName").setValue(holder);
                })
            builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()

        }

        myAdapter = object : FirebaseRecyclerAdapter<ListDataFormat, ViewHolder?>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_layout, parent, false)
                return ViewHolder(view)
            }
            protected override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ListDataFormat) {
                holder.setTxtTitle(model.listCurrentName)
                holder.text.setOnClickListener() {
                    if(!viewModel.editMode && !viewModel.deleteMode) {
                        val intent = Intent(application, ListItemActivity::class.java).putExtra("LISTNAME", model.listTrueName)
                            .putExtra("LISTCURRENTNAME", model.listCurrentName)
                            .putExtra("UID", currentUserID)
                        startActivity(intent)
                    }else if(!viewModel.deleteMode){
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle("Change List Name")
                        val input = EditText(this@MainActivity)
                        input.inputType = InputType.TYPE_CLASS_TEXT
                        builder.setView(input)
                        var holder = ""
                        builder.setPositiveButton("OK",
                            DialogInterface.OnClickListener { dialog, which ->
                                holder = input.text.toString(); var v = database.reference.child(currentUserID.toString()).child(model.listTrueName).child("listCurrentName").setValue(holder).addOnCompleteListener(){
                                viewModel.editMode = false
                            }
                            })
                        builder.setNegativeButton("Cancel",
                            DialogInterface.OnClickListener { dialog, which -> viewModel.editMode = false; dialog.cancel() })
                        builder.show()
                    }else{
                        database.reference.child(currentUserID.toString()).child(model.listTrueName).removeValue()
                        viewModel.deleteMode = false
                    }
                }
            }
        }
        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = viewManager
        viewModel.loggedIn = true
        myAdapter.startListening()
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val bottomNavDrawerFragment = BottomFragmentHandler()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
            when (item!!.itemId) {
                R.id.exit -> finish()
            }
        }
        return true

    }


}
