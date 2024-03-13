package com.cs4530.lifestyleapp

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserTable) : Long

    @Query("SELECT * FROM user_table")
    suspend fun getAll(): List<UserTable>

    @Query("SELECT * FROM user_table WHERE id = :userId")
    suspend fun loadById(userId: Long): UserTable

}
