package com.example.albud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UserAllergyForm extends AppCompatActivity {

    Button save;
    ArrayList<String> addArray = new ArrayList<String>();
    HashSet<String> test2 = new HashSet<String>();
    EditText inp;
    ListView show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_allergy_form);

        inp = (EditText) findViewById(R.id.AlergyInput);
        show= (ListView) findViewById(R.id.myList);
        save = (Button) findViewById(R.id.SaveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String gt = inp.getText().toString();
               if(!test2.contains(gt))
               {
                   addArray.add(gt);
                   test2.add(gt);
                   ArrayAdapter<String> adapter = new ArrayAdapter<String>(UserAllergyForm.this, android.R.layout.simple_list_item_1,addArray);
                   show.setAdapter(adapter);
                   ((EditText)findViewById(R.id.AlergyInput)).setText("");
               }
               else if(gt == null || gt.trim().equals("") )
               {
                   Toast.makeText(getBaseContext(),"Input Field is Empty",Toast.LENGTH_LONG).show();
               }
               else
               {
                   Toast.makeText(getBaseContext(),"Some Error Occured",Toast.LENGTH_LONG).show();
               }
            }
        });
    }

    public void finalPhase(View view) {
        Intent intent =  new Intent(UserAllergyForm.this,MainActivity.class);

        //where arrSongs is object of  ArrayList<Songs>

        intent.putExtra("alllergy1",addArray);
        startActivity(intent);
    }
}