package mindcar.testing.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import mindcar.testing.R;
import mindcar.testing.util.DatabaseAccess;
import mindcar.testing.util.UserData;

import static mindcar.testing.util.UserData.*;

/**
 * Created by Nikolaos-Machairiotis Sasopoulos on 12/05/2016.
 */
public class UserSettings  extends Activity implements View.OnClickListener {

    EditText changeUsername;
    EditText changeUsernameConf;
    EditText changePassword;
    EditText changePassordConf;

    Button usernameOK;
    Button passwordOK;

    //UserData newCredentials = new UserData();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_settings);

        changeUsername = (EditText) findViewById(R.id.changeUsername);
        changePassword = (EditText) findViewById(R.id.changePassword);
        changeUsernameConf = (EditText) findViewById(R.id.changeUsernameConf);
        changePassordConf = (EditText) findViewById(R.id.changePasswordConf);

        usernameOK = (Button) findViewById(R.id.usernameOK);
        passwordOK = (Button) findViewById(R.id.passwordOK);

        usernameOK.setOnClickListener(this);
        passwordOK.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        String Username = changeUsername.getText().toString();
        String UsernameConf = changeUsernameConf.getText().toString();
        //newCredentials.setUsername(Username);
        DatabaseAccess databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        switch (v.getId()) {
            case R.id.usernameOK:

                if (databaseAccess.checkAvailability(Username) && Username.equals(UsernameConf)){

                    databaseAccess.updateUsername(StartActivity.ET_USER_NAME.getText().toString(), Username);
                    Toast.makeText(getApplicationContext(), "Username successfully changed to " + "" + Username, Toast.LENGTH_SHORT).show();
                    //databaseAccess.close();
                    finish();
                    startActivity(new Intent(this, UserActivity.class));
                }

                else

                    Toast.makeText(getApplicationContext(), "Wrong credentials", Toast.LENGTH_SHORT).show();

               break;

            case R.id.passwordOK:

                String Password = changePassword.getText().toString();
                String PasswordConf = changePassordConf.getText().toString();
                //newCredentials.setPassword(Password);

                if (Password.equals(PasswordConf) && Password!=Username)
                    databaseAccess.updatePassword(StartActivity.ET_PASS.getText().toString(),Password);

                Toast.makeText(getApplicationContext(), "Password successfully changed to" + Password, Toast.LENGTH_LONG).show();

                break;
                }
        }
    }

