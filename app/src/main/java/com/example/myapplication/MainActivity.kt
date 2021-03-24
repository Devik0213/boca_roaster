package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.helper.ToastHelper
import com.example.myapplication.room.DB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        historyAdapter = HistoryAdapter(layoutInflater) {
            ToastHelper.showSingleToast(this, "${it.timeId}", Toast.LENGTH_SHORT)
        }
        binding.recyclerView.adapter = historyAdapter

        lifecycleScope.launchWhenResumed {
            historyAdapter.setData(withContext(Dispatchers.IO) {
                DB.instance.roastInfoDao().getRecent()
            })
        }
    }


}