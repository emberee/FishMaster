package com.example.myapplication.data

class FishRepository(private val fishDao: FishDao) {

    val allSpecies = fishDao.getAllSpecies()
    val allRecords = fishDao.getAllRecords()
    val totalCount = fishDao.getTotalCount()
    val speciesCounts = fishDao.getSpeciesCounts()
    val speciesWeightSum = fishDao.getSpeciesWeightSum()
    val topHeavyRecords = fishDao.getTopHeavyRecords()

    suspend fun addRecord(record: FishRecord): Long = fishDao.insertRecord(record)
    suspend fun updateRecord(record: FishRecord) = fishDao.updateRecord(record)
    suspend fun deleteRecord(record: FishRecord) = fishDao.deleteRecord(record)
    suspend fun getRecordById(id: Long): FishRecord? = fishDao.getRecordById(id)
    suspend fun getSpeciesById(id: Long): FishSpecies? = fishDao.getSpeciesById(id)
    fun getRecordsBySpecies(speciesId: Long) = fishDao.getRecordsBySpecies(speciesId)
    fun getCountSince(since: Long) = fishDao.getCountSince(since)
    fun getSpeciesCountsSince(since: Long) = fishDao.getSpeciesCountsSince(since)
    suspend fun getLatestRecord(): FishRecord? = fishDao.getLatestRecord()
    fun getLatestRecordFlow() = fishDao.getLatestRecordFlow()
}
