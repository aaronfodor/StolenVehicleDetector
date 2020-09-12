package com.arpadfodor.stolenvehicledetector.android.app.model.repository

import com.arpadfodor.stolenvehicledetector.android.app.model.db.ApplicationDB
import com.arpadfodor.stolenvehicledetector.android.app.model.db.dataclasses.DbUser
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.dataclasses.User

object UserRepository {

    fun saveUser(user : User, callback: (Boolean) -> Unit){

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {

                val dbUser = userToDbUser(user)

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    database.userTable().deleteAll()
                    database.userTable().insert(dbUser)
                }
                //update the local flag
                isSuccess = true

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }

        }.start()

    }

    fun deleteUser(callback: (Boolean) -> Unit){

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var isSuccess = false

            try {
                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    database.userTable().deleteAll()
                }
                //update the local flag
                isSuccess = true
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(isSuccess)
            }

        }.start()

    }

    /**
     * User
     **/
    fun getUser(callback: (User?) -> Unit) {

        Thread {

            val database = ApplicationDB.getDatabase(GeneralRepository.context)
            var user: User? = null

            try {

                // run delete, insert, etc. in an atomic transaction
                database.runInTransaction {
                    val dbContent = database.userTable().getAll() ?: listOf()
                    val dbUser = dbContent.firstOrNull()
                    dbUser?.let {
                        user = dbUserToUser(it)
                    }
                }

            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                callback(user)
            }

        }.start()

    }

    private fun userToDbUser(source: User) : DbUser{
        return DbUser(source.email, source.name, source.password, source.hint, source.numReports)
    }

    private fun dbUserToUser(source: DbUser) : User{
        return User(source.email, source.name, source.password, source.hint, source.numReports)
    }

}