package com.example.myapplication.room

import androidx.room.*
import com.example.myapplication.Info

@Dao
interface RoastInfoDao {
    @Query("SELECT * FROM roast_info LIMIT 5")
    fun getRecent(): List<RoastInfo>


    @Insert
    fun insert(roastInfo: RoastInfo)

    @Query("SELECT * FROM roast_info WHERE timeId = :timeId")
    fun get(timeId: Long): RoastInfo?

    @Delete
    fun delete(roastInfo: RoastInfo)
}

@Entity(tableName = "roast_info")
data class RoastInfo(
    @PrimaryKey val timeId: Long,
    @ColumnInfo(name = "bean_name") val beanName: String,
    @ColumnInfo(name = "bean_weight") val beanWeight: String,
    @ColumnInfo(name = "roasted_weight") val roastedWeight: String,
    @ColumnInfo(name = "history_json_string") val jsonString: String
)
