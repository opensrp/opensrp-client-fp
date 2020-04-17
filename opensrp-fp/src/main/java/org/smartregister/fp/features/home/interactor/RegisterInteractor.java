package org.smartregister.fp.features.home.interactor;

import android.content.ContentValues;
import android.util.Pair;

import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.UniqueId;
import org.smartregister.fp.common.event.PatientRemovedEvent;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.sync.BaseAncClientProcessorForJava;
import org.smartregister.fp.common.util.AppExecutors;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.FPJsonFormUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.contract.RegisterContract;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.Date;

import timber.log.Timber;

/**
 * Created by keyman 27/06/2018.
 */
public class RegisterInteractor implements RegisterContract.Interactor {
    private AppExecutors appExecutors;
    private UniqueIdRepository uniqueIdRepository;
    private ECSyncHelper syncHelper;
    private AllSharedPreferences allSharedPreferences;
    private ClientProcessorForJava clientProcessorForJava;
    private AllCommonsRepository allCommonsRepository;

    public RegisterInteractor() {
        this(new AppExecutors());
    }

    @VisibleForTesting
    RegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        //TODO set presenter or model to null
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple,
                                final RegisterContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            UniqueId uniqueId = getUniqueIdRepository().getNextUniqueId();
            final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
            appExecutors.mainThread().execute(() -> {
                if (StringUtils.isBlank(entityId)) {
                    callBack.onNoUniqueId();
                    PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG); //Non were found...lets trigger this againz
                } else {
                    callBack.onUniqueIdFetched(triple, entityId);
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final Pair<Client, Event> pair, final String jsonString, final boolean isEditMode,
                                 final RegisterContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            saveRegistration(pair, jsonString, isEditMode);
            String baseEntityId = getBaseEntityId(pair);
            appExecutors.mainThread().execute(() -> {
                callBack.setBaseEntityRegister(baseEntityId);
                callBack.onRegistrationSaved(isEditMode);
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void removeWomanFromANCRegister(final String closeFormJsonString, final String providerId) {
        Runnable runnable = () -> {
            try {
                Triple<Boolean, Event, Event> triple = FPJsonFormUtils
                        .saveRemovedFromANCRegister(getAllSharedPreferences(), closeFormJsonString, providerId);

                if (triple == null) {
                    return;
                }

                boolean isDeath = triple.getLeft();
                Event event = triple.getMiddle();
                Event updateChildDetailsEvent = triple.getRight();

                String baseEntityId = event.getBaseEntityId();

                //Update client to deceased
                JSONObject client = getSyncHelper().getClient(baseEntityId);
                if (isDeath) {
                    client.put(ConstantsUtils.JsonFormKeyUtils.DEATH_DATE, Utils.getTodaysDate());
                    client.put(ConstantsUtils.JsonFormKeyUtils.DEATH_DATE_APPROX, false);
                }
                JSONObject attributes = client.getJSONObject(ConstantsUtils.JsonFormKeyUtils.ATTRIBUTES);
                attributes.put(DBConstantsUtils.KeyUtils.DATE_REMOVED, Utils.getTodaysDate());
                client.put(ConstantsUtils.JsonFormKeyUtils.ATTRIBUTES, attributes);
                getSyncHelper().addClient(baseEntityId, client);

                //Add Remove Event for child to flag for Server delete
                JSONObject eventJson = new JSONObject(FPJsonFormUtils.gson.toJson(event));
                getSyncHelper().addEvent(event.getBaseEntityId(), eventJson);

                //Update Child Entity to include death date
                JSONObject eventJsonUpdateChildEvent =
                        new JSONObject(FPJsonFormUtils.gson.toJson(updateChildDetailsEvent));
                getSyncHelper().addEvent(baseEntityId, eventJsonUpdateChildEvent); //Add event to flag server update

                //Update REGISTER and FTS Tables
                if (getAllCommonsRepository() != null) {
                    ContentValues values = new ContentValues();
                    values.put(DBConstantsUtils.KeyUtils.DATE_REMOVED, Utils.getTodaysDate());
                    getAllCommonsRepository().update(DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME, values, baseEntityId);
                    getAllCommonsRepository().updateSearch(baseEntityId);

                }
            } catch (Exception e) {
                Timber.e(e, " --> removeWomanFromANCRegister");
            } finally {
                Utils.postStickyEvent(new PatientRemovedEvent());
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getRegistrationFromMetaData(String closeFormJsonString, String providerId) {

    }

    public AllCommonsRepository getAllCommonsRepository() {
        if (allCommonsRepository == null) {
            allCommonsRepository =
                    FPLibrary.getInstance().getContext().allCommonsRepositoryobjects(DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME);
        }
        return allCommonsRepository;
    }

    public void setAllCommonsRepository(AllCommonsRepository allCommonsRepository) {
        this.allCommonsRepository = allCommonsRepository;
    }

    private void saveRegistration(Pair<Client, Event> pair, String jsonString, boolean isEditMode) {
        try {
            Client baseClient = pair.first;
            Event baseEvent = pair.second;

            if (baseClient != null) {
                JSONObject clientJson = new JSONObject(FPJsonFormUtils.gson.toJson(baseClient));
                if (isEditMode) {
                    FPJsonFormUtils.mergeAndSaveClient(baseClient);
                } else {
                    getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
                }
            }

            if (baseEvent != null) {
                JSONObject eventJson = new JSONObject(FPJsonFormUtils.gson.toJson(baseEvent));
                getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
            }

            if (isEditMode) {
                // Unassign current OPENSRP ID
                if (baseClient != null) {
                    String newOpenSRPId = baseClient.getIdentifier(ConstantsUtils.ClientUtils.FP_ID).replace("-", "");
                    String currentOpenSRPId =
                            FPJsonFormUtils.getString(jsonString, ConstantsUtils.CURRENT_OPENSRP_ID).replace("-", "");
                    if (!newOpenSRPId.equals(currentOpenSRPId)) {
                        //OPENSRP ID was changed
                        // TODO: The new ID should be closed in the unique_ids repository
                        getUniqueIdRepository().open(currentOpenSRPId);
                    }
                }

            } else {
                if (baseClient != null) {
                    String opensrpId = baseClient.getIdentifier(ConstantsUtils.ClientUtils.FP_ID);

                    //mark OPENSRP ID as used
                    getUniqueIdRepository().close(opensrpId);
                }
            }

            if (baseClient != null || baseEvent != null) {
                String imageLocation = FPJsonFormUtils.getFieldValue(jsonString, ConstantsUtils.WOM_IMAGE);
                FPJsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
            }

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);


            // Todo: Use the event clients from above here
            getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.e(e, " --> saveRegistration");
        }
    }

    private String getBaseEntityId(Pair<Client, Event> clientEventPair) {
        String baseEntityId = "";
        if (clientEventPair != null) {
            Client client = clientEventPair.first;
            baseEntityId = client.getBaseEntityId();
        }

        return baseEntityId;
    }

    public ECSyncHelper getSyncHelper() {
        if (syncHelper == null) {
            syncHelper = FPLibrary.getInstance().getEcSyncHelper();
        }
        return syncHelper;
    }

    public void setSyncHelper(ECSyncHelper syncHelper) {
        this.syncHelper = syncHelper;
    }

    public AllSharedPreferences getAllSharedPreferences() {
        if (allSharedPreferences == null) {
            allSharedPreferences = FPLibrary.getInstance().getContext().allSharedPreferences();
        }
        return allSharedPreferences;
    }

    public void setAllSharedPreferences(AllSharedPreferences allSharedPreferences) {
        this.allSharedPreferences = allSharedPreferences;
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        if (clientProcessorForJava == null) {
            clientProcessorForJava = FPLibrary.getInstance().getClientProcessorForJava();
        }
        return clientProcessorForJava;
    }

    public void setClientProcessorForJava(BaseAncClientProcessorForJava clientProcessorForJava) {
        this.clientProcessorForJava = clientProcessorForJava;
    }

    public UniqueIdRepository getUniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = FPLibrary.getInstance().getUniqueIdRepository();
        }
        return uniqueIdRepository;
    }

    public void setUniqueIdRepository(UniqueIdRepository uniqueIdRepository) {
        this.uniqueIdRepository = uniqueIdRepository;
    }

    public enum TYPE {SAVED, UPDATED}
}
