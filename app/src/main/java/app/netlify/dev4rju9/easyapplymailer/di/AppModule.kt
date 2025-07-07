package app.netlify.dev4rju9.easyapplymailer.di

import android.content.Context
import androidx.room.Room
import app.netlify.dev4rju9.easyapplymailer.model.repository.Repository
import app.netlify.dev4rju9.easyapplymailer.model.room.AppDatabase
import app.netlify.dev4rju9.easyapplymailer.model.room.EmailDao
import app.netlify.dev4rju9.easyapplymailer.model.room.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).fallbackToDestructiveMigration(false).build()

    @Provides
    @Singleton
    fun providesUserDao (db: AppDatabase) = db.userDao()

    @Provides
    @Singleton
    fun providesEmailDao (db: AppDatabase) = db.emailDao()

    @Provides
    @Singleton
    fun providesRepository (userDao: UserDao, emailDao: EmailDao) = Repository(userDao, emailDao)

}