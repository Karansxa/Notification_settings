package com.theplasmadonation.jeewan.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.theplasmadonation.jeewan.MainScreenActivity;
import com.theplasmadonation.jeewan.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.theplasmadonation.jeewan.databinding.ActivityProfileFormBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileForm extends AppCompatActivity {
    String json;
    final String TAG = "MAin Activity";
    //set of all states
    Set<String> states;
    ActivityProfileFormBinding profileFormBinding;
    //list of all cities in a state
    HashMap<String, ArrayList<String>> cities;
    ProfileViewModel profileViewModel;
    String city;
    String state;
    String contact;
    ActionBar actionBar;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileFormBinding = ActivityProfileFormBinding.inflate(getLayoutInflater());
        setContentView(profileFormBinding.getRoot());
        actionBar = getSupportActionBar();
        actionBar.hide();



        //initialize both set and map with data
        init();

        progressBar=(ProgressBar)findViewById(R.id.profileform_bar);

        //populate the state adapter using states set and arrayadapter
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, new ArrayList(states));
        profileFormBinding.profileformStateSpinner.setAdapter(arrayAdapter);
        //set onclick listener on state spinner
        profileFormBinding.profileformStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //get selected state
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                state = selectedItem;
                //populate the city adapter using cities hashmap and arrayadapter
                ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(ProfileForm.this, R.layout.support_simple_spinner_dropdown_item, cities.get(selectedItem));
                profileFormBinding.profileformCitySpinner.setAdapter(arrayAdapter1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        //get selected city
        profileFormBinding.profileformCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                city = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        try {
            if (!(null == FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                contact = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }
        } catch (Exception e) {
        }

        try {
            if (!(null == FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                contact = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            }
        } catch (Exception e) {
        }

        //set contact in form
        profileFormBinding.profileformContactTv.setText(contact);



        //set listener on submit button
        profileFormBinding.profileformSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (profileFormBinding.profileformNameTv.getText().toString().isEmpty()) {
                    profileFormBinding.profileformNameTv.setError("Enter name");
                    return;
                }
                if (profileFormBinding.profileformAgeTv.getText().toString().isEmpty()) {
                    profileFormBinding.profileformAgeTv.setError("Enter Hospital name");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                profileViewModel = new ViewModelProvider(getViewModelStore(), new ProfileViewModelFactory(
                        profileFormBinding.profileformNameTv.getText().toString(),
                        profileFormBinding.profileformAgeTv.getText().toString(),
                        contact, city, state)).get(ProfileViewModel.class);
                profileViewModel.getDataPushed().observe(ProfileForm.this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(ProfileForm.this, MainScreenActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ProfileForm.this, "Please Try Again Later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    //method to create list of states and cities from json file
    @SuppressLint("WrongConstant")
    private void init() {
        states = new HashSet<>();
        cities = new HashMap<>();

        try {
            //create a json object by loading cities.json file
            JSONObject obj = new JSONObject(loadJson());
            JSONArray array = obj.getJSONArray("array");
            //get state name from each entries in file and add it in set
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                states.add(jsonObject.getString("state"));

                //add the city name in existing list if an entry with given state name key  exists
                if (cities.containsKey(jsonObject.getString("state"))) {
                    ArrayList<String> tempStrings = cities.get(jsonObject.getString("state"));
                    tempStrings.add(jsonObject.getString("name"));
                    cities.put(jsonObject.getString("state"), tempStrings);
                }
                //otherwise create a new list and add entry with state name as key
                else {
                    ArrayList<String> tempStrings = new ArrayList<>();
                    tempStrings.add(jsonObject.getString("name"));
                    cities.put(jsonObject.getString("state"), tempStrings);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //method to load data ofjson file in json object
    public String loadJson() {
        try {
            InputStream is = getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}




