package org.smartregister.fp.features.home.interactor

import org.apache.commons.lang3.StringUtils
import org.smartregister.DristhiConfiguration
import org.smartregister.domain.Response
import org.smartregister.fp.FPLibrary
import org.smartregister.fp.common.util.AppExecutors
import org.smartregister.fp.features.home.contract.AdvancedSearchContract
import org.smartregister.service.HTTPAgent
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class AdvancedSearchInteractor : AdvancedSearchContract.Interactor {

    companion object {
        const val SEARCH_URL = "/rest/search/search"
    }
    private val appExecutors: AppExecutors
    private var httpAgent: HTTPAgent? = null
    private var dristhiConfiguration: DristhiConfiguration? = null

    constructor() : this(AppExecutors())
    constructor(appExecutors: AppExecutors) {
        this.appExecutors = appExecutors
    }

    override fun search(editMap: Map<String, String>, callBack: AdvancedSearchContract.InteractorCallBack?, fpId: String) {
        val runnable = Runnable {
            val response: Response<String> = globalSearch(editMap)
            appExecutors.mainThread().execute { callBack!!.onResultsFound(response, fpId) }
        }

        appExecutors.networkIO().execute(runnable)
    }

    private fun globalSearch(map: Map<String, String>): Response<String> {
        val baseUrl: String = getDristhiConfiguration().dristhiBaseURL()
        var paramString = ""
        if (!map.isEmpty()) {
            for (entry in map.entries) {
                val key = entry.key
                var value = entry.value
                if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                    value = urlEncode(value)
                    val param = key.trim { it <= ' ' } + "=" + value.trim { it <= ' ' }
                    if (StringUtils.isBlank(paramString)) {
                        paramString = "?$param"
                    } else {
                        paramString += "&$param"
                    }
                }
            }
        }
        val uri = baseUrl + SEARCH_URL + paramString
        return getHttpAgent().fetch(uri)
    }

    fun getDristhiConfiguration(): DristhiConfiguration {
        if (dristhiConfiguration == null) {
            dristhiConfiguration = FPLibrary.getInstance().getContext().configuration()
        }
        return dristhiConfiguration!!
    }

    private fun urlEncode(value: String): String {
        return try {
            URLEncoder.encode(value, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            value
        }
    }

    fun getHttpAgent(): HTTPAgent {
        if (httpAgent == null) {
            httpAgent = FPLibrary.getInstance().getContext().getHttpAgent()
        }
        return httpAgent!!
    }

    fun setHttpAgent(httpAgent: HTTPAgent?) {
        this.httpAgent = httpAgent
    }

    fun setDristhiConfiguration(dristhiConfiguration: DristhiConfiguration?) {
        this.dristhiConfiguration = dristhiConfiguration
    }
}