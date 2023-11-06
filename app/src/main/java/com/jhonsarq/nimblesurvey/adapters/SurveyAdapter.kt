package com.jhonsarq.nimblesurvey.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jhonsarq.nimblesurvey.R
import com.jhonsarq.nimblesurvey.databinding.SurveyBinding
import com.jhonsarq.nimblesurvey.model.Survey
import com.squareup.picasso.Picasso

class SurveyAdapter(private val surveys: List<Survey>, private val onClickListener: (String) -> Unit): RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return SurveyViewHolder(layoutInflater.inflate(R.layout.survey, parent, false))
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        val survey = surveys[position]

        Picasso.get().load(survey.coverImageUrl).into(holder.binding.image)

        holder.binding.title.text = survey.title
        holder.binding.description.text = survey.description

        holder.binding.surveyDetail.setOnClickListener {
            onClickListener(survey.id)
        }
    }

    override fun getItemCount(): Int = surveys.size

    inner class SurveyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = SurveyBinding.bind(view)
    }
}