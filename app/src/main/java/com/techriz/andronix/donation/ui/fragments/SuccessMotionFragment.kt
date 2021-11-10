package studio.com.techriz.andronix.ui

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.FragmentSuccessMotionBinding

class SuccessMotionFragment : Fragment(R.layout.fragment_success_motion) {

    lateinit var binding: FragmentSuccessMotionBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSuccessMotionBinding.bind(view)

        binding.lottieSuccessAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }
        })
    }

}