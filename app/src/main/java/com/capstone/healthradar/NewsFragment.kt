package com.capstone.healthradar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class NewsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val articles = mutableListOf<NewsArticle>()
    private val apiKey = "81d5ffd08f9042f5bd1d49dd6c6404b8" // Your working API key

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        recyclerView = view.findViewById(R.id.newsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = NewsAdapter(articles)

        fetchNews()

        return view
    }

    private fun fetchNews() {
        val url =
            "https://newsapi.org/v2/everything?q=philippines+(infectious+disease+OR+infection+OR+virus+OR+outbreak)&from=2025-06-15&sortBy=publishedAt&language=en&apiKey=$apiKey"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to fetch news", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    activity?.runOnUiThread {
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return
                }

                response.body?.string()?.let { jsonString ->
                    try {
                        val jsonObject = JSONObject(jsonString)
                        val articlesArray = jsonObject.getJSONArray("articles")

                        articles.clear()

                        for (i in 0 until articlesArray.length()) {
                            val obj = articlesArray.getJSONObject(i)

                            val title = obj.optString("title", "No Title")
                            val description = obj.optString("description", "No Description")
                            val url = obj.optString("url", "")
                            val urlToImage = obj.optString("urlToImage", "")

                            if (title.isNotBlank() && url.isNotBlank()) {
                                val article = NewsArticle(title, description, url, urlToImage)
                                articles.add(article)
                            }
                        }

                        activity?.runOnUiThread {
                            if (isAdded) {
                                recyclerView.adapter?.notifyDataSetChanged()
                            }
                        }

                    } catch (e: Exception) {
                        activity?.runOnUiThread {
                            if (isAdded) {
                                Toast.makeText(requireContext(), "Parsing error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })
    }
}
