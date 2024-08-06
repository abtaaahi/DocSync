
# DocSync - Document Syncing

DocSync is a comprehensive document syncing application designed to enhance document management and collaboration.

The app features a seamless integration of Firebase for authentication and real-time database storage, ensuring a robust and interactive user experience.

App Preview: https://youtu.be/5tqxx5ZuGpw?si=qMH9OFpHBhXJo-m4

Firebase: https://console.firebase.google.com/u/0/

Authentication: https://firebase.google.com/docs/auth

Database: https://firebase.google.com/docs/database
## API Reference

#### Dependencies 

```
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation("com.google.firebase:firebase-database:20.0.4")
    implementation("com.google.code.gson:gson:2.8.8")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
```

#### Fetch document

```
private fun fetchDocuments() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                documents.clear()
                for (documentSnapshot in snapshot.children) {
                    val document = documentSnapshot.getValue(Document::class.java)
                    document?.let { documents.add(0, it) }
                }
                documentAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
```
#### GSON to JSON
```
    val gson = Gson()
    val documentJson = gson.toJson(document)

```
#### Plugins

```
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

```


## Features

- User Authentication: Utilizes Firebase Authentication for secure signup and login.
- Document Management: Displays all documents in a RecyclerView, leveraging Firebase Realtime Database for data storage and synchronization.
- Offline Support: Implements SharedPreferences and Room Database to ensure access to data even without an internet connection.
- Version Control: Shows past editors and commits changes in real-time, enhancing collaboration and tracking.
- Search Functionality: Allows users to search for documents easily.
- Local Storage: Supports file import from and export to local storage, as well as file sharing capabilities.


## Tech Stack

**Development Environment:** Android Studio

**Programming Language:** Kotlin

**Firebase:** Authentication, Realtime Database

**SharedPreferences:** Offline data storage

**Room Database:** Offline data storage

**RecyclerView:** Displaying documents

**User Interface Design:** Material Design Principles

**Testing:** JUnit, Espresso

