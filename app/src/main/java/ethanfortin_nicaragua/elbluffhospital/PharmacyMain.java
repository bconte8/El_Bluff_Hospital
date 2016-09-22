package ethanfortin_nicaragua.elbluffhospital;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PharmacyMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phamarcy_main);
    }

    public void seeInventory(View v) {
        Intent seeInv = new Intent(this, SeeInventory.class);
        startActivity(seeInv);
    }

    public void addMedicine(View v) {
        Intent addMed = new Intent(this, AddMedicine.class);
        startActivity(addMed);
    }

    public void subMedicine(View v) {
        Intent subMed = new Intent(this, SubtractMedicine.class);
        startActivity(subMed);
    }
}


