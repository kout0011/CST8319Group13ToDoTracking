package cst8319.group13.todotracking;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;

public class UserHomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_page);

        // Get the username from the Intent
        String username = getIntent().getStringExtra("USERNAME");

        // Update the textViewTitle with the username
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText(username + "'s TO-DO");

        // Find the Logout button by its ID
        Button buttonLogout = findViewById(R.id.buttonLogout);

        // Set an OnClickListener on the Logout button
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a confirmation dialog before logging out
                new AlertDialog.Builder(UserHomePage.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() { // Change "OK" to "Yes"
                            public void onClick(DialogInterface dialog, int which) {
                                // Clear user data from SharedPreferences
                                clearUserData();

                                // Logout the user and navigate back to MainActivity
                                Intent intent = new Intent(UserHomePage.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish(); // Close the UserHomePage so that it is not in the back stack
                            }
                        })
                        .setNegativeButton("No", null) // Change "Cancel" to "No"
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void clearUserData() {
        // Clear the user data from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}