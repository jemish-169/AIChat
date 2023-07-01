package com.Practice.AIChat

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practice.aichat.API.RetrofitHelper
import com.practice.aichat.Models.Google_Response
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.IOException
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    val GOOGLE_API_KEY = BuildConfig.GOOGLE_API_KEY
    val CX = BuildConfig.CX
    val GPT_API_KEY = BuildConfig.GPT_API_KEY
    private lateinit var sendButton: ImageButton
    private var messageList: MutableList<Message> = ArrayList()
    private lateinit var messageAdapter: MessageAdapter
    private var GPT_VERSION = "text-davinci-003"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        messageList = ArrayList()
        recyclerView = findViewById(R.id.recycler_view)
        messageEditText = findViewById(R.id.message_edit_text)
        sendButton = findViewById(R.id.send_btn)

        // Setup recycler view
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
        val llm = LinearLayoutManager(this)
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm

        supportActionBar?.title = "AIChat with $GPT_VERSION"

        sendButton.setOnClickListener {
            val question = messageEditText.text.toString().trim()
            addToChat(question, Message.SENT_BY_ME)
            messageEditText.setText("")
            callAPI(question)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    @SuppressLint("ResourceType")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.app_bar_settings) {

            val builder = AlertDialog.Builder(this)
            val view: View =
                LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_box, null)
            builder.setView(view)
            val dialog = builder.create()

            val okButton = view.findViewById<Button>(R.id.OkButton)
            val cancelButton = view.findViewById<Button>(R.id.CancelButton)
            val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)

            okButton.setOnClickListener {
                val selectedID = radioGroup.checkedRadioButtonId
                val selectedRadiobutton = view.findViewById<RadioButton>(selectedID)
                GPT_VERSION = selectedRadiobutton.text.toString()
                supportActionBar?.title = "AIChat with $GPT_VERSION"
                dialog.dismiss()
            }
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(true)
            dialog.show()

        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addToChat(message: String, sentBy: String) {
        runOnUiThread {
            messageList.add(Message(message, sentBy))
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
        }
    }

    private fun addResponse(response: String) {
        messageList.removeAt(messageList.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    private fun callAPI(question: String) {
        // okhttp
        messageList.add(Message("Typing... ", Message.SENT_BY_BOT))

        if (GPT_VERSION == "Google Search") {
            val interFace = RetrofitHelper.create()
            interFace.urlHit(apiKey = GOOGLE_API_KEY, cx = CX, query = question)
                .enqueue(object : retrofit2.Callback<Google_Response> {
                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onResponse(
                        call: retrofit2.Call<Google_Response>,
                        response: retrofit2.Response<Google_Response>
                    ) {
                        val response_list = response.body()?.items
                        var response_text = ""
                        var response_size: Int? = response_list?.size
                        var itr = 0

                        if (response_size != null) {
                            while (response_size > 1 && itr < 10) {

                                val link_text= "<a href='${response_list?.get(itr)?.link.toString()}'>${
                                    response_list?.get(
                                        itr
                                    )?.link.toString()
                                }</a>"
                                val spannedText: Spanned = Html.fromHtml(link_text, Html.FROM_HTML_MODE_LEGACY)

                                response_text += "Google Search Result ${itr + 1}: \n\n"
                                response_text += (response_list?.get(itr)?.title.toString() + "\n\n")
                                response_text += spannedText
                                response_text += "\n\n"
                                response_text += (response_list?.get(itr)?.snippet.toString() + "\n\n\n")

                                response_size -= 1
                                itr += 1

                            }
                            addResponse(response_text.trim())
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<Google_Response>,
                        t: Throwable
                    ) {
                        addResponse("Sorry, Network Failure!")

                    }

                })
        } else {
            val jsonBody = JSONObject()
            try {
                jsonBody.put("model", GPT_VERSION)
                jsonBody.put("prompt", question)
                jsonBody.put("max_tokens", 4000)
                jsonBody.put("temperature", 0)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val body = RequestBody.create(JSON, jsonBody.toString())
            val request = Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header(
                    "Authorization",
                    GPT_API_KEY
                )
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    addResponse("Failed to load response due to " + e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    Log.d("my-message", GPT_VERSION)
                    if (response.isSuccessful) {
                        var jsonObject: JSONObject? = null
                        try {
                            jsonObject = response.body?.string()?.let { JSONObject(it) }
                            val jsonArray = jsonObject?.getJSONArray("choices")
                            val result = jsonArray?.getJSONObject(0)?.getString("text")
                            if (result != null) {
                                addResponse(result.trim())
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        addResponse("Failed to load response due to " + response.body!!.toString())
                    }
                }
            })
        }
    }
}
