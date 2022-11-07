package com.awais.storemanagementsystem.roomdb

import androidx.annotation.NonNull
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.awais.storemanagementsystem.roomdb.deo.BrandDao
import com.awais.storemanagementsystem.roomdb.deo.CustomerDao
import com.awais.storemanagementsystem.roomdb.deo.ProductsDao
import com.awais.storemanagementsystem.roomdb.deo.RackDao
import com.awais.storemanagementsystem.roomdb.entity.BrandEntity
import com.awais.storemanagementsystem.roomdb.entity.CustomerEntity
import com.awais.storemanagementsystem.roomdb.entity.ProductEntity
import com.awais.storemanagementsystem.roomdb.entity.RacksEntity
import com.awais.storemanagementsystem.util.App
import java.util.concurrent.Executors

@Database(
    entities = [
        ProductEntity::class, BrandEntity::class, RacksEntity::class, CustomerEntity::class
    ], version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val NUMBER_OF_THREADS = 8
        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)!!

        fun get(): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room
                            .databaseBuilder(
                                App.context,
                                AppDatabase::class.java,
                                "StoreAppDatabase"
                            )
                            .addCallback(sRoomDatabaseCallback)
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private val sRoomDatabaseCallback: Callback = object : Callback() {
            override fun onCreate(@NonNull db: SupportSQLiteDatabase) {
                super.onCreate(db)
                databaseWriteExecutor.execute {
                    INSTANCE?.productsDao()?.deleteAll()
                    INSTANCE?.brandsDao()?.deleteAll()
                    INSTANCE?.racksDao()?.deleteAll()
                    INSTANCE?.customerDao()?.deleteAll()
                }
            }
        }
    }

    abstract fun productsDao(): ProductsDao
    abstract fun brandsDao(): BrandDao
    abstract fun racksDao(): RackDao
    abstract fun customerDao(): CustomerDao


}