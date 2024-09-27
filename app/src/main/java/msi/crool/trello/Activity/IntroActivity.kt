package msi.crool.trello.Activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import msi.crool.trello.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {
    private var binding: ActivityIntroBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding?.apply {
            val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
            tvAppNameIntro.typeface = typeface

            btnSignInIntro.setOnClickListener {
                startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
            }

            btnSignUpIntro.setOnClickListener {
                startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
