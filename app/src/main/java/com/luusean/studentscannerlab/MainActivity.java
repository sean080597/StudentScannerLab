package com.luusean.studentscannerlab;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luusean.studentscannerlab.database.DaoMaster;
import com.luusean.studentscannerlab.database.DaoSession;
import com.luusean.studentscannerlab.database.EventObject;
import com.luusean.studentscannerlab.database.EventObjectDao;
import com.luusean.studentscannerlab.database.EventStudentObject;
import com.luusean.studentscannerlab.database.EventStudentObjectDao;
import com.luusean.studentscannerlab.database.StudentObjectDao;
import com.luusean.studentscannerlab.event.EventAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //DAO --> Data Access Object
    private EventObjectDao eventObjectDao;//sql access object
    private EventObject eventObject;
    private StudentObjectDao studentObjectDao;
    private EventStudentObjectDao eventStudentObjectDao;

    private RecyclerView recyclerView;
    private TextView txtEmpty;

    private final int REQUEST_CODE = 1997;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mapping recyclerView
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        txtEmpty = findViewById(R.id.empty_view);

        //Initialise DAO
        eventObjectDao = initEventObjectDb();
        studentObjectDao = initStudentObjectDb();
        eventStudentObjectDao = initEventStudentDb();

        List<EventObject> ls_events = eventObjectDao.queryBuilder()
                .orderDesc(EventObjectDao.Properties.Id).build().list();
        if(ls_events.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);
        }else{
            recyclerView.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.GONE);

            //get list students offline --> get above
            EventAdapter adapter = new EventAdapter(this, ls_events );
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    //action for menu item - add new event
    public void onAddAction(MenuItem mi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Event");
        builder.setMessage("Enter your event name");

        final EditText edtEventName = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtEventName.setLayoutParams(lp);
        builder.setView(edtEventName);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_export_excel_black);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                //check if null input event name
                if(TextUtils.isEmpty(edtEventName.getText().toString())){
                    edtEventName.setError(getString(R.string.invalid_input));
                    edtEventName.requestFocus();
                }else{
                    wantToCloseDialog = true;
                }
                //dismiss dialog
                if(wantToCloseDialog){
                    //save to Table Event DB & reload recyclerView
                    eventObject = new EventObject(null, edtEventName.getText().toString(), null);
                    eventObjectDao.insert(eventObject);

                    //dismiss dialog
                    alertDialog.dismiss();

                    //move to ScannerActivity
                    Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
    }
    //method to reload list events
    private void reloadListEvents(){
        //reload list events
        List<EventObject> ls_es = eventObjectDao.queryBuilder().orderDesc(EventObjectDao.Properties.Id).build().list();
        EventAdapter adapter = new EventAdapter(MainActivity.this, ls_es);
        recyclerView.setAdapter(adapter);
        //check if ls_es null to show/hide recyclerView
        if(!ls_es.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.GONE);
        }
    }

    //reload list events when back from ScannerActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            reloadListEvents();
        }
    }

    //initiate EventObject DB
    private EventObjectDao initEventObjectDb() {
        //create db file if not exist
        String DB_NAME = "event_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getEventObjectDao();
    }

    //initiate Student DB
    private StudentObjectDao initStudentObjectDb() {
        //create db file if not exist
        String DB_NAME = "student_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getStudentObjectDao();
    }

    //initiate EventStudentObject DB
    private EventStudentObjectDao initEventStudentDb() {
        //create db file if not exist
        String DB_NAME = "event_student_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getEventStudentObjectDao();
    }

    //method delete event to use in adapter
    public void deleteEvent(final EventObject eventObject){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Delete event");
        builder.setMessage(R.string.do_u_wan_del_event);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get list of students of event id
                List<EventStudentObject> ls_es = eventStudentObjectDao.queryBuilder()
                        .where(EventStudentObjectDao.Properties.Event_id.eq(eventObject.getId()))
                        .list();
                //delete all of list
                eventStudentObjectDao.deleteInTx(ls_es);
                //delete event
                eventObjectDao.delete(eventObject);
                reloadListEvents();
                //check if deleted all list events
                List<EventObject> ls_events = eventObjectDao.queryBuilder()
                        .orderDesc(EventObjectDao.Properties.Id).build().list();
                if(ls_events.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    txtEmpty.setVisibility(View.VISIBLE);
                }
                Toast.makeText(MainActivity.this, R.string.delete_successfully, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
