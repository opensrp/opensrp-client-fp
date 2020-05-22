package org.smartregister.fp.common.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.FPJsonFormUtils;

import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FPJsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra("json");
                Timber.d("JSONResult %s", jsonString);
            } catch (Exception e) {
                Timber.e(e);
            }

        }
    }

    public void goToHomeRegisterPage() {
        Intent intent = new Intent(this, FPLibrary.getInstance().getActivityConfiguration().getLandingPageActivityClass())
                .putExtra(ConstantsUtils.IntentKeyUtils.IS_REMOTE_LOGIN,
                        getIntent().getBooleanExtra(ConstantsUtils.IntentKeyUtils.IS_REMOTE_LOGIN, false));
        startActivity(intent);
        finish();
    }

}
