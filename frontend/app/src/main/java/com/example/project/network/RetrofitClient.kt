package com.example.project.network

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.project.R
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    //private const val BASE_URL = "https://20.30.69.128:8443/"
    private const val BASE_URL = "https://20.30.0.165:8443/"
    private var retrofit: Retrofit? = null
    private var authToken: String? = null
    private const val TAG = "RetrofitClient"

    // Initialize with application context
    fun initialize(context: Context) {
        if (retrofit == null) {
            synchronized(this) {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getOkHttpClient(context))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    Log.d(TAG, "Retrofit initialized with base URL: $BASE_URL")
                }
            }
        }
    }

    private fun getOkHttpClient(context: Context): OkHttpClient {
        return try {
            OkHttpClient.Builder().apply {
                // 1. Add authentication interceptor FIRST
                addInterceptor { chain ->
                    val request = chain.request().newBuilder().apply {
                        authToken?.let { header("Authorization", it) }
                    }.build()
                    chain.proceed(request)
                }

                // 2. Add logging
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                })

                // 3. Bypass SSL verification (TEMPORARY for testing only)
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
                })

                val sslContext = SSLContext.getInstance("SSL").apply {
                    init(null, trustAllCerts, java.security.SecureRandom())
                }

                sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                hostnameVerifier { _, _ -> true }

                // 4. Timeouts
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }.build()
        } catch (e: Exception) {
            Log.e(TAG, "SSL configuration failed", e)
            throw RuntimeException("SSL configuration failed: ${e.message}", e)
        }
    }


    // Custom interceptors as inner classes for better organization
    private class AuthInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder().apply {
                authToken?.let { token ->
                    header("Authorization", token)
                    Log.d(TAG, "Added Authorization header")
                }
                // Add common headers
                header("Accept", "application/json")
                header("Content-Type", "application/json")
            }.build()

            val response = chain.proceed(request)

            if (response.code == 401) {
                Log.w(TAG, "Authentication failed - clearing token")
                authToken = null
            }
            return response
        }
    }

    private class LoggingInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            Log.d(TAG, "Request: ${request.method} ${request.url}")

            val response = chain.proceed(request)
            Log.d(TAG, "Response: ${response.code} ${response.message}")

            return response
        }
    }

    private class ConnectionInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("Connection", "keep-alive")
                .build()
            return chain.proceed(request)
        }
    }

    private fun loadCertificate(context: Context): X509Certificate {
        return context.resources.openRawResource(R.raw.server).use { inputStream ->
            CertificateFactory.getInstance("X.509")
                .generateCertificate(inputStream) as X509Certificate
        }.also {
            Log.d(TAG, "Loaded certificate: ${it.subjectDN}")
        }
    }

    private fun createTrustManager(certificate: X509Certificate) = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            if (chain.isEmpty() || chain[0].encoded.sha256Fingerprint() != certificate.encoded.sha256Fingerprint()) {
                throw java.security.cert.CertificateException("Certificate fingerprint mismatch")
            }
        }

        override fun getAcceptedIssuers() = arrayOf(certificate)
    }

    // Public API
    val apiService: ApiService
        get() = retrofit?.create(ApiService::class.java)
            ?: throw IllegalStateException("Retrofit not initialized. Call initialize() first")

    fun setCredentials(email: String, password: String) {
        val credentials = "$email:$password".toByteArray(Charsets.UTF_8)
        authToken = "Basic ${Base64.encodeToString(credentials, Base64.NO_WRAP)}"
        Log.d(TAG, "Credentials set for user: $email")
    }


    fun clearCredentials() {
        authToken = null
        Log.d(TAG, "Credentials cleared")
    }

    private fun ByteArray.sha256Fingerprint(): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(this)
            .joinToString(":") { "%02X".format(it) }
    }
}