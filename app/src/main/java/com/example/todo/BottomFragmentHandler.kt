package com.example.todo


import AppViewModel
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.nav_drawer_main.*

class BottomFragmentHandler(): BottomSheetDialogFragment() {
    val fdb = FirebaseDatabase.getInstance().reference
    private lateinit var viewModel: AppViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(activity!!).get(AppViewModel::class.java)
        return inflater.inflate(R.layout.nav_drawer_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationViewMain.setNavigationItemSelectedListener { menuItem ->
            when (menuItem!!.itemId) {
                R.id.changeListName -> {viewModel.editMode = true; viewModel.deleteMode = false; Toast.makeText(activity,"Edit Mode Enabled - Select a list to change the name.",Toast.LENGTH_LONG).show(); dismiss()}
                R.id.deleteListMain -> {viewModel.deleteMode = true; viewModel.editMode = false; Toast.makeText(activity,"Delete Mode Enabled - Select a list to delete.",Toast.LENGTH_LONG).show(); dismiss()}
            }
            true
        }
    }
}
