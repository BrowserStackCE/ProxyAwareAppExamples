package com.example.proxyawareapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.loopj).setOnClickListener {
            LoopJRequest(URI.create("http://ip-api.com/json"));
        }
        findViewById<Button>(R.id.volley).setOnClickListener {
            VolleyRequest(URI.create("http://ip-api.com/json"));
        }
        findViewById<Button>(R.id.httpurlconnection).setOnClickListener {
            HttpUrlConnectionRequest(URI.create("http://ip-api.com/json"));
        }
    }

    private fun findProxy(uri: URI): Proxy? {
        try {
            val selector = ProxySelector.getDefault()
            val proxyList = selector.select(uri)
            // Look for HTTP Proxy
            return  proxyList.find { p -> p.type() == Proxy.Type.HTTP }
        } catch (e: IllegalArgumentException) {
        }
        return Proxy.NO_PROXY
    }

    fun LoopJRequest(url: URI){
        var proxy = findProxy(url);
        var HttpClient = AsyncHttpClient()
        if(proxy != null && proxy.type() == Proxy.Type.HTTP) {
            val addr: InetSocketAddress = proxy.address() as InetSocketAddress
            HttpClient.setProxy(addr.hostString,addr.port);
        }
        HttpClient.get(url.toString(), object : AsyncHttpResponseHandler() {
            override fun onStart() {
                // called before request is started
            }

            override fun onSuccess(
                statusCode: Int,
                headers: Array<Header?>?,
                response: ByteArray?
            ) {
                if(response != null) {
                    findViewById<TextView>(R.id.textView).setText(String(response, Charsets.UTF_8))
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<Header?>?,
                errorResponse: ByteArray?,
                e: Throwable?
            ) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            override fun onRetry(retryNo: Int) {
                // called when request is retried
            }
        })
    }

    fun VolleyRequest(url: URI){
        val mRequestQueue = Volley.newRequestQueue(baseContext, ProxiedHurlStack())
        val stringRequest = StringRequest(Request.Method.GET,url.toString(),Response.Listener<String> { response ->
            // Do something with the response
            findViewById<TextView>(R.id.textView).setText(response.toString());
        },
            Response.ErrorListener { error ->
                // Handle error

            })
        mRequestQueue.add(stringRequest);

    }

    fun  HttpUrlConnectionRequest(url: URI){
        Thread(Runnable {
            var proxy = findProxy(url);
            var conn:HttpURLConnection
            if(proxy != null && proxy.type() == Proxy.Type.HTTP) {
                conn = url.toURL().openConnection(proxy) as HttpURLConnection;
            }else{
                conn = url.toURL().openConnection() as HttpURLConnection;
            }
            conn.requestMethod = "GET"
            val responseCode: Int = conn.getResponseCode()
            if(responseCode == HttpURLConnection.HTTP_OK){
                var inp = BufferedReader(InputStreamReader(conn.inputStream));
                var response: String;
                var responseBuffer = StringBuffer()

                while(true){
                    response = inp.readLine() ?: break;
                    responseBuffer.append(response)
                }
                inp.close();
                findViewById<TextView>(R.id.textView).setText(responseBuffer.toString());
            }
        }).start()
    }

    class ProxiedHurlStack : HurlStack() {
        private fun findProxy(uri: URI): Proxy? {
            try {
                val selector = ProxySelector.getDefault()
                val proxyList = selector.select(uri)
                if (proxyList.size > 1) return proxyList[0]
            } catch (e: IllegalArgumentException) {
            }
            return Proxy.NO_PROXY
        }

        @Throws(IOException::class)
        override fun createConnection(url: URL): HttpURLConnection {
            val proxy: Proxy? = findProxy(url.toURI());
            if (proxy != null && proxy.type() == Proxy.Type.HTTP) {
                return url.openConnection(proxy) as HttpURLConnection
            } else {
                return url.openConnection() as HttpURLConnection
            }
        }
    }
}