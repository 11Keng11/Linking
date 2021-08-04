package com.example.linking_application_android

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class FirebaseUtils {
    val fireStoreDatabase = FirebaseFirestore.getInstance()
}

fun readFBData() {
    val hashMap = hashMapOf<String, Any>(
        "name" to "John doe",
        "city" to "Nairobi",
        "age" to 24 )

    val db = FirebaseUtils().fireStoreDatabase
    val docRef = db.collection("route").document("testroute")
    docRef.get()
        .addOnSuccessListener { document ->
            if (document != null) {
                val data = document.data
                Log.e("FIREBASE", "DocumentSnapshot data: ${data!!::class.simpleName}")
            } else {
                Log.e("FIREBASE", "No such document")
            }
        }
        .addOnFailureListener { exception ->
            Log.d("FIREBASE", "get failed with ", exception)
        }
}