import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.AddEditActivity
import com.example.todo.ListInteriorDataFormat
import com.example.todo.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class TodoItemDataAdapter(private val c: Context, private val myDataset: ArrayList<ListInteriorDataFormat>, private val viewModel: AppViewModel, private val keyArrayList: ArrayList<String?>, private val uID: String) :
    RecyclerView.Adapter<TodoItemDataAdapter.MyViewHolder>() {
    class MyViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    private val database = FirebaseDatabase.getInstance()
    private val currentListDatabaseRef = database.reference.child(uID).child(viewModel.listTrueName)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemDataAdapter.MyViewHolder {
        val linear = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false) as LinearLayout
        return MyViewHolder(linear)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            itemView.setOnClickListener(){
                if(viewModel.editItemMode){
                    val intent = Intent(c, AddEditActivity::class.java).putExtra("CURRENTLIST", viewModel.listTrueName)
                        .putExtra("TIMEDATEDATA", myDataset[position].timedate)
                        .putExtra("TODODATA", myDataset[position].todo)
                        .putExtra("EDITITEMMODE", true)
                        .putExtra("UID", uID)
                        .putExtra("ITEMID", keyArrayList[position].toString())
                    c.startActivity(intent)
                    viewModel.editItemMode = false
                }else if (viewModel.deleteItemMode){
                    currentListDatabaseRef.child((keyArrayList[position]).toString()).removeValue()
                    viewModel.deleteItemMode = false
                }
            }
            itemView.findViewById<TextView>(R.id.todoItemName).text = myDataset[position].todo
            itemView.findViewById<TextView>(R.id.timeDate).text = "When: " + myDataset[position].timedate
        }
    }
    override fun getItemCount() = myDataset.size


}
