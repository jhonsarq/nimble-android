package com.jhonsarq.nimblesurvey.view

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.jhonsarq.nimblesurvey.R
import com.jhonsarq.nimblesurvey.databinding.FragmentTakeSurveyBinding
import com.jhonsarq.nimblesurvey.model.Answer
import com.jhonsarq.nimblesurvey.model.Question
import com.jhonsarq.nimblesurvey.model.Survey
import com.jhonsarq.nimblesurvey.model.SurveyAnswerRequest
import com.jhonsarq.nimblesurvey.model.SurveyQuestionRequest
import com.jhonsarq.nimblesurvey.model.SurveyRequest
import com.jhonsarq.nimblesurvey.utilities.Utils
import com.jhonsarq.nimblesurvey.viewmodel.LoaderViewModel
import com.jhonsarq.nimblesurvey.viewmodel.SurveysViewModel
import com.jhonsarq.nimblesurvey.viewmodel.UserViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import kotlin.math.floor

class TakeSurveyFragment : DialogFragment() {
    private var _binding: FragmentTakeSurveyBinding? = null
    private val binding get() = _binding!!
    private val loaderViewModel: LoaderViewModel by activityViewModels()
    private val surveysViewModel: SurveysViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var questions: List<Question>
    private lateinit var currentSurvey: Survey
    private var currentQuestion: Int = 0
    private var questionsRequest: MutableList<SurveyQuestionRequest> = mutableListOf()
    private val utils = Utils()
    private var successFragment = SuccessFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTakeSurveyBinding.inflate(inflater, container, false)

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loaderViewModel.setLoader(true)

        val surveyContainer = binding.surveyContainer

        surveysViewModel.survey.observe(viewLifecycleOwner) { survey ->
            if(survey != null) {
                currentSurvey = survey
                questions = survey.questions!!

                if(questions.size > 1) {
                    binding.nextButton.visibility = View.VISIBLE
                    binding.sendSurvey.visibility = View.GONE
                } else {
                    binding.nextButton.visibility = View.GONE
                    binding.sendSurvey.visibility = View.VISIBLE
                }

                for((i, question) in questions.withIndex()) {
                    val constraintLayout = ConstraintLayout(requireContext())
                    val image = ImageView(requireContext())
                    val overlay = View(requireContext())
                    val scrollView = NestedScrollView(requireContext())
                    val linearLayout = LinearLayout(requireContext())
                    val firstTextView = TextView(requireContext())
                    val secondTextView = TextView(requireContext())

                    constraintLayout.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)

                    if(i > 0) {
                        constraintLayout.visibility = View.GONE
                    }

                    image.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                    image.scaleType = ImageView.ScaleType.CENTER_CROP
                    image.id = View.generateViewId()

                    Picasso.get().load(question.coverImageUrl).into(image, object: Callback {
                        override fun onSuccess() {
                            loaderViewModel.setLoader(false)
                        }

                        override fun onError(e: Exception?) {
                            loaderViewModel.setLoader(false)
                        }
                    })

                    overlay.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                    overlay.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black50))
                    overlay.id = View.generateViewId()

                    scrollView.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                    scrollView.id = View.generateViewId()
                    scrollView.isFillViewport = true

                    constraintLayout.addView(image)
                    constraintLayout.addView(overlay)
                    constraintLayout.addView(scrollView)

                    val constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)

                    constraintSet.connect(scrollView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
                    constraintSet.applyTo(constraintLayout)

                    linearLayout.orientation = LinearLayout.VERTICAL

                    val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    val verticalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80F, resources.displayMetrics).toInt()
                    val horizontalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20F, resources.displayMetrics).toInt()

                    layoutParams.setMargins(0, verticalMargin, 0, verticalMargin)
                    linearLayout.layoutParams = layoutParams
                    linearLayout.setPadding(horizontalPadding, 0, horizontalPadding, 0)

                    firstTextView.text = "${i + 1}/${questions.size}"
                    firstTextView.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.BOLD)

                    firstTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15F)

                    secondTextView.text = question.text
                    secondTextView.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.BOLD)

                    secondTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    secondTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34F)

                    linearLayout.addView(firstTextView)
                    linearLayout.addView(secondTextView)

                    if(question.helpText != null) {
                        val thirdTextView = TextView(requireContext())

                        thirdTextView.text = question.helpText
                        thirdTextView.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.NORMAL)

                        thirdTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        thirdTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17F)

                        linearLayout.addView(thirdTextView)
                    }

                    var form: LinearLayout?

                    when (question.type) {
                        "star", "heart" -> {
                            form = scoreForm(question.answers!!, question.type)
                        }
                        "smiley", "money" -> {
                            form = selectionForm(question.answers!!)
                        }
                        "choice", "slider" -> {
                            form = choiceForm(question.answers!!, question.pick)
                        }
                        "textfield", "textarea" -> {
                            form = textForm(question.answers!!)
                        }
                        "nps" -> {
                            form = npsForm(question.answers!!)
                        }
                        else -> {
                            form = dropdownForm(question.answers!!)
                        }
                    }

                    form.tag = question.id

                    linearLayout.addView(form)
                    scrollView.addView(linearLayout)
                    surveyContainer.addView(constraintLayout)
                }
            }
        }

        binding.dismissButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())

            builder.setTitle(requireContext().getString(R.string.warning))
            builder.setMessage(requireContext().getString(R.string.warning_text))

            builder.setPositiveButton(requireContext().getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                dismiss()
            }

            builder.setNegativeButton(requireContext().getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = builder.create()

            alertDialog.show()
        }

        binding.nextButton.setOnClickListener {
            val currQuestion = questions[currentQuestion]

            checkTextForms()

            if(questionsRequest.any { it.id == currQuestion.id }) {
                val totalQuestions = surveyContainer.childCount
                val nextQuestion = currentQuestion + 1

                surveyContainer.getChildAt(currentQuestion).visibility = View.GONE
                surveyContainer.getChildAt(nextQuestion).visibility = View.VISIBLE

                if(nextQuestion == totalQuestions - 1) {
                    binding.nextButton.visibility = View.GONE
                    binding.sendSurvey.visibility = View.VISIBLE
                }

                currentQuestion = nextQuestion
            } else {
                val currentQuestionType = currQuestion.type
                val currentQuestionPick = currQuestion.pick
                var toastText = requireContext().getString(R.string.invalid_single_form)

                if(currentQuestionPick != "one") {
                    toastText = if(currentQuestionType == "choice" || currentQuestionType == "slider") {
                        requireContext().getString(R.string.invalid_multiple_form)
                    } else {
                        requireContext().getString(R.string.invalid_input_form)
                    }
                }

                Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show()
            }

            utils.hideKeyboard(requireActivity(), view)
        }

        binding.sendSurvey.setOnClickListener {
            val currQuestion = questions[currentQuestion]

            checkTextForms()

            if(questionsRequest.any { it.id == currQuestion.id }) {
                loaderViewModel.setLoader(true)

                val user = userViewModel.user.value!!
                val surveyRequest = SurveyRequest(currentSurvey.id, questionsRequest)

                surveysViewModel.submitSurvey(user.accessToken!!, surveyRequest)
            } else {
                val currentQuestionType = currQuestion.type
                val currentQuestionPick = currQuestion.pick
                var toastText = requireContext().getString(R.string.invalid_single_form)

                if(currentQuestionPick != "one") {
                    toastText = if(currentQuestionType == "choice" || currentQuestionType == "slider") {
                        requireContext().getString(R.string.invalid_multiple_form)
                    } else {
                        requireContext().getString(R.string.invalid_input_form)
                    }
                }

                Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show()
            }

            utils.hideKeyboard(requireActivity(), view)
        }

        surveysViewModel.apiResponse.observe(viewLifecycleOwner) { apiResponse ->
            if(apiResponse != null) {
                if(apiResponse.success) {
                    if(!successFragment.isVisible) {
                        successFragment.isCancelable
                        successFragment.show(requireActivity().supportFragmentManager, "success_fragment")
                    }
                } else {
                    Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_SHORT).show()
                }

                loaderViewModel.setLoader(false)
            }
        }

        surveysViewModel.successClosed.observe(viewLifecycleOwner) { successClosed ->
            if(successClosed != null) {
                if(successClosed) {
                    surveysViewModel.cleanApiResponse()
                    surveysViewModel.setSuccessClosed(null)

                    dismiss()
                }
            }
        }
    }

    private fun checkTextForms() {
        val currQuestion = questions[currentQuestion]

        if(currQuestion.type == "textarea" || currQuestion.type == "textfield") {
            val itemAnswers = currQuestion.answers!!
            val container = utils.findElementByTag(binding.surveyContainer, currQuestion.id) as LinearLayout
            var validForm = true
            val answersRequest: MutableList<SurveyAnswerRequest> = mutableListOf()

            for(i in 0 until container.childCount) {
                val item = container.getChildAt(i) as EditText
                val itemText = item.text.toString().trim()

                if(itemText == "") {
                    validForm = false
                    break
                } else {
                    answersRequest.add(SurveyAnswerRequest(itemAnswers[i].id, itemText))
                }
            }

            if(validForm && answersRequest.isNotEmpty()) {
                questionsRequest.add(SurveyQuestionRequest(currQuestion.id, answersRequest))
            }
        }

        if(currQuestion.type == "choice" || currQuestion.type == "slider") {
            if(!questionsRequest.any { it.id == currQuestion.id }) {
                val itemAnswers = currQuestion.answers!!
                val container = utils.findElementByTag(binding.surveyContainer, currQuestion.id) as LinearLayout
                var validForm = true
                val answersRequest: MutableList<SurveyAnswerRequest> = mutableListOf()

                for(i in 0 until container.childCount) {
                    if(container.getChildAt(i) is EditText) {
                        val item = container.getChildAt(i) as EditText
                        val itemText = item.text.toString().trim()

                        if(itemText == "") {
                            validForm = false
                            break
                        } else {
                            answersRequest.add(SurveyAnswerRequest(itemAnswers[i].id, itemText))
                        }
                    }
                }

                if(validForm && answersRequest.isNotEmpty()) {
                    questionsRequest.add(SurveyQuestionRequest(currQuestion.id, answersRequest))
                }
            }
        }
    }

    private fun scoreForm(answers: List<Answer>, type: String): LinearLayout {
        val linearLayout = setLinearLayout(LinearLayout.HORIZONTAL)

        for((i, _) in answers.withIndex()) {
            val image = when(type) {
                "star" -> R.mipmap.star
                else -> R.mipmap.heart
            }

            val imageView = ImageView(requireContext())
            val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            val horizontalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, resources.displayMetrics).toInt()

            layoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 0)

            imageView.layoutParams = layoutParams
            imageView.setImageResource(image)
            imageView.alpha = 0.5F
            imageView.tag = i

            imageView.setOnClickListener { view ->
                val index = view.tag.toString().toInt()
                val parent = view.parent as LinearLayout
                val answersRequest: MutableList<SurveyAnswerRequest> = mutableListOf()
                val currentQuestion = questions[currentQuestion]
                val currentAnswer = currentQuestion.answers!![index]
                val answerRequest = SurveyAnswerRequest(currentAnswer.id, currentAnswer.text)
                val newQuestionsRequest: MutableList<SurveyQuestionRequest> = mutableListOf()

                answersRequest.add(answerRequest)

                for(question in questionsRequest) {
                    if(question.id != currentQuestion.id) {
                        newQuestionsRequest.add(question)
                    }
                }

                newQuestionsRequest.add(SurveyQuestionRequest(currentQuestion.id, answersRequest))

                questionsRequest = newQuestionsRequest

                for(j in 0 until parent.childCount) {
                    val item = parent.getChildAt(j) as ImageView

                    if(j <= index) {
                        item.alpha = 1F
                    } else {
                        item.alpha = 0.5F
                    }
                }
            }

            linearLayout.addView(imageView)
        }

        return linearLayout
    }

    private fun selectionForm(answers: List<Answer>): LinearLayout {
        val linearLayout = setLinearLayout(LinearLayout.HORIZONTAL)

        for((i) in answers.withIndex()) {
            val imageView = ImageView(requireContext())
            val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            val horizontalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, resources.displayMetrics).toInt()

            layoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 0)

            imageView.layoutParams = layoutParams
            imageView.setImageResource(R.mipmap.hand)
            imageView.alpha = 0.5F
            imageView.tag = i

            imageView.setOnClickListener { view ->
                val index = view.tag.toString().toInt()
                val parent = view.parent as LinearLayout
                val answersRequest: MutableList<SurveyAnswerRequest> = mutableListOf()
                val currentQuestion = questions[currentQuestion]
                val currentAnswer = currentQuestion.answers!![index]
                val answerRequest = SurveyAnswerRequest(currentAnswer.id, currentAnswer.text)
                val newQuestionsRequest: MutableList<SurveyQuestionRequest> = mutableListOf()

                answersRequest.add(answerRequest)

                for(question in questionsRequest) {
                    if(question.id != currentQuestion.id) {
                        newQuestionsRequest.add(question)
                    }
                }

                newQuestionsRequest.add(SurveyQuestionRequest(currentQuestion.id, answersRequest))

                questionsRequest = newQuestionsRequest

                for(j in 0 until parent.childCount) {
                    val item = parent.getChildAt(j) as ImageView

                    if(j == index) {
                        item.alpha = 1F
                    } else {
                        item.alpha = 0.5F
                    }
                }
            }

            linearLayout.addView(imageView)
        }

        return linearLayout
    }

    private fun choiceForm(answers: List<Answer>, pick: String): LinearLayout {
        val linearLayout = setLinearLayout(LinearLayout.VERTICAL)
        val itemHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56F, resources.displayMetrics).toInt()
        val separatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, resources.displayMetrics).toInt()
        val topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, resources.displayMetrics).toInt()
        val horizontalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20F, resources.displayMetrics).toInt()
        val checkboxSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24F, resources.displayMetrics).toInt()

        for((i, answer) in answers.withIndex()) {
            if(answer.type == "string") {
                val editText = EditText(requireContext())
                val editTextParams = LayoutParams(LayoutParams.MATCH_PARENT, itemHeight)

                editTextParams.setMargins(0, topMargin, 0, 0)

                editText.layoutParams = editTextParams
                editText.inputType = InputType.TYPE_CLASS_TEXT
                editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_style1)
                editText.hint = answer.text
                editText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.white30))
                editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                editText.setPadding(horizontalPadding, 0, horizontalPadding, 0)
                editText.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.NORMAL)

                linearLayout.addView(editText)
            } else {
                val textContainer = LinearLayout(requireContext())
                val horizontalContainer = LinearLayout(requireContext())
                val textView = TextView(requireContext())
                val params = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                val horizontalParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0)

                textContainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, itemHeight)
                textContainer.orientation = LinearLayout.VERTICAL
                textContainer.gravity = Gravity.CENTER
                textContainer.tag = i

                horizontalParams.weight = 1F

                horizontalContainer.layoutParams = horizontalParams
                horizontalContainer.orientation = LinearLayout.HORIZONTAL

                params.weight = 1F

                textView.layoutParams = params
                textView.text = answer.text
                textView.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.BOLD)
                textView.alpha = 0.5F
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)

                if(pick == "one") {
                    horizontalContainer.gravity = Gravity.CENTER
                    textView.gravity = Gravity.CENTER

                    horizontalContainer.addView(textView)
                } else {
                    horizontalContainer.gravity = Gravity.CENTER_VERTICAL

                    horizontalContainer.addView(textView)

                    val checkbox = LinearLayout(requireContext())
                    val checkboxImage = ImageView(requireContext())

                    checkbox.layoutParams = LinearLayout.LayoutParams(checkboxSize, checkboxSize)
                    checkbox.background = ContextCompat.getDrawable(requireContext(), R.drawable.whit_border)
                    checkbox.orientation = LinearLayout.VERTICAL
                    checkbox.gravity = Gravity.CENTER

                    checkboxImage.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    checkboxImage.setImageResource(R.mipmap.checkbox)
                    checkboxImage.alpha = 0F

                    checkbox.addView(checkboxImage)

                    horizontalContainer.addView(checkbox)
                }

                textContainer.addView(horizontalContainer)

                if(i < answers.size - 1) {
                    val separator = View(requireContext())

                    separator.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, separatorHeight)
                    separator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

                    textContainer.addView(separator)
                }

                textContainer.setOnClickListener { view ->
                    val index = view.tag.toString().toInt()
                    val parent = view.parent as LinearLayout
                    val answersRequest: MutableList<SurveyAnswerRequest> = mutableListOf()
                    val currentQuestion = questions[currentQuestion]
                    val currentPick = currentQuestion.pick
                    val currentAnswer = currentQuestion.answers!![index]
                    val answerRequest = SurveyAnswerRequest(currentAnswer.id, currentAnswer.text)
                    val newQuestionsRequest: MutableList<SurveyQuestionRequest> = mutableListOf()

                    for(question in questionsRequest) {
                        if(question.id != currentQuestion.id) {
                            newQuestionsRequest.add(question)
                        }
                    }

                    if(currentPick == "any") {
                        if(questionsRequest.any { it.id == currentQuestion.id}) {
                            var currentQuestionRequest: SurveyQuestionRequest? = null

                            for(questionRequest in questionsRequest) {
                                if(questionRequest.id == currentQuestion.id) {
                                    currentQuestionRequest = questionRequest

                                    break
                                }
                            }

                            for(ans in currentQuestionRequest!!.answers) {
                                if(ans.id != currentAnswer.id) {
                                    answersRequest.add(ans)
                                }
                            }

                            if(!currentQuestionRequest.answers.any { it.id == currentAnswer.id}) {
                                answersRequest.add(answerRequest)
                            }
                        } else {
                            answersRequest.add(answerRequest)
                        }
                    } else {
                        answersRequest.add(answerRequest)
                    }

                    if(answersRequest.isNotEmpty()) {
                        newQuestionsRequest.add(SurveyQuestionRequest(currentQuestion.id, answersRequest))
                    }

                    questionsRequest = newQuestionsRequest

                    for(j in 0 until parent.childCount) {
                        if(parent.getChildAt(j) is LinearLayout) {
                            val item = parent.getChildAt(j) as LinearLayout
                            val itemChild = item.getChildAt(0) as LinearLayout
                            val itemGrandChildText = itemChild.getChildAt(0) as TextView

                            if(j == index) {
                                if(currentPick == "one") {
                                    itemGrandChildText.alpha = 1F
                                } else {
                                    val itemGrandChildCheckbox = itemChild.getChildAt(1) as LinearLayout
                                    val checkbox = itemGrandChildCheckbox.getChildAt(0) as ImageView

                                    if(checkbox.alpha == 1F) {
                                        itemGrandChildCheckbox.background = ContextCompat.getDrawable(requireContext(), R.drawable.whit_border)
                                        checkbox.alpha = 0F
                                        itemGrandChildText.alpha = 0.5F
                                    } else {
                                        itemGrandChildCheckbox.background = ContextCompat.getDrawable(requireContext(), R.drawable.button_style1)
                                        checkbox.alpha = 1F
                                        itemGrandChildText.alpha = 1F
                                    }
                                }
                            }

                            if(currentPick == "one" && j != index) {
                                itemGrandChildText.alpha = 0.5F
                            }
                        }
                    }
                }

                linearLayout.addView(textContainer)
            }
        }

        return linearLayout
    }

    private fun npsForm(answers: List<Answer>): LinearLayout {
        val linearLayout = setLinearLayout(LinearLayout.VERTICAL)
        val layoutContainer = LinearLayout(requireContext())
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val elementsHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56F, resources.displayMetrics).toInt()
        val separatorsWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1F, resources.displayMetrics).toInt()

        layoutContainer.layoutParams = layoutParams
        layoutContainer.orientation = LinearLayout.HORIZONTAL
        layoutContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.whit_border)

        for((i, answer) in answers.withIndex()) {
            val textContainer = LinearLayout(requireContext())
            val textView = TextView(requireContext())
            val containerParams = LinearLayout.LayoutParams(0, elementsHeight)
            val textParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT)

            containerParams.weight = 1F
            textParams.weight = 1F

            textContainer.layoutParams = containerParams
            textContainer.orientation = LinearLayout.HORIZONTAL
            textContainer.gravity = Gravity.CENTER
            textContainer.tag = i

            textView.layoutParams = textParams
            textView.text = answer.text
            textView.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.BOLD)
            textView.gravity = Gravity.CENTER
            textView.alpha = 0.5F
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)

            textContainer.addView(textView)

            if(i < answers.size - 1) {
                val separator = View(requireContext())
                val separatorParams = LayoutParams(separatorsWidth, LayoutParams.MATCH_PARENT)

                separator.layoutParams = separatorParams
                separator.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

                textContainer.addView(separator)
            }

            textContainer.setOnClickListener { view ->
                val index = view.tag.toString().toInt()
                val parent = view.parent as LinearLayout
                val grandParent = parent.parent as LinearLayout
                val sibling = grandParent.getChildAt(1) as ConstraintLayout
                val answersRequest: MutableList<SurveyAnswerRequest> = mutableListOf()
                val currentQuestion = questions[currentQuestion]
                val currentAnswer = currentQuestion.answers!![index]
                val answerRequest = SurveyAnswerRequest(currentAnswer.id, currentAnswer.text)
                val newQuestionsRequest: MutableList<SurveyQuestionRequest> = mutableListOf()

                answersRequest.add(answerRequest)

                for(question in questionsRequest) {
                    if(question.id != currentQuestion.id) {
                        newQuestionsRequest.add(question)
                    }
                }

                newQuestionsRequest.add(SurveyQuestionRequest(currentQuestion.id, answersRequest))

                questionsRequest = newQuestionsRequest

                for(j in 0 until parent.childCount) {
                    val item = parent.getChildAt(j) as LinearLayout
                    val itemChild = item.getChildAt(0) as TextView

                    if(j <= index) {
                        itemChild.alpha = 1F
                    } else {
                        itemChild.alpha = 0.5F
                    }
                }

                val firstSiblingText = sibling.getChildAt(0) as TextView
                val lastSiblingText = sibling.getChildAt(1) as TextView
                val middleScore = floor(answers.size.toDouble() / 2)

                if(index > middleScore) {
                    firstSiblingText.alpha = 0.5F
                    lastSiblingText.alpha = 1F
                } else {
                    firstSiblingText.alpha = 1F
                    lastSiblingText.alpha = 0.5F
                }
            }

            layoutContainer.addView(textContainer)
        }

        val constraintLayout = ConstraintLayout(requireContext())
        val notLikeText = TextView(requireContext())
        val likeText = TextView(requireContext())
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        val paramsConstraint = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val constraintTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, resources.displayMetrics).toInt()

        paramsConstraint.setMargins(0, constraintTop, 0 ,0)

        constraintLayout.layoutParams = paramsConstraint

        notLikeText.id = View.generateViewId()
        notLikeText.layoutParams = params
        notLikeText.alpha = 0.5F
        notLikeText.text = ContextCompat.getString(requireContext(), R.string.nps_not_likely)
        notLikeText.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.BOLD)
        notLikeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        notLikeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17F)

        likeText.id = View.generateViewId()
        likeText.layoutParams = params
        likeText.alpha = 0.5F
        likeText.text = ContextCompat.getString(requireContext(), R.string.nps_likely)
        likeText.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.BOLD)
        likeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        likeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17F)

        constraintLayout.addView(notLikeText)
        constraintLayout.addView(likeText)

        val constraintSet = ConstraintSet()

        constraintSet.clone(constraintLayout)

        constraintSet.connect(notLikeText.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        constraintSet.connect(notLikeText.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        constraintSet.connect(likeText.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        constraintSet.connect(likeText.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)

        constraintSet.applyTo(constraintLayout)

        linearLayout.addView(layoutContainer)
        linearLayout.addView(constraintLayout)

        return linearLayout
    }

    private fun dropdownForm(answers: List<Answer>): LinearLayout {
        val linearLayout = setLinearLayout(LinearLayout.VERTICAL)
        val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56F, resources.displayMetrics).toInt()
        val items: MutableList<String> = mutableListOf()
        val spinner = Spinner(requireContext())

        spinner.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        spinner.setBackgroundResource(R.drawable.button_style1)

        for(answer in answers) {
            items.add(answer.text)
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, items)

        adapter.setDropDownViewResource(R.layout.dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val answersRequest: MutableList<SurveyAnswerRequest> = mutableListOf()
                val currentQuestion = questions[currentQuestion]
                val currentAnswer = currentQuestion.answers!![position]
                val answerRequest = SurveyAnswerRequest(currentAnswer.id, currentAnswer.text)
                val newQuestionsRequest: MutableList<SurveyQuestionRequest> = mutableListOf()

                answersRequest.add(answerRequest)

                for(question in questionsRequest) {
                    if(question.id != currentQuestion.id) {
                        newQuestionsRequest.add(question)
                    }
                }

                newQuestionsRequest.add(SurveyQuestionRequest(currentQuestion.id, answersRequest))

                questionsRequest = newQuestionsRequest
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Doesn't exists data")
            }
        }

        linearLayout.addView(spinner)

        return linearLayout
    }

    private fun textForm(answers: List<Answer>): LinearLayout {
        val linearLayout = setLinearLayout(LinearLayout.VERTICAL)
        val textareaHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 168F, resources.displayMetrics).toInt()
        val textHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56F, resources.displayMetrics).toInt()
        val horizontalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20F, resources.displayMetrics).toInt()

        for(answer in answers) {
            val editText = EditText(requireContext())
            var height = textHeight
            var verticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0F, resources.displayMetrics).toInt()

            if(answer.type == "text") {
                height = textareaHeight
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                editText.gravity = Gravity.START or Gravity.TOP

                verticalPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20F, resources.displayMetrics).toInt()
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT
            }

            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)

            params.setMargins(0, horizontalPadding, 0, 0)

            editText.layoutParams = params
            editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_style1)
            editText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.white30))
            editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            editText.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            editText.typeface = Typeface.create("neuzeit_s_lt_std_book", Typeface.NORMAL)

            if(answer.placeholder != null) {
                editText.hint = answer.placeholder
            }

            linearLayout.addView(editText)
        }

        return linearLayout
    }

    private fun setLinearLayout(orientation: Int): LinearLayout {
        val linearLayout = LinearLayout(requireContext())
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100F, resources.displayMetrics).toInt()

        layoutParams.setMargins(0, topMargin, 0, 0)
        linearLayout.layoutParams = layoutParams
        linearLayout.orientation = orientation
        linearLayout.gravity = Gravity.CENTER

        return linearLayout
    }
}