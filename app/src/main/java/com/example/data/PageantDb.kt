package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "contestants")
data class Contestant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val candidateNumber: String,
    val age: Int,
    val region: String,
    val platform: String,
    val biography: String,
    val votes: Int = 0,
    val imageResName: String // "img_contestant_1" etc.
)

@Entity(tableName = "transactions")
data class VoteTransaction(
    @PrimaryKey val transactionId: String, // TXN-XXXXXX
    val contestantId: Int,
    val contestantName: String,
    val votesCount: Int,
    val amount: Double,
    val currency: String = "USD",
    val phoneNumber: String,
    val provider: String, // "MTN MoMo", "M-Pesa", "Airtel Money", "Orange Money"
    val status: String, // "Pending", "Completed", "Failed"
    val timestamp: Long = System.currentTimeMillis()
)

// 2. DAO (Data Access Object)
@Dao
interface PageantDao {
    @Query("SELECT * FROM contestants ORDER BY votes DESC")
    fun getAllContestantsByVotes(): Flow<List<Contestant>>

    @Query("SELECT * FROM contestants WHERE id = :id")
    suspend fun getContestantById(id: Int): Contestant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContestants(contestants: List<Contestant>)

    @Query("UPDATE contestants SET votes = votes + :votesCount WHERE id = :id")
    suspend fun incrementVotes(id: Int, votesCount: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: VoteTransaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<VoteTransaction>>

    @Query("UPDATE transactions SET status = :status WHERE transactionId = :txnId")
    suspend fun updateTransactionStatus(txnId: String, status: String)

    @Query("SELECT COUNT(*) FROM contestants")
    suspend fun getContestantsCount(): Int
}

// 3. Database
@Database(entities = [Contestant::class, VoteTransaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pageantDao(): PageantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pageant_vote_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Repository
class PageantRepository(private val dao: PageantDao) {
    val contestants: Flow<List<Contestant>> = dao.getAllContestantsByVotes()
    val transactions: Flow<List<VoteTransaction>> = dao.getAllTransactions()

    suspend fun getContestantById(id: Int): Contestant? = dao.getContestantById(id)

    suspend fun addContestants(list: List<Contestant>) {
        dao.insertContestants(list)
    }

    suspend fun updateContestant(contestant: Contestant) {
        dao.insertContestants(listOf(contestant))
    }

    suspend fun addTransaction(txn: VoteTransaction) {
        dao.insertTransaction(txn)
    }

    suspend fun completeVote(txnId: String, contestantId: Int, votesCount: Int) {
        dao.updateTransactionStatus(txnId, "Completed")
        dao.incrementVotes(contestantId, votesCount)
    }

    suspend fun failTransaction(txnId: String) {
        dao.updateTransactionStatus(txnId, "Failed")
    }

    suspend fun hasContestants(): Boolean {
        return dao.getContestantsCount() > 0
    }
}
