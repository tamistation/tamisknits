import com.example.tamisknits.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public class repo(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    fun getUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .addSnapshotListener { document, error ->
                        if (error != null) {
                            trySend(null);
                            return@addSnapshotListener
                        }
                        if (document != null
                            && document.exists()
                        ) {
                            val user = User(
                                uid = document.id,
                                name = document.getString("name") ?: "",
                                email = document.getString("email") ?: "",
                                phone = document.getString("phone") ?: "",

                                )
                            trySend(user)
                        } else {
                            trySend(null)
                        }
                    }
            }
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }
}