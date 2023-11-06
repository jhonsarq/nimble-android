package com.jhonsarq.nimblesurvey.utilities

import android.content.ContentValues
import android.content.Context
import com.jhonsarq.nimblesurvey.model.Answer
import com.jhonsarq.nimblesurvey.model.ApiResponse
import com.jhonsarq.nimblesurvey.model.LoginRequest
import com.jhonsarq.nimblesurvey.model.LogoutRequest
import com.jhonsarq.nimblesurvey.model.Question
import com.jhonsarq.nimblesurvey.model.RefreshTokenRequest
import com.jhonsarq.nimblesurvey.model.ResetPasswordRequest
import com.jhonsarq.nimblesurvey.model.Survey
import com.jhonsarq.nimblesurvey.model.SurveyRequest
import com.jhonsarq.nimblesurvey.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

class ApiClient {
    interface ApiService {
        @POST
        suspend fun login(@Url url: String, @Body parameters: LoginRequest): Response<Map<*, *>?>

        @POST
        suspend fun resetPassword(@Url url: String, @Body parameters: ResetPasswordRequest): Response<Map<*, *>?>

        @GET
        suspend fun getProfile(@Url url: String): Response<Map<*, *>?>

        @POST
        suspend fun refreshToken(@Url url: String, @Body parameters: RefreshTokenRequest): Response<Map<*, *>>

        @POST
        suspend fun logout(@Url url: String, @Body parameters: LogoutRequest): Response<Map<*, *>>

        @GET
        suspend fun getSurveys(@Url url: String): Response<Map<*, *>>

        @GET
        suspend fun getSurvey(@Url url: String): Response<Map<*, *>>

        @POST
        suspend fun submitSurvey(@Url url: String, @Body parameters: SurveyRequest): Response<Map<*, *>>
    }

    private val constants = Constants()
    private val utils = Utils()
    private val retrofit = Retrofit.Builder().baseUrl(constants.apiUrl).addConverterFactory(GsonConverterFactory.create()).build()

    suspend fun login(parameters: LoginRequest): User {
        return withContext(Dispatchers.IO) {
            val response: Response<Map<*, *>?> = retrofit.create(ApiService::class.java).login("oauth/token", parameters)
            var user = User(false, null, null, null, null, null, null, "There was an error, please try again.")

            if(response.isSuccessful) {
                val loginResponse = response.body()

                if(loginResponse != null) {
                    if(loginResponse.containsKey("data")) {
                        val data: Map<*, *> = loginResponse["data"] as Map<*, *>
                        val attributes: Map<*, *> = data["attributes"] as Map<*, *>
                        val accessToken: String = attributes["access_token"].toString()
                        val expiresIn: Long = attributes["expires_in"].toString().toDouble().toLong()
                        val refreshToken: String = attributes["refresh_token"].toString()
                        val createdAt: Long = attributes["created_at"].toString().toDouble().toLong()

                        user = User(true, accessToken, expiresIn, refreshToken, createdAt, null, null, null)
                    }

                    if(loginResponse.containsKey("errors")) {
                        val errors: Map<*, *> = loginResponse["errors"] as Map<*, *>
                        val detail: String = errors["detail"].toString()

                        user = User(false, null, null, null, null, null, null, detail)
                    }
                }
            } else {
                val loginResponse: Map<*, *>? = utils.jsonToMap(response.errorBody()!!.string())

                if (loginResponse != null) {
                    if(loginResponse.containsKey("errors")) {
                        val errors: List<Map<*, *>> = loginResponse["errors"] as List<Map<*, *>>
                        val detail: String = errors[0]["detail"].toString()

                        user = User(false, null, null, null, null, null, null, detail)
                    }
                }
            }

            user
        }
    }

    suspend fun resetPassword(parameters: ResetPasswordRequest): String {
        return withContext(Dispatchers.IO) {
            val response: Response<Map<*, *>?> = retrofit.create(ApiService::class.java).resetPassword("passwords", parameters)
            var text = ""

            if(response.isSuccessful) {
                val resetResponse = response.body()

                if(resetResponse != null) {
                    if(resetResponse.containsKey("meta")) {
                        val meta: Map<*, *> = resetResponse["meta"] as Map<*, *>

                        text = meta["message"].toString()
                    }
                }
            } else {
                val resetResponse: Map<*, *>? = utils.jsonToMap(response.errorBody()!!.string())

                if(resetResponse != null) {
                    val errors: List<Map<*, *>> = resetResponse["errors"] as List<Map<*, *>>

                    text = errors[0]["detail"].toString()
                }
            }

            text
        }
    }

    suspend fun getProfile(userData: User, context: Context): User {
        val retrofitToken = Retrofit.Builder().baseUrl(constants.apiUrl).addConverterFactory(GsonConverterFactory.create()).client(OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder().header("Authorization", "Bearer ${userData.accessToken}")
            val request = requestBuilder.build()

            chain.proceed(request)
        }.build()).build()

        return withContext(Dispatchers.IO) {
            val response: Response<Map<*, *>?> = retrofitToken.create(ApiService::class.java).getProfile("me")
            var user = User(false, null, null, null, null, null, null, "There was an error, please try again.")

            if(response.isSuccessful) {
                val profileResponse = response.body()

                if(profileResponse != null) {
                    if(profileResponse.containsKey("data")) {
                        val data: Map<*, *> = profileResponse["data"] as Map<*, *>
                        val attributes: Map<*, *> = data["attributes"] as Map<*, *>
                        val name: String = attributes["name"].toString()
                        val avatarUrl: String = attributes["avatar_url"].toString()
                        val db = Database(context)

                        val userValues = ContentValues().apply {
                            put("accessToken", userData.accessToken)
                            put("expiresIn", userData.expiresIn)
                            put("refreshToken", userData.refreshToken)
                            put("createdAt", userData.createdAt)
                            put("name", name)
                            put("avatarUrl", avatarUrl)
                        }

                        db.addData("user", userValues)

                        user = User(true, userData.accessToken, userData.expiresIn, userData.refreshToken, userData.createdAt, name, avatarUrl, null)
                    }
                }
            }

            user
        }
    }

    suspend fun refreshToken(userData: User, context: Context, parameters: RefreshTokenRequest): User {
        return withContext(Dispatchers.IO) {
            val response: Response<Map<*, *>> = retrofit.create(ApiService::class.java).refreshToken("oauth/token", parameters)
            var user = User(false, null, null, null, null, null, null, "There was an error, please try again.")

            if(response.isSuccessful) {
                val refreshTokenResponse = response.body()

                if(refreshTokenResponse != null) {
                    if(refreshTokenResponse.containsKey("data")) {
                        val data: Map<*, *> = refreshTokenResponse["data"] as Map<*, *>
                        val attributes: Map<*, *> = data["attributes"] as Map<*, *>
                        val accessToken: String = attributes["access_token"].toString()
                        val expiresIn: Long = attributes["expires_in"].toString().toDouble().toLong()
                        val refreshToken: String = attributes["refresh_token"].toString()
                        val createdAt: Long = attributes["created_at"].toString().toDouble().toLong()
                        val db = Database(context)

                        val userValues = ContentValues().apply {
                            put("accessToken", accessToken)
                            put("expiresIn", expiresIn)
                            put("refreshToken", refreshToken)
                            put("createdAt", createdAt)
                        }

                        db.updateData("user", userValues, null, null)

                        user = User(true, accessToken, expiresIn, refreshToken, createdAt, userData.name, userData.avatarUrl, null)
                    }
                }
            } else {
                val refreshTokenResponse: Map<*, *>? = utils.jsonToMap(response.errorBody()!!.string())

                if (refreshTokenResponse != null) {
                    if(refreshTokenResponse.containsKey("errors")) {
                        val errors: List<Map<*, *>> = refreshTokenResponse["errors"] as List<Map<*, *>>
                        val detail: String = errors[0]["detail"].toString()

                        user = User(false, null, null, null, null, null, null, detail)
                    }
                }
            }

            user
        }
    }

    suspend fun logout(context: Context, parameters: LogoutRequest): User {
        return  withContext(Dispatchers.IO) {
            val response: Response<Map<*, *>> = retrofit.create(ApiService::class.java).logout("oauth/revoke", parameters)

            if(response.isSuccessful) {
                val db = Database(context)

                db.deleteData("user", null, null)
            }

            User(null, null, null, null, null, null, null, null)
        }
    }

    suspend fun getSurveys(accessToken: String): List<Survey> {
        val retrofitToken = Retrofit.Builder().baseUrl(constants.apiUrl).addConverterFactory(GsonConverterFactory.create()).client(OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder().header("Authorization", "Bearer $accessToken")
            val request = requestBuilder.build()

            chain.proceed(request)
        }.build()).build()

        return withContext(Dispatchers.IO) {
            val response: Response<Map<*, *>> = retrofitToken.create(ApiService::class.java).getSurveys("surveys?page[number]=1&page[size]=50")
            val surveys: MutableList<Survey> = mutableListOf()

            if(response.isSuccessful) {
                val surveysResponse = response.body()

                if(surveysResponse != null) {
                    if(surveysResponse.containsKey("data")) {
                        val surveysList: List<Map<*, *>> = surveysResponse["data"] as List<Map<*, *>>

                        for(survey in surveysList) {
                            val surveyId: String = survey["id"].toString()
                            val attributes: Map<*, *> = survey["attributes"] as Map<*, *>
                            val title: String = attributes["title"].toString()
                            val description: String = attributes["description"].toString()
                            val coverImageUrl: String = attributes["cover_image_url"].toString()
                            val type: String = attributes["survey_type"].toString()

                            surveys.add(Survey(surveyId, title, description, coverImageUrl, type, null, null, null))
                        }
                    }
                }
            }

            surveys
        }
    }

    suspend fun getSurvey(accessToken: String, surveyId: String): Survey? {
        val retrofitToken = Retrofit.Builder().baseUrl(constants.apiUrl).addConverterFactory(GsonConverterFactory.create()).client(OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder().header("Authorization", "Bearer $accessToken")
            val request = requestBuilder.build()

            chain.proceed(request)
        }.build()).build()

        return withContext(Dispatchers.IO) {
            val response: Response<Map<*, *>> = retrofitToken.create(ApiService::class.java).getSurvey("surveys/$surveyId")
            var survey: Survey? = null

            if(response.isSuccessful) {
                val surveyResponse = response.body()

                if(surveyResponse != null) {
                    if(surveyResponse.containsKey("data")) {
                        val surveyData: Map<*, *> = surveyResponse["data"] as Map<*, *>
                        val id: String = surveyData["id"].toString()
                        val attributes: Map<*, *> = surveyData["attributes"] as Map<*, *>
                        val included: List<Map<*, *>> = surveyResponse["included"] as List<Map<*, *>>
                        val includedFirstAttributes: Map<*, *> = included[0]["attributes"] as Map<*, *>
                        val includedLastAttributes: Map<*, *> = included[included.size - 1]["attributes"] as Map<*, *>
                        val title: String = attributes["description"].toString()
                        val description: String = includedFirstAttributes["text"].toString()
                        val coverImageUrl = "${includedFirstAttributes["cover_image_url"].toString()}l"
                        val type: String = attributes["survey_type"].toString()
                        val thanksText: String = includedLastAttributes["text"].toString()
                        val thanksCoverImageUrl = "${includedLastAttributes["cover_image_url"].toString()}l"
                        val questionsList: MutableList<Question> = mutableListOf()
                        val answersList: MutableList<Answer> = mutableListOf()
                        val relations: MutableMap<String, MutableList<String>> = mutableMapOf()
                        val questions: MutableList<Question> = mutableListOf()

                        for(i in 1..included.size - 2) {
                            val itemAttributes: Map<*, *> = included[i]["attributes"] as Map<*, *>
                            val displayType: String = itemAttributes["display_type"].toString()

                            if(displayType != "message") {
                                val itemType: String = included[i]["type"].toString()
                                val itemId: String = included[i]["id"].toString()
                                val itemText: String = itemAttributes["text"].toString()
                                val itemOrder: Int = itemAttributes["display_order"].toString().toDouble().toInt()
                                val itemCoverImageUrl = "${itemAttributes["cover_image_url"]}l"

                                if(itemType == "question") {
                                    var itemHelpText: String? = null
                                    val itemPick: String = itemAttributes["pick"].toString()
                                    val relationships: Map<*, *> = included[i]["relationships"] as Map<*, *>
                                    val answersArray: Map<*, *> = relationships["answers"] as Map<*, *>
                                    val answersArrayData: List<Map<*, *>> = answersArray["data"] as List<Map<*, *>>

                                    if(itemAttributes["help_text"] != null) {
                                        itemHelpText = itemAttributes["help_text"].toString()
                                    }

                                    relations[itemId] = mutableListOf()

                                    for(element in answersArrayData) {
                                        val answerId: String = element["id"].toString()

                                        relations[itemId]?.add(answerId)
                                    }

                                    questionsList.add(Question(itemId, itemText, itemHelpText, itemOrder, itemPick, displayType, itemCoverImageUrl, null))
                                }

                                if(itemType == "answer") {
                                    val typeItem: String = itemAttributes["response_class"].toString()
                                    var itemPlaceholder: String? = null

                                    if(itemAttributes["input_mask_placeholder"] != null) {
                                        itemPlaceholder = itemAttributes["input_mask_placeholder"].toString()
                                    }

                                    answersList.add(Answer(itemId, itemText, itemOrder, typeItem, itemPlaceholder))
                                }
                            }
                        }

                        for(question in questionsList) {
                            val answersIds = relations[question.id]
                            val answers: MutableList<Answer> = mutableListOf()

                            for(answer in answersList) {
                                if(answersIds!!.contains(answer.id)) {
                                    answers.add(answer)
                                }
                            }

                            questions.add(Question(question.id, question.text, question.helpText, question.order, question.pick, question.type, question.coverImageUrl, answers))
                        }

                        survey = Survey(id, title, description, coverImageUrl, type, thanksText, thanksCoverImageUrl, questions)
                    }
                }
            }

            survey
        }
    }

    suspend fun submitSurvey(accessToken: String, params: SurveyRequest): ApiResponse? {
        val retrofitToken = Retrofit.Builder().baseUrl(constants.apiUrl).addConverterFactory(GsonConverterFactory.create()).client(OkHttpClient.Builder().addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder().header("Authorization", "Bearer $accessToken")
            val request = requestBuilder.build()

            chain.proceed(request)
        }.build()).build()

        return withContext(Dispatchers.IO) {
            val response: Response<Map<*, *>> = retrofitToken.create(ApiService::class.java).submitSurvey("responses", params)
            var apiResponse: ApiResponse? = null

            if(response.isSuccessful) {
                apiResponse = ApiResponse(true, "")
            } else {
                val refreshTokenResponse: Map<*, *>? = utils.jsonToMap(response.errorBody()!!.string())

                if (refreshTokenResponse != null) {
                    if(refreshTokenResponse.containsKey("errors")) {
                        val errors: List<Map<*, *>> = refreshTokenResponse["errors"] as List<Map<*, *>>
                        val detail: String = errors[0]["detail"].toString()

                        apiResponse = ApiResponse(false, detail)
                    }
                }
            }

            apiResponse
        }
    }
}