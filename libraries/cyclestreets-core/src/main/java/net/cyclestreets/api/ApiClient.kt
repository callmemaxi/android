package net.cyclestreets.api

import android.content.Context
import android.content.pm.PackageManager

import net.cyclestreets.api.client.RetrofitApiClient
import net.cyclestreets.core.R

interface CycleStreetsApi {
    fun getJourneyJson(plan: String, leaving: String?, arriving: String?, speed: Int, lonLat: DoubleArray): String
    fun getJourneyJson(plan: String, itineraryId: Long): String
    fun getPhotomapCategories(): PhotomapCategories
    fun getPhotos(e: Double, w: Double, n: Double, s: Double): Photos
    fun getUserJourneys(username: String): UserJourneys
    fun geoCoder(search: String, n: Double, s: Double, e: Double, w: Double): GeoPlaces
    fun sendFeedback(itinerary: Int, comments: String, name: String, email: String): Result
    fun uploadPhoto(filename: String, username: String, password: String, lon: Double, lat: Double, metaCat: String, category: String, dateTime: String, caption: String): Upload.Result
    fun signin(username: String, password: String): Signin.Result
    fun register(username: String, password: String, name: String, email: String): Result
    fun getPOICategories(iconSize: Int): POICategories
    fun getPOIs(key: String, lonE: Double, lonW: Double, latN: Double, latS: Double): List<POI>
    fun getPOIs(key: String, lon: Double, lat: Double, radius: Int): List<POI>
    fun getBlogEntries(): Blog
}

object ApiClient : CycleStreetsApi {
    private const val API_HOST = "https://www.cyclestreets.net"
    private const val API_HOST_V2 = "https://api.cyclestreets.net"

    private lateinit var delegate: CycleStreetsApi
    private lateinit var messages: Map<Int, String>
    private var customiser: ApiCustomiser? = null

    fun initialise(context: Context) {
        val retrofitApiClient = RetrofitApiClient.Builder()
            .withContext(context)
            .withApiKey(findApiKey(context))
            .withV1Host(API_HOST)
            .withV2Host(API_HOST_V2)
            .build()
        delegate = ApiClientImpl(retrofitApiClient)

        POICategories.backgroundLoad()
        PhotomapCategories.backgroundLoad()
        initMessages(context)
    }

    fun initialiseForTests(context: Context, delegate: CycleStreetsApi) {
        this.delegate = delegate
        initMessages(context)
    }

    fun setCustomiser(customiser: ApiCustomiser) {
        ApiClient.customiser = customiser
    }

    fun getMessage(resId: Int): String {
        return messages[resId]!!
    }

    private fun findApiKey(context: Context): String? {
        try {
            val ai = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle = ai.metaData
            return bundle.getString("CycleStreetsAPIKey")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun initMessages(context: Context) {
        messages = setOf(
            R.string.registration_ok,
            R.string.registration_error_prefix,
            R.string.feedback_ok,
            R.string.feedback_error_prefix,
            R.string.signin_ok,
            R.string.signin_error_prefix,
            R.string.signin_default_error,
            R.string.upload_ok,
            R.string.upload_error_prefix
        ).map { resId -> (resId to context.getString(resId)) }.toMap()
    }

    override fun getJourneyJson(plan: String, leaving: String?, arriving: String?, speed: Int, lonLat: DoubleArray): String {
        return delegate.getJourneyJson(plan, leaving, arriving, speed, lonLat)
    }
    override fun getJourneyJson(plan: String, itineraryId: Long): String {
        return delegate.getJourneyJson(plan, itineraryId)
    }
    override fun getPhotomapCategories(): PhotomapCategories {
        return delegate.getPhotomapCategories()
    }
    override fun getPhotos(e: Double, w: Double, n: Double, s: Double): Photos {
        return delegate.getPhotos(w, e, n, s)
    }
    override fun getUserJourneys(username: String): UserJourneys {
        return delegate.getUserJourneys(username)
    }
    override fun geoCoder(search: String, n: Double, s: Double, e: Double, w: Double): GeoPlaces {
        return delegate.geoCoder(search, n, s, e, w)
    }
    override fun sendFeedback(itinerary: Int, comments: String, name: String, email: String): Result {
        return delegate.sendFeedback(itinerary, comments, name, email)
    }
    override fun uploadPhoto(filename: String, username: String, password: String, lon: Double, lat: Double, metaCat: String, category: String, dateTime: String, caption: String): Upload.Result {
        return delegate.uploadPhoto(filename, username, password, lon, lat, metaCat, category, dateTime, caption)
    }
    override fun signin(username: String, password: String): Signin.Result {
        return delegate.signin(username, password)
    }
    override fun register(username: String, password: String, name: String, email: String): Result {
        return delegate.register(username, password, name, email)
    }
    override fun getPOICategories(iconSize: Int): POICategories {
        return delegate.getPOICategories(iconSize)
    }
    override fun getPOIs(key: String, lonE: Double, lonW: Double, latN: Double, latS: Double): List<POI> {
        return delegate.getPOIs(key, lonE, lonW, latN, latS)
    }
    override fun getPOIs(key: String, lon: Double, lat: Double, radius: Int): List<POI> {
        return delegate.getPOIs(key, lon, lat, radius)
    }
    override fun getBlogEntries(): Blog {
        return delegate.getBlogEntries()
    }

}

private class ApiClientImpl(val retrofitApiClient: RetrofitApiClient): CycleStreetsApi {
    override fun getJourneyJson(plan: String,
                                leaving: String?,
                                arriving: String?,
                                speed: Int,
                                lonLat: DoubleArray): String {
        val points = itineraryPoints(*lonLat)
        return retrofitApiClient.getJourneyJson(plan, points, leaving, arriving, speed)
    }

    override fun getJourneyJson(plan: String,
                                itineraryId: Long): String {
        return retrofitApiClient.retrievePreviousJourneyJson(plan, itineraryId)
    }

    override fun getPhotomapCategories(): PhotomapCategories {
        return retrofitApiClient.photomapCategories
    }

    override fun getPhotos(e: Double,
                           w: Double,
                           n: Double,
                           s: Double): Photos {
        return retrofitApiClient.getPhotos(w, s, e, n)
    }

    override fun getUserJourneys(username: String): UserJourneys {
        return retrofitApiClient.getUserJourneys(username)
    }

    override fun geoCoder(search: String,
                          n: Double,
                          s: Double,
                          e: Double,
                          w: Double): GeoPlaces {
        return retrofitApiClient.geoCoder(search, w, s, e, n)
    }

    override fun sendFeedback(itinerary: Int,
                              comments: String,
                              name: String,
                              email: String): Result {
        return retrofitApiClient.sendFeedback(itinerary, comments, name, email)
    }

    override fun uploadPhoto(filename: String,
                             username: String,
                             password: String,
                             lon: Double,
                             lat: Double,
                             metaCat: String,
                             category: String,
                             dateTime: String,
                             caption: String): Upload.Result {
        return retrofitApiClient.uploadPhoto(username, password, lon, lat, java.lang.Long.valueOf(dateTime),
                                             category, metaCat, caption, filename)
    }

    override fun signin(username: String,
                        password: String): Signin.Result {
        return retrofitApiClient.authenticate(username, password)
    }

    override fun register(username: String,
                          password: String,
                          name: String,
                          email: String): Result {
        return retrofitApiClient.register(username, password, name, email)
    }

    override fun getPOICategories(iconSize: Int): POICategories {
        return retrofitApiClient.getPOICategories(iconSize)
    }

    override fun getPOIs(key: String,
                         lonE: Double,
                         lonW: Double,
                         latN: Double,
                         latS: Double): List<POI> {
        return retrofitApiClient.getPOIs(key, lonW, latS, lonE, latN)
    }

    override fun getPOIs(key: String,
                         lon: Double,
                         lat: Double,
                         radius: Int): List<POI> {
        return retrofitApiClient.getPOIs(key, lon, lat, radius)
    }

    override fun getBlogEntries(): Blog {
        return retrofitApiClient.blogEntries
    }

    /////////////////////////////////////////////////////
    private fun itineraryPoints(vararg lonLat: Double): String {
        val sb = StringBuilder()
        var i = 0
        while (i != lonLat.size) {
            if (i != 0)
                sb.append("|")
            sb.append(lonLat[i]).append(",").append(lonLat[i + 1])
            i += 2
        }
        return sb.toString()
    }
}