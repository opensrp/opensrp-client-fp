package org.smartregister.sample.fp.base;

import android.os.Build;

import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.smartregister.sample.fp.application.TestFPApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by ndegwamartin on 27/03/2018.
 */

@RunWith(RobolectricTestRunner.class)
@Config(application = TestFPApplication.class,sdk = Build.VERSION_CODES.P)
public abstract class BaseUnitTest {

    protected static final String DUMMY_USERNAME = "myusername";
    protected static final String DUMMY_PASSWORD = "mypassword";

    protected JSONObject getMainJsonObject(String filePath) throws Exception {
        InputStream inputStream = RuntimeEnvironment.application.getAssets()
                .open(filePath + ".json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                StandardCharsets.UTF_8));
        String jsonString;
        StringBuilder stringBuilder = new StringBuilder();
        while ((jsonString = reader.readLine()) != null) {
            stringBuilder.append(jsonString);
        }
        inputStream.close();
        return new JSONObject(stringBuilder.toString());
    }
}
