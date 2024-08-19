package cst8319.group13.todotracking;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.Pattern;

public class RegistrationPage extends AppCompatActivity {

    private EditText editTextUserName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private CheckBox checkBoxNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        // Find the UI elements by their IDs
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextUserName = findViewById(R.id.editTextEnterUser);
        checkBoxNotifications = findViewById(R.id.checkBoxNotifications);

        // Find the Register button and Cancel button
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonCancelRegistration = findViewById(R.id.buttonCancelRegistration);

        // Set the click event for the Register button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder errorMessage = new StringBuilder();

                // Validate User Name, Email, and Password
                if (!validateUserName()) {
                    errorMessage.append("User name cannot be empty.\n");
                }
                if (!validateEmail()) {
                    errorMessage.append("Invalid Email format.\n");
                }
                if (!validatePassword()) {
                    errorMessage.append("Password must be at least 6 characters long and contain both letters and numbers.\n");
                }

                if (errorMessage.length() > 0) {
                    // Display all error messages at once
                    Toast.makeText(RegistrationPage.this, errorMessage.toString(), Toast.LENGTH_LONG).show();
                } else {
                    // If validation passed, insert the new user into the database
                    User newUser = new User(
                            editTextUserName.getText().toString().trim(),
                            editTextEmail.getText().toString().trim(),
                            editTextPassword.getText().toString().trim()
                    );

                    // Pass the context and database instance to the AsyncTask
                    new InsertUserAsyncTask(ToDoDatabase.getInstance(RegistrationPage.this), RegistrationPage.this).execute(newUser);
                }
            }
        });

        // Set the click event for the Cancel button, which when clicked returns to the MainActivity
        buttonCancelRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationPage.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Long> {
        private ToDoDatabase db;
        private Context context;

        InsertUserAsyncTask(ToDoDatabase db, Context context) {
            this.db = db;
            this.context = context;
        }

        @Override
        protected Long doInBackground(User... users) {
            return db.userDao().insertUser(users[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            if (result != -1) {
                // Assuming the registration is successful, go back to MainActivity
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to validate the User Name
    private boolean validateUserName() {
        String userName = editTextUserName.getText().toString().trim();
        return !userName.isEmpty();
    }

    // Method to validate the Email format
    private boolean validateEmail() {
        String email = editTextEmail.getText().toString().trim();
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Method to validate the Password format
    private boolean validatePassword() {
        String password = editTextPassword.getText().toString().trim();
        return !password.isEmpty() && password.length() >= 6 && Pattern.compile("[A-Za-z]").matcher(password).find() && Pattern.compile("[0-9]").matcher(password).find();
    }
}