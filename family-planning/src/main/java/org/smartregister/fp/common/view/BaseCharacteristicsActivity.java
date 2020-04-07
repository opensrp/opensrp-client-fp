package org.smartregister.fp.common.view;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.domain.ServerSetting;
import org.smartregister.fp.R;
import org.smartregister.fp.common.adapter.CharacteristicsAdapter;
import org.smartregister.fp.common.contact.BaseCharacteristicsContract;
import org.smartregister.fp.common.contact.PopulationCharacteristicsContract;

import java.util.List;

/**
 * Created by ndegwamartin on 31/08/2018.
 */
public abstract class BaseCharacteristicsActivity extends BaseActivity
        implements CharacteristicsAdapter.ItemClickListener, PopulationCharacteristicsContract.View {

    protected Toolbar mToolbar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characteristics);

        recyclerView = findViewById(R.id.population_characteristics);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);


        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        BaseCharacteristicsContract.BasePresenter basePresenter = getPresenter();
        basePresenter.getCharacteristics();

        mToolbar = findViewById(R.id.register_toolbar);
        mToolbar.findViewById(R.id.close_characteristics).setOnClickListener(view -> onBackPressed());

        TextView titleTextView = mToolbar.findViewById(R.id.characteristics_toolbar_title);
        String title = getToolbarTitle();
        titleTextView.setText(title);

        mToolbar.findViewById(R.id.characteristics_toolbar_edit)
                .setVisibility(title.equals(getString(R.string.population_characteristics)) ? View.GONE : View.VISIBLE);

    }

    protected abstract BaseCharacteristicsContract.BasePresenter getPresenter();

    protected abstract String getToolbarTitle();

    @Override
    public void onItemClick(View view, int position) {
        renderSubInfoAlertDialog(view.findViewById(R.id.info).getTag(R.id.CHARACTERISTIC_DESC).toString());

    }

    protected void renderSubInfoAlertDialog(String info) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, R.style.FPAlertDialog);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Info").setMessage(info)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.dismiss()).setIcon(R.drawable.ic_info);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
    }

    @Override
    public void renderSettings(List<ServerSetting> characteristics) {

        CharacteristicsAdapter adapter = new CharacteristicsAdapter(this, characteristics);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }
}
