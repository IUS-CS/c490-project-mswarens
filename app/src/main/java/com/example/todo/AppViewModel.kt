import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppViewModel : ViewModel() {
    var editMode = false
    var deleteMode = false
    var editItemMode = false
    var deleteItemMode = false
    var listTrueName = ""
    var uID = ""
    var loggedIn = false
}