package com.example.albud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.albud.databinding.ActivityIngredientsBinding

class IngredientsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityIngredientsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIngredientsBinding.inflate(layoutInflater)
        val view =binding.root
        setContentView(view)

        val bundle: Bundle? = intent.extras

        val myArray: ArrayList<String>? = intent.getStringArrayListExtra("Ingredients")

        binding.test1.text= myArray!!.get(0)

    }
}