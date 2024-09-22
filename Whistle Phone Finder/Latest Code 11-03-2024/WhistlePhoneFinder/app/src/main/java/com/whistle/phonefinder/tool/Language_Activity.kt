package com.whistle.phonefinder.tool

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.whistle.phonefinder.tool.databinding.ActivityLanguageBinding
import com.whistle.phonefinder.tool.whistle.LanguageModel
import com.whistle.phonefinder.tool.whistle.LanguagesAdapter

class Language_Activity : AppCompatActivity() {
    lateinit var binding: ActivityLanguageBinding
    private var langList: ArrayList<LanguageModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.statusbar_color)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        binding.back.setOnClickListener {
            val intent = Intent(this@Language_Activity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val gridLayoutManager = GridLayoutManager(this@Language_Activity, 1)
        gridLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rvLanguage.layoutManager = gridLayoutManager
        bindLanguages()
        val adapter = LanguagesAdapter(langList)

        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }

        })

        if (currentAppLanguage(this)=="Arabic")
        {
            binding.back.setBackgroundResource(R.drawable.back_ar)

        }
        else  if (currentAppLanguage(this)=="Persian")
        {
            binding.back.setBackgroundResource(R.drawable.back_ar)
        }
        else
        {
            binding.back.setBackgroundResource(R.drawable.back)

        }
        adapter.notifyDataSetChanged()
        binding.rvLanguage.adapter = adapter


    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LanguageChanged.wrapContext(base!!))
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@Language_Activity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun bindLanguages() {
        langList.clear()
        langList.add(LanguageModel("1", "English", R.drawable.eng_flag))
        langList.add(LanguageModel("2", "Arabic", R.drawable.ar_flag))
        langList.add(LanguageModel("3", "French", R.drawable.french_flag))
        langList.add(LanguageModel("4", "Italian", R.drawable.italian_flag))
        langList.add(LanguageModel("5", "Spanish", R.drawable.spanish_flag))
        langList.add(LanguageModel("6", "Portuguese", R.drawable.portguess_flag))
        langList.add(LanguageModel("7", "German", R.drawable.german_flag))
        langList.add(LanguageModel("8", "Indonesian", R.drawable.indo_flag))
        langList.add(LanguageModel("9", "Russian", R.drawable.russian_flag))
        langList.add(LanguageModel("10", "Malaysian", R.drawable.malay_flag))
        langList.add(LanguageModel("11", "Chinese", R.drawable.chinees_flag))
        langList.add(LanguageModel("12", "Persian", R.drawable.iran_flag))
        langList.add(LanguageModel("13", "Turkish", R.drawable.turkish_flag))
        langList.add(LanguageModel("14", "Thai", R.drawable.thai_flag))
        langList.add(LanguageModel("15", "Vietnamese", R.drawable.vitanmese_flag))
        langList.add(LanguageModel("16", "Polish", R.drawable.polish_flag))
        langList.add(LanguageModel("17", "Greek", R.drawable.greek_flag))
        langList.add(LanguageModel("18", "Czech", R.drawable.czech_flag))
        langList.add(LanguageModel("19", "Swedish", R.drawable.swedish_flag))
        langList.add(LanguageModel("20", "Hungarian", R.drawable.hungry_flag))
        langList.add(LanguageModel("21", "Norwegian", R.drawable.norway_flag))
        langList.add(LanguageModel("22", "Japanese", R.drawable.japanees_flag))
        langList.add(LanguageModel("23", "Hindi", R.drawable.hinid_flag))
        langList.add(LanguageModel("24", "Danish", R.drawable.danish_flag))
        langList.add(LanguageModel("25", "Korean", R.drawable.korean_flag))
        langList.add(LanguageModel("26", "Finish", R.drawable.finish_flag))
    }

}