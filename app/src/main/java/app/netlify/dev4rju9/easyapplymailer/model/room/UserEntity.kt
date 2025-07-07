package app.netlify.dev4rju9.easyapplymailer.model.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val resumeFileName: String,
    val resumeUri: String
)