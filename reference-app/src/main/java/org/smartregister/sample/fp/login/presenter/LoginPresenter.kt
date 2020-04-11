package org.smartregister.sample.fp.login.presenter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.toolbox.ImageLoader
import org.smartregister.configurableviews.model.LoginConfiguration
import org.smartregister.configurableviews.model.ViewConfiguration
import org.smartregister.fp.common.library.FPLibrary
import org.smartregister.fp.common.util.ConstantsUtils
import org.smartregister.fp.common.util.ImageLoaderRequestUtils
import org.smartregister.login.model.BaseLoginModel
import org.smartregister.login.presenter.BaseLoginPresenter
import org.smartregister.sample.fp.R
import org.smartregister.sample.fp.login.interactor.LoginInteractor
import org.smartregister.view.contract.BaseLoginContract
import timber.log.Timber
import java.lang.ref.WeakReference

class LoginPresenter(loginView: BaseLoginContract.View) : BaseLoginPresenter(), BaseLoginContract.Presenter {

    init {
        mLoginView = WeakReference(loginView)
        mLoginInteractor = LoginInteractor(this)
        mLoginModel = BaseLoginModel()
    }

    override fun processViewCustomizations() {
        try {
            val jsonString = getJsonViewFromPreference(ConstantsUtils.VIEW_CONFIGURATION_PREFIX + ConstantsUtils.ConfigurationUtils.LOGIN)
                    ?: return
            val loginView: ViewConfiguration = FPLibrary.getJsonSpecHelper().getConfigurableView(jsonString)
            val metadata: LoginConfiguration = loginView.metadata as LoginConfiguration
            val background: LoginConfiguration.Background = metadata.background
            val showPasswordCheckBox = getLoginView().activityContext.findViewById<CheckBox>(R.id.login_show_password_checkbox)
            val showPasswordTextView = getLoginView().activityContext.findViewById<TextView>(R.id.login_show_password_text_view)
            if (!metadata.showPasswordCheckbox) {
                showPasswordCheckBox.visibility = View.GONE
                showPasswordTextView.visibility = View.GONE
            } else {
                showPasswordCheckBox.visibility = View.VISIBLE
                showPasswordTextView.visibility = View.VISIBLE
            }
            if (background.orientation != null && background.startColor != null && background.endColor != null) {
                val loginLayout = getLoginView().activityContext.findViewById<View>(R.id.login_layout)
                val gradientDrawable = GradientDrawable()
                gradientDrawable.shape = GradientDrawable.RECTANGLE
                gradientDrawable.orientation = GradientDrawable.Orientation.valueOf(background.orientation)
                gradientDrawable.colors = intArrayOf(Color.parseColor(background.startColor), Color.parseColor(background.endColor))
                loginLayout.background = gradientDrawable
            }
            if (metadata.logoUrl != null) {
                val imageView = getLoginView().activityContext.findViewById<ImageView>(R.id.login_logo)
                ImageLoaderRequestUtils.getInstance(getLoginView().activityContext)?.imageLoader
                        ?.get(metadata.logoUrl,
                                ImageLoader.getImageListener(imageView, R.drawable.ic_who_logo, R.drawable.ic_who_logo))
                        ?.bitmap
            }
        } catch (e: Exception) {
            Timber.d(e.message.toString())
        }
    }

    override fun isServerSettingsSet(): Boolean {
        return false
    }
}