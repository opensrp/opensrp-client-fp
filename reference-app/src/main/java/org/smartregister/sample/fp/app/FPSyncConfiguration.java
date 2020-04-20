package org.smartregister.sample.fp.app;

import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sample.fp.BuildConfig;

import java.util.List;

/**
 * Created by samuelgithengi on 10/19/18.
 */
public class FPSyncConfiguration extends SyncConfiguration {
    @Override
    public int getSyncMaxRetries() {
        return BuildConfig.MAX_SYNC_RETRIES;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return SyncFilter.TEAM_ID;
    }

    @Override
    public String getSyncFilterValue() {
        AllSharedPreferences sharedPreferences =
                FPLibrary.getInstance().getContext().userService().getAllSharedPreferences();
        return sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM());
    }

    @Override
    public int getUniqueIdSource() {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    }

    @Override
    public boolean isSyncSettings() {
        return BuildConfig.IS_SYNC_SETTINGS;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return SyncFilter.TEAM;
    }

    @Override
    public boolean updateClientDetailsTable() {
        return true;
    }

    @Override
    public List<String> getSynchronizedLocationTags() {
        return null;
    }

    @Override
    public String getTopAllowedLocationLevel() {
        return null;
    }
}
