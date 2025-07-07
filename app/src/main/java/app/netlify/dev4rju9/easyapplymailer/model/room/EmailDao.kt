package app.netlify.dev4rju9.easyapplymailer.model.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {

    @Upsert
    suspend fun insertEmail(email: EmailEntity)

    @Query("SELECT * FROM emails")
    fun getAllEmails(): Flow<List<EmailEntity>>

    @Delete
    suspend fun deleteEmail(email: EmailEntity)

}
