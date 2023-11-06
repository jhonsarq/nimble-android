package com.jhonsarq.nimblesurvey

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.jhonsarq.nimblesurvey.databinding.ActivityMainBinding
import com.jhonsarq.nimblesurvey.databinding.MenuHeaderBinding
import com.jhonsarq.nimblesurvey.model.LogoutRequest
import com.jhonsarq.nimblesurvey.utilities.Constants
import com.jhonsarq.nimblesurvey.utilities.Database
import com.jhonsarq.nimblesurvey.utilities.DbClient
import com.jhonsarq.nimblesurvey.utilities.Utils
import com.jhonsarq.nimblesurvey.view.LoaderFragment
import com.jhonsarq.nimblesurvey.viewmodel.LoaderViewModel
import com.jhonsarq.nimblesurvey.viewmodel.SurveysViewModel
import com.jhonsarq.nimblesurvey.viewmodel.UserViewModel
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val userViewModel: UserViewModel by viewModels()
    private val loaderViewModel: LoaderViewModel by viewModels()
    private val surveysViewModel: SurveysViewModel by viewModels()
    private val loaderFragment = LoaderFragment()
    private val utils = Utils()
    private val constants = Constants()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_NimbleSurvey)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawer = binding.drawer
        val header = binding.header
        val date = binding.date
        val menuHeaderBinding = MenuHeaderBinding.bind(binding.menuView.getHeaderView(0))
        val logoutButton = binding.menuView.menu.findItem(R.id.logout)
        val navHostFragment: NavHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment

        navHostFragment.findNavController().setGraph(R.navigation.splashscreen_navigation)

        userViewModel.getLocalProfile(this)

        userViewModel.user.observe(this) { user ->
            if(user != null) {
                if(user.name != null) {
                    navHostFragment.findNavController().setGraph(R.navigation.logged_navigation)
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                    header.visibility = View.VISIBLE
                    menuHeaderBinding.name.text = user.name

                    Picasso.get().load(user.avatarUrl).into(menuHeaderBinding.avatar)
                    Picasso.get().load(user.avatarUrl).into(binding.avatar)

                    date.text = utils.getCurrentDateFormatted()
                } else {
                    navHostFragment.findNavController().setGraph(R.navigation.unlogged_navigation)
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                    header.visibility = View.GONE
                }
            }
        }

        loaderViewModel.loader.observe(this) { loader ->
            if(loader != null && loader && !loaderFragment.isVisible) {
                loaderFragment.isCancelable = false
                loaderFragment.show(supportFragmentManager, "loader_fragment")
            }
        }

        binding.avatar.setOnClickListener {
            drawer.openDrawer(GravityCompat.END)
        }

        logoutButton.setOnMenuItemClickListener {
            val user = userViewModel.user.value
            val token = user!!.accessToken!!
            val parameters = LogoutRequest(token, constants.clientId, constants.clientSecret)

            userViewModel.logout(this, parameters)
            surveysViewModel.cleanSurveys()

            return@setOnMenuItemClickListener true
        }
    }
}