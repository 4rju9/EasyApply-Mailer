package app.netlify.dev4rju9.easyapplymailer.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val body: String
)