package app.netlify.dev4rju9.easyapplymailer.model.repository

import app.netlify.dev4rju9.easyapplymailer.model.room.EmailDao
import app.netlify.dev4rju9.easyapplymailer.model.room.EmailEntity
import app.netlify.dev4rju9.easyapplymailer.model.room.UserDao
import app.netlify.dev4rju9.easyapplymailer.model.room.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repository @Inject constructor(
    private val userDao: UserDao,
    private val emailDao: EmailDao
) {

    suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getUsers(): List<UserEntity> = userDao.getAllUsers()

    suspend fun saveEmail(email: EmailEntity) = emailDao.insertEmail(email)

    fun getEmails(): Flow<List<EmailEntity>> = emailDao.getAllEmails()

    suspend fun deleteEmail(email: EmailEntity) = emailDao.deleteEmail(email)

}