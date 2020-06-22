package com.bangkit.pneumoniadetector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Login_activity extends AppCompatActivity {
    private EditText id_input, password_input;
    private Button login_button;
    private Button hint;
    private int counter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_button = (Button) findViewById(R.id.login_main);
        id_input = (EditText) findViewById(R.id.input_id);
        password_input = (EditText) findViewById(R.id.input_password);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation(id_input.getText().toString(),password_input.getText().toString());

            }
        });

        hint = (Button) findViewById(R.id.hint);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });


    }
    public void openDialog(){
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(),"example dialog");

    }

    private void validation(String user_id, String user_password){
        if((user_id.equals("Admin"))&&(user_password.equals("Admin"))){
            Intent login = new Intent(Login_activity.this, camera_get.class);
            startActivity(login);
        }
    }
}
