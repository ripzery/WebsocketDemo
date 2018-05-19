package me.ripzery.websocketdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import me.ripzery.websocketdemo.requestor.RequestorFragment

class MainActivity : AppCompatActivity() {
    private lateinit var mRequestorFragment: RequestorFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = viewPager as ViewPager
        viewPager.adapter = PagerAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

//        mRequestorFragment = RequestorFragment()

//        supportFragmentManager.beginTransaction().replace(R.id.layoutRoot, mRequestorFragment).commit()
    }
}
