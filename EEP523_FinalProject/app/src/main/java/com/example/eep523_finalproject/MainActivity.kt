package com.example.eep523_finalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    NavigationView.OnNavigationItemSelectedListener {
    var bottomNavigationView: BottomNavigationView? = null

    var toolbar: Toolbar? = null
    var toggle: ActionBarDrawerToggle? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var drawerLayout: DrawerLayout? = findViewById(R.id.drawer)
        var navigationView: NavigationView? = findViewById(R.id.navigationView)
        bottomNavigationView =
            findViewById<View>(R.id.navigation) as BottomNavigationView
        bottomNavigationView!!.setOnNavigationItemSelectedListener(this)

        toolbar = findViewById(R.id.toolbar)
        //setSupportActionBar(toolbar)
        supportActionBar!!.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawerOpen,
            R.string.drawerClose
        )
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(toggle!!)
        };
        toggle!!.syncState()
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_today -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_daily -> {
                val intent = Intent(this, daily::class.java)
                startActivity(intent)
                return true
            }
        }

        return false
    }
}
