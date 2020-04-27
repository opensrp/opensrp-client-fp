package org.smartregister.sample.fp.repository;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.configurableviews.repository.ConfigurableViewsRepository;
import org.smartregister.fp.common.repository.PreviousContactRepository;
import org.smartregister.fp.features.home.repository.ContactTasksRepository;
import org.smartregister.fp.features.home.repository.PartialContactRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.repository.SettingsRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sample.fp.BuildConfig;
import org.smartregister.view.activity.DrishtiApplication;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 09/04/2018.
 */

public class FPRepository extends Repository {
    protected SQLiteDatabase mReadableDatabase;
    protected SQLiteDatabase mWritableDatabase;

    public FPRepository(Context context, org.smartregister.Context openSRPContext) {
        super(context, AllConstants.DATABASE_NAME, BuildConfig.DATABASE_VERSION, openSRPContext.session(),
                CoreLibrary.getInstance().context().commonFtsObject(), openSRPContext.sharedRepositoriesArray());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        ConfigurableViewsRepository.createTable(database);
        EventClientRepository
                .createTable(database, EventClientRepository.Table.client, EventClientRepository.client_column.values());
        EventClientRepository
                .createTable(database, EventClientRepository.Table.event, EventClientRepository.event_column.values());

        UniqueIdRepository.createTable(database);
        SettingsRepository.onUpgrade(database);
        PartialContactRepository.createTable(database);
        PreviousContactRepository.createTable(database);
        ContactTasksRepository.createTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.tag(FPRepository.class.getName()).w("Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    // upgradeToVersion2(db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return getReadableDatabase(DrishtiApplication.getInstance().getPassword());
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return getWritableDatabase(DrishtiApplication.getInstance().getPassword());
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        if (mWritableDatabase == null || !mWritableDatabase.isOpen()) {
            if (mWritableDatabase != null) {
                mWritableDatabase.close();
            }
            mWritableDatabase = super.getWritableDatabase(password);
        }
        return mWritableDatabase;
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        try {
            if (mReadableDatabase == null || !mReadableDatabase.isOpen()) {
                if (mReadableDatabase != null) {
                    mReadableDatabase.close();
                }
                mReadableDatabase = super.getReadableDatabase(password);
            }
            return mReadableDatabase;
        } catch (Exception e) {
            Timber.e(e, "Database Error");
            return null;
        }

    }

    @Override
    public synchronized void close() {
        if (mReadableDatabase != null) {
            mReadableDatabase.close();
        }

        if (mWritableDatabase != null) {
            mWritableDatabase.close();
        }
        super.close();
    }

}

