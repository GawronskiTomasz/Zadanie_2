package com.example.zadanie2;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.zadanie2.tasks.DeleteDialog;
import com.example.zadanie2.tasks.TaskListContent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TaskFragment.OnListFragmentInteractionListener, DeleteDialog.OnDeleteDialogInteractionListener {
    private File storageDir;
    private String mCurrentPhotoPath;
    public static final String taskExtra = "taskExtra";
    private int currentItemPosition = -1;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;
    private TaskListContent.Task currentTask;
    private final String CURRENT_TASK_KEY = "CurrentTask";
    private final String TASKS_SHARED_PREFS = "TasksSharedPrefs";
    private final String NUM_TASKS = "NumOfTasks";
    private final String TASK = "task_";
    private final String DETAIL = "desc_";
    private final String DATA = "date_";
    private final String PIC = "pic_";
    private final String ID = "id_";
    public static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        if (savedInstanceState != null) {
            currentTask = savedInstanceState.getParcelable(CURRENT_TASK_KEY);
        }
        restoreTasksFromSharedPreferences();
    }

    private void startSecondActivity(TaskListContent.Task task, int position)
    {
        Intent intent =  new Intent ( this, TaskInfoActivity.class );
        intent.putExtra ( taskExtra, ( Parcelable ) task );
        startActivity ( intent );
    }

    @Override
    public void OnDeleteClick(int position) {
        showDeleteDialog ();
        currentItemPosition=position;
    }

    @Override
    public void onListFragmentClickInteraction(TaskListContent.Task task, int position)
    {
        currentTask = task;
            //Toast.makeText ( this, getString ( R.string.nazwa ) + position, Toast.LENGTH_SHORT ).show ();
            if (getResources ().getConfiguration ().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                displayTaskInFragment ( task );
            } else {
                startSecondActivity ( task, position );
            }
    }

    private void displayTaskInFragment(TaskListContent.Task task)
    {
        TaskInfoFragment taskInfoFragment = ((TaskInfoFragment) getSupportFragmentManager ().findFragmentById ( R.id.displayFragment ));
        if(taskInfoFragment != null)
        {
            taskInfoFragment.displayTask ( task );
        }
    }
    private  void showDeleteDialog()
    {

        DeleteDialog.newInstance().show(getSupportFragmentManager (), getString ( R.string.hello_blank_fragment ));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if(currentItemPosition != -1 && currentItemPosition < TaskListContent.ITEMS.size ())
        {
            TaskListContent.removeItem(currentItemPosition);
            ((TaskFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.taskFragment))).notifyDataChange ();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        View v =findViewById ( R.id.floatingActionButton );
            if(v != null)
            {
            Snackbar.make(v, getString(R.string.delete_cancel_msg), Snackbar.LENGTH_LONG).setAction ( getString ( R.string.retray_msg ), new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    showDeleteDialog ();
                }
            } ).show ();
        }
    }

    public void addNextContact(View view) {
        //FloatingActionButton floatingActionButton = findViewById ( R.id.floatingActionButton );
        Intent intent = new Intent ( getApplicationContext (), AddContact.class );
        startActivityForResult ( intent, SECOND_ACTIVITY_REQUEST_CODE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult ( requestCode, resultCode, data );
        ((TaskFragment) getSupportFragmentManager ().findFragmentById ( R.id.taskFragment )).notifyDataChange ();
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            Intent intent = new Intent(getApplicationContext(),AddContact.class);
            intent.putExtra("photo", mCurrentPhotoPath);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(currentTask != null)
            outState.putParcelable ( CURRENT_TASK_KEY, currentTask );
        super.onSaveInstanceState ( outState );
    }

    @Override
    protected void onResume()
    {
        super.onResume ();
        ((TaskFragment) getSupportFragmentManager ().findFragmentById ( R.id.taskFragment )).notifyDataChange ();
        if(getResources ().getConfiguration ().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            if(currentTask != null)
            {
                displayTaskInFragment ( currentTask );
            }
        }
    }

    private void saveTaskToSharedPreferences(){
        SharedPreferences tasks = getSharedPreferences(TASKS_SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = tasks.edit();

        editor.clear();

        editor.putInt(NUM_TASKS, TaskListContent.ITEMS.size());
        for(int i = 0; i < TaskListContent.ITEMS.size(); i++){
            TaskListContent.Task task = TaskListContent.ITEMS.get(i);
            editor.putString(TASK + i, task.title);
            editor.putString(DETAIL + i, task.details);
            editor.putString(DATA + i, task.data);
            editor.putString(PIC + i, task.picPath);
            editor.putString(ID + i, task.id);
        }
        editor.apply();
        editor.commit();
    }

    private void restoreTasksFromSharedPreferences(){
        SharedPreferences tasks = getSharedPreferences(TASKS_SHARED_PREFS, MODE_PRIVATE);
        int numOfTasks = tasks.getInt(NUM_TASKS,0);
        if(numOfTasks != 0){
            TaskListContent.clearList();

            for(int i = 0; i < numOfTasks; i++){
                String id = tasks.getString(ID + i, "0");
                String title = tasks.getString(TASK + i, "0");
                String detail = tasks.getString(DETAIL + i, "0");
                String date = tasks.getString(DATA + i, "0");
                String picPath = tasks.getString(PIC + i, "0");

                TaskListContent.addItem(new TaskListContent.Task(id, title, detail, date, picPath));
            }
        }
    }

    @Override
    public void onDestroy()
    {
        saveTaskToSharedPreferences();
        super.onDestroy();
    }

    public void addContactThroughPhoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(getApplicationContext(),
                        getString(R.string.myFileprovider),
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "photo" + timeStamp + "_";
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
