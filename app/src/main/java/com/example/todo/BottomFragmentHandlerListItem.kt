package com.example.todo

import AppViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.nav_drawer_list_item.*
import kotlinx.android.synthetic.main.nav_drawer_main.*

class BottomFragmentHandlerListItem: BottomSheetDialogFragment() {
    val fdb = FirebaseDatabase.getInstance().reference
    private lateinit var viewModel: AppViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(activity!!).get(AppViewModel::class.java)
        return inflater.inflate(R.layout.nav_drawer_list_item, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationViewItem.setNavigationItemSelectedListener { menuItem ->
            when (menuItem!!.itemId) {
                R.id.editListItem -> {viewModel.editItemMode = true; viewModel.deleteItemMode = false; Toast.makeText(activity,"Edit Mode Enabled - Select a list item to edit.",
                    Toast.LENGTH_LONG).show(); dismiss()}
                R.id.deleteItem -> {viewModel.deleteItemMode = true; viewModel.editItemMode = false; Toast.makeText(activity,"Delete Mode Enabled - Select a list item to delete.",
                    Toast.LENGTH_LONG).show(); dismiss()}
                R.id.deleteList -> {fdb.child(viewModel.uID).child(viewModel.listTrueName).removeValue(); viewModel.editItemMode = false; viewModel.deleteItemMode = false; activity!!.finish();}
                R.id.backToLists -> activity!!.finish()
            }
            true
        }
    }
}