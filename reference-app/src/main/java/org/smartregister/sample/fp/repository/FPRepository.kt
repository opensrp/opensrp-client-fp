package org.smartregister.sample.fp.repository

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import org.smartregister.AllConstants
import org.smartregister.CoreLibrary
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository
import org.smartregister.repository.EventClientRepository
import org.smartregister.repository.Repository
import org.smartregister.repository.SettingsRepository
import org.smartregister.repository.UniqueIdRepository
import org.smartregister.sample.fp.BuildConfig
import org.smartregister.view.activity.DrishtiApplication
import timber.log.Timber

/**
 * Created by ndegwamartin on 09/04/2018.
 */
class FPRepository(context: Context?, openSRPContext: org.smartregister.Context) : Repository(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(),
        CoreLibrary.getInstance().context().commonFtsObject(), *openSRPContext.sharedRepositoriesArray()) {
    protected var mReadableDatabase: SQLiteDatabase? = null
    protected var mWritableDatabase: SQLiteDatabase? = null
    override fun onCreate(database: SQLiteDatabase) {
        super.onCreate(database)
        ConfigurableViewsRepository.createTable(database)
        EventClientRepository
                .createTable(database, EventClientRepository.Table.client, EventClientRepository.client_column.values())
        EventClientRepository
                .createTable(database, EventClientRepository.Table.event, EventClientRepository.event_column.values())
        UniqueIdRepository.createTable(database)
        SettingsRepository.onUpgrade(database)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Timber.tag(FPRepository::class.java.name).w("Upgrading database from version $oldVersion to $newVersion, which will destroy all old data")

//        AncLibrary.getInstance().performMigrations(db);
        var upgradeTo = oldVersion + 1
        while (upgradeTo <= newVersion) {
            when (upgradeTo) {
                2 -> {
                }
                else -> {
                }
            }
            upgradeTo++
        }
    }

    override fun getReadableDatabase(): SQLiteDatabase? {
        return getReadableDatabase(DrishtiApplication.getInstance().password)
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        return getWritableDatabase(DrishtiApplication.getInstance().password)
    }

    @Synchronized
    override fun getWritableDatabase(password: String): SQLiteDatabase {
        if (mWritableDatabase == null || !mWritableDatabase!!.isOpen) {
            if (mWritableDatabase != null) {
                mWritableDatabase!!.close()
            }
            mWritableDatabase = super.getWritableDatabase(password)
        }
        return mWritableDatabase!!
    }

    @Synchronized
    override fun getReadableDatabase(password: String): SQLiteDatabase? {
        return try {
            if (mReadableDatabase == null || !mReadableDatabase!!.isOpen) {
                if (mReadableDatabase != null) {
                    mReadableDatabase!!.close()
                }
                mReadableDatabase = super.getReadableDatabase(password)
            }
            mReadableDatabase!!
        } catch (e: Exception) {
            Timber.e(e, "Database Error")
            return null
        }
    }

    @Synchronized
    override fun close() {
        if (mReadableDatabase != null) {
            mReadableDatabase!!.close()
        }
        if (mWritableDatabase != null) {
            mWritableDatabase!!.close()
        }
        super.close()
    }
}