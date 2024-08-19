package cst8319.group13.todotracking;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the user is already logged in
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (preferences.contains("email")) {
            // User is already logged in, navigate to UserHomePage
            String username = preferences.getString("username", "User");
            navigateToHomePage(username);
        }

        // 自动执行无害查询以确保数据库已创建
        new CheckDatabaseTask().execute();

        // Find the UI elements
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        // Find the Register button by its ID
        Button buttonRegister = findViewById(R.id.buttonRegister);
        // Find the Login button by its ID
        Button buttonLogin = findViewById(R.id.buttonLogin);

        // Set an OnClickListener on the Register button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start RegistrationPage
                Intent intent = new Intent(MainActivity.this, RegistrationPage.class);
                startActivity(intent);
            }
        });

        // Set an OnClickListener on the Login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate the user's login information
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Perform validation through AsyncTask
                new ValidateLoginAsyncTask().execute(email, password);
            }
        });
    }

    // 无害查询任务以确保数据库已创建
    private class CheckDatabaseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            ToDoDatabase db = ToDoDatabase.getInstance(MainActivity.this);
            // 执行一个简单的查询，比如查询用户数量
            int userCount = db.userDao().getAllUsers().size();
            return null;
        }
    }

    // Define ValidateLoginAsyncTask as a non-static inner class
    private class ValidateLoginAsyncTask extends AsyncTask<String, Void, User> {

        @Override
        protected User doInBackground(String... strings) {
            String email = strings[0];
            String password = strings[1];
            // Fetch user from the database using email
            ToDoDatabase db = ToDoDatabase.getInstance(MainActivity.this);
            User user = db.userDao().getUserByEmail(email);
            // Check if the user exists and if the password matches
            if (user != null && user.password.equals(password)) {
                return user;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                // Save user data to SharedPreferences
                saveUserData(user);

                // Display welcome message
                Toast.makeText(MainActivity.this, "Welcome to your To-Do Tracking!", Toast.LENGTH_SHORT).show();

                // If valid, navigate to UserHomePage and pass the username
                navigateToHomePage(user.userName);
            } else {
                // Show invalid email or password error
                Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUserData(User user) {
        // Save the user data to SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", user.email);
        editor.putString("username", user.userName);
        editor.apply();
    }

    private void navigateToHomePage(String username) {
        Intent intent = new Intent(MainActivity.this, UserHomePage.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish(); // Close the MainActivity so that the user can't navigate back to it
    }
}