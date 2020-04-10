package org.smartregister.fp.common.util

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.Cache
import com.android.volley.Network
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.ImageLoader

/**
 * Created by samuelgithengi on 1/19/18.
 */
class ImageLoaderRequestUtils private constructor(private val context: Context) {
    val imageLoader: ImageLoader
    private var requestQueue: RequestQueue?
    private fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            val cache: Cache = DiskBasedCache(context.cacheDir, 10 * 1024 * 1024)
            val network: Network = BasicNetwork(HurlStack())
            requestQueue = RequestQueue(cache, network)
            requestQueue!!.start()
        }
        return requestQueue!!
    }

    companion object {
        private var imageLoaderRequestUtils: ImageLoaderRequestUtils? = null

        @Synchronized
        fun getInstance(context: Context): ImageLoaderRequestUtils? {
            if (imageLoaderRequestUtils == null) {
                imageLoaderRequestUtils = ImageLoaderRequestUtils(context)
            }
            return imageLoaderRequestUtils
        }
    }

    init {
        requestQueue = getRequestQueue()
        imageLoader = ImageLoader(requestQueue, object : ImageLoader.ImageCache {
            private val cache = LruCache<String, Bitmap>(20)
            override fun getBitmap(url: String): Bitmap {
                return cache[url]
            }

            override fun putBitmap(url: String, bitmap: Bitmap) {
                cache.put(url, bitmap)
            }
        })
    }
}