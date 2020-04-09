package org.smartregister.fp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.greenrobot.eventbus.meta.SubscriberInfoIndex;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.configurableviews.helper.JsonSpecHelper;
import org.smartregister.domain.Setting;
import org.smartregister.sync.ClientProcessorForJava;
import org.yaml.snakeyaml.Yaml;

import id.zelory.compressor.Compressor;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-02
 */

public class FPLibrary {
    private static FPLibrary instance;
    private final Context context;
    private JsonSpecHelper jsonSpecHelper;
/*    private PartialContactRepositoryHelper partialContactRepositoryHelper;
    private PreviousContactRepositoryHelper previousContactRepositoryHelper;
    private ContactTasksRepositoryHelper contactTasksRepositoryHelper;
    private EventClientRepository eventClientRepository;
    private UniqueIdRepository uniqueIdRepository;
    private DetailsRepository detailsRepository;

    private ECSyncHelper ecSyncHelper;
    private AncRulesEngineHelper ancRulesEngineHelper;*/

    private ClientProcessorForJava clientProcessorForJava;
    private JSONObject defaultContactFormGlobals = new JSONObject();

    private Compressor compressor;
    private Gson gson;

    private Yaml yaml;

    private SubscriberInfoIndex subscriberInfoIndex;

    private int databaseVersion;
//    private ActivityConfiguration activityConfiguration;

    private FPLibrary(@NonNull Context context, int dbVersion/*, @NonNull ActivityConfiguration activityConfiguration, @Nullable SubscriberInfoIndex subscriberInfoIndex*/) {
        this.context = context;
        this.subscriberInfoIndex = subscriberInfoIndex;
        this.databaseVersion = dbVersion;
//        this.activityConfiguration = activityConfiguration;

        //Initialize JsonSpec Helper
        this.jsonSpecHelper = new JsonSpecHelper(getApplicationContext());

    }

    public android.content.Context getApplicationContext() {
        return context.applicationContext();
    }


   /* public static void init(@NonNull Context context, int dbVersion) {
        init(context, dbVersion, new ActivityConfiguration());
    }

    public static void init(@NonNull Context context, int dbVersion, @NonNull ActivityConfiguration activityConfiguration) {
        init(context, dbVersion, activityConfiguration, null);
    }*/

    public static void init(@NonNull Context context, int dbVersion/*, @NonNull ActivityConfiguration activityConfiguration, @Nullable SubscriberInfoIndex subscriberInfoIndex*/) {
        if (instance == null) {
            instance = new FPLibrary(context, dbVersion/*, activityConfiguration, subscriberInfoIndex*/);
        }
    }

   /* public static void init(@NonNull Context context, int dbVersion, @Nullable SubscriberInfoIndex subscriberInfoIndex) {
        init(context, dbVersion*//*, new ActivityConfiguration()*//*, subscriberInfoIndex);
    }*/

    public static JsonSpecHelper getJsonSpecHelper() {
        return getInstance().jsonSpecHelper;
    }

    public static FPLibrary getInstance() {
        if (instance == null) {
            throw new IllegalStateException(" Instance does not exist!!! Call "
                    + FPLibrary.class.getName()
                    + ".init method in the onCreate method of "
                    + "your Application class ");
        }
        return instance;
    }

    /**
     * This method should be called in onUpgrade method of the Repository class where the migrations
     * are already managed instead of writing new code to manage them.
     */
    /*public static void performMigrations(@NonNull SQLiteDatabase database) {
        PatientRepositoryHelper.performMigrations(database);
    }

    public PartialContactRepositoryHelper getPartialContactRepositoryHelper() {
        if (partialContactRepositoryHelper == null) {
            partialContactRepositoryHelper = new PartialContactRepositoryHelper();
        }

        return partialContactRepositoryHelper;
    }

    public PreviousContactRepositoryHelper getPreviousContactRepositoryHelper() {
        if (previousContactRepositoryHelper == null) {
            previousContactRepositoryHelper = new PreviousContactRepositoryHelper();
        }

        return previousContactRepositoryHelper;
    }

    public ContactTasksRepositoryHelper getContactTasksRepositoryHelper() {
        if (contactTasksRepositoryHelper == null) {
            contactTasksRepositoryHelper = new ContactTasksRepositoryHelper();
        }

        return contactTasksRepositoryHelper;
    }

    public EventClientRepository getEventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository();
        }
        return eventClientRepository;
    }

    public UniqueIdRepository getUniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository();
        }

        return uniqueIdRepository;
    }

    public AncRulesEngineHelper getAncRulesEngineHelper() {
        if (ancRulesEngineHelper == null) {
            ancRulesEngineHelper = new AncRulesEngineHelper(getApplicationContext());
        }
        return ancRulesEngineHelper;
    }

    public ECSyncHelper getEcSyncHelper() {
        if (ecSyncHelper == null) {
            ecSyncHelper = ECSyncHelper.getInstance(getApplicationContext());
        }
        return ecSyncHelper;
    }

    public Compressor getCompressor() {
        if (compressor == null) {
            compressor = Compressor.getDefault(getApplicationContext());
        }
        return compressor;
    }*/
/*
    public ClientProcessorForJava getClientProcessorForJava() {
        if (clientProcessorForJava == null) {
            clientProcessorForJava = DrishtiApplication.getInstance().getClientProcessor();
        }

        return clientProcessorForJava;
    }

    public DetailsRepository getDetailsRepository() {
        if (detailsRepository == null) {
            detailsRepository = CoreLibrary.getInstance().context().detailsRepository();
        }

        return detailsRepository;
    }

    public Gson getGsonInstance() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public void populateGlobalSettings() {

        Setting setting = getCharacteristics(ConstantsUtils.PrefKeyUtils.SITE_CHARACTERISTICS);
        Setting populationSetting = getCharacteristics(ConstantsUtils.PrefKeyUtils.POPULATION_CHARACTERISTICS);

        populateGlobalSettingsCore(setting);
        populateGlobalSettingsCore(populationSetting);
    }*/

    public Setting getCharacteristics(String characteristics) {
        return FPLibrary.getInstance().getContext().allSettings().getSetting(characteristics);
    }

    /*private void populateGlobalSettingsCore(Setting setting) {
        try {
            JSONObject settingObject = setting != null ? new JSONObject(setting.getValue()) : null;
            if (settingObject != null) {
                JSONArray settingArray = settingObject.getJSONArray(AllConstants.SETTINGS);
                if (settingArray != null) {

                    for (int i = 0; i < settingArray.length(); i++) {

                        JSONObject jsonObject = settingArray.getJSONObject(i);
                        Boolean value = jsonObject.optBoolean(JsonFormConstants.VALUE);
                        JSONObject nullObject = null;
                        if (value != null && !value.equals(nullObject)) {
                            defaultContactFormGlobals.put(jsonObject.getString(JsonFormConstants.KEY), value);
                        } else {

                            defaultContactFormGlobals.put(jsonObject.getString(JsonFormConstants.KEY), false);
                        }
                    }


                }
            }
        } catch (JSONException e) {
            Timber.e(" --> populateGlobalSettingsCore");
        }
    }*/

    public Context getContext() {
        return context;
    }

/*    public JSONObject getDefaultContactFormGlobals() {
        return defaultContactFormGlobals;
    }

    *//*public Iterable<Object> readYaml(String filename) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(
                getApplicationContext().getAssets().open((FilePathUtils.FolderUtils.CONFIG_FOLDER_PATH + filename)));
        return yaml.loadAll(inputStreamReader);
    }*//*

    public int getDatabaseVersion() {
        return databaseVersion;
    }*/

}
