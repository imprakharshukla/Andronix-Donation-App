package com.techriz.andronix.donation.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.CustomItemViewBinding
import com.techriz.andronix.donation.utils.ActionUtils.copyText

@SuppressLint("Recycle")
class CustomItemView(
    context: Context, attrs: AttributeSet
) : LinearLayout(context, attrs) {
    private var binding: CustomItemViewBinding =
        CustomItemViewBinding.inflate(LayoutInflater.from(context), this, true)

    var logoIv: ImageView
    var titleTv: TextView
    var descTv: TextView
    var cardLl: LinearLayout

    init {

        inflate(context, R.layout.custom_item_view, this)
        logoIv = binding.logo
        titleTv = binding.title
        descTv = binding.description
        cardLl = binding.termuxAbsentCard

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CustomItemView)


        val isChecked = attributes.getBoolean(R.styleable.CustomItemView_isSelected, false);

        if (isChecked) {
            cardLl.background =
                AppCompatResources.getDrawable(context, R.drawable.rounded_borders_orange)
        } else {
            cardLl.background =
                AppCompatResources.getDrawable(context, R.drawable.rounded_borders)
        }
        val maxLines = attributes.getInteger(R.styleable.CustomItemView_maxLinesForDesp, 4)

        descTv.maxLines = maxLines


        logoIv.setImageDrawable(attributes.getDrawable(R.styleable.CustomItemView_logo_image))


        titleTv.text = attributes.getString(R.styleable.CustomItemView_title_text)
        descTv.text = attributes.getString(R.styleable.CustomItemView_description_text)
    }
}