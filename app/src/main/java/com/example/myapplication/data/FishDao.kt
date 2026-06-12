package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {

    // ================== 鱼种操作 ==================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecies(species: FishSpecies): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeciesList(species: List<FishSpecies>)

    @Query("SELECT * FROM fish_species ORDER BY id ASC")
    fun getAllSpecies(): Flow<List<FishSpecies>>

    @Query("SELECT * FROM fish_species WHERE id = :id")
    suspend fun getSpeciesById(id: Long): FishSpecies?

    // ================== 鱼获记录操作 ==================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FishRecord): Long

    @Update
    suspend fun updateRecord(record: FishRecord)

    @Delete
    suspend fun deleteRecord(record: FishRecord)

    @Query("SELECT * FROM fish_records ORDER BY catchTime DESC")
    fun getAllRecords(): Flow<List<FishRecord>>

    @Query("SELECT * FROM fish_records WHERE id = :id")
    suspend fun getRecordById(id: Long): FishRecord?

    @Query("SELECT * FROM fish_records WHERE speciesId = :speciesId ORDER BY catchTime DESC")
    fun getRecordsBySpecies(speciesId: Long): Flow<List<FishRecord>>

    // ================== 统计查询 ==================

    @Query("SELECT COUNT(*) FROM fish_records")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT speciesId, COUNT(*) as count FROM fish_records GROUP BY speciesId ORDER BY count DESC")
    fun getSpeciesCounts(): Flow<List<SpeciesCount>>

    @Query("SELECT speciesId, SUM(weight) as totalWeight FROM fish_records WHERE weight IS NOT NULL GROUP BY speciesId ORDER BY totalWeight DESC")
    fun getSpeciesWeightSum(): Flow<List<SpeciesWeightSum>>

    @Query("SELECT * FROM fish_records WHERE weight IS NOT NULL ORDER BY weight DESC LIMIT 10")
    fun getTopHeavyRecords(): Flow<List<FishRecord>>

    @Query("SELECT COUNT(*) FROM fish_records WHERE catchTime >= :since")
    fun getCountSince(since: Long): Flow<Int>

    @Query("SELECT speciesId, COUNT(*) as count FROM fish_records WHERE catchTime >= :since GROUP BY speciesId ORDER BY count DESC")
    fun getSpeciesCountsSince(since: Long): Flow<List<SpeciesCount>>

    @Query("SELECT * FROM fish_records ORDER BY catchTime DESC LIMIT 1")
    suspend fun getLatestRecord(): FishRecord?

    @Query("SELECT * FROM fish_records ORDER BY catchTime DESC LIMIT 1")
    fun getLatestRecordFlow(): Flow<FishRecord?>
}

data class SpeciesCount(
    val speciesId: Long,
    val count: Int
)

data class SpeciesWeightSum(
    val speciesId: Long,
    val totalWeight: Double
)
