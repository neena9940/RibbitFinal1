package com.teamtreehouse.ribbitfinal1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected Uri mMediaUri;

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10;//10 MB






    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case 0://Take Photo
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputFileUri(MEDIA_TYPE_IMAGE);
                    if(mMediaUri == null){
                        //display error
                        Toast.makeText(MainActivity.this,
                                getString(R.string.error_external_storage),
                                Toast.LENGTH_LONG).show();
                    }
                    else{
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);

                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }

                    return;
                case 1://Take Video
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputFileUri(MEDIA_TYPE_VIDEO);
                    if(mMediaUri == null){
                        //display error
                        Toast.makeText(MainActivity.this,
                                getString(R.string.error_external_storage),
                                Toast.LENGTH_LONG).show();
                    }
                    else{
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                        startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                    }
                    return;
                case 2://Pick Photo
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                    return;
                case 3://Pick Video
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    Toast.makeText(MainActivity.this,
                            getString(R.string.video_file_size_warning),
                            Toast.LENGTH_LONG).show();
                    startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
                    return;
            }
        }

        private Uri getOutputFileUri(int mediaType) {
            if(isExternalStorageAvailable()){
                //Success -- Get the Uri
                //1. Get the Directory
                String appName = MainActivity.this.getString(R.string.app_name);
                File mediaStorageDir =
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);
                //2. Create subdirectory
                if(!mediaStorageDir.exists()){
                    if(!mediaStorageDir.mkdirs()){
                        Log.e(TAG, "Failed to create directory..");

                        return null;
                    }
                }
                //3. Create file name
                //4. Create file
                String path = mediaStorageDir.getPath()+ File.separator;
                Date now = new Date();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);
                File mediaFile = null;
                if(mediaType == MEDIA_TYPE_IMAGE){
                    mediaFile = new File(path + "IMG_"+ timeStamp + ".jpg");
                }
                else if(mediaType == MEDIA_TYPE_VIDEO){
                    mediaFile = new File(path + "VID_"+ timeStamp +".mp4");
                }

                Log.d(TAG, "File: "+ Uri.fromFile(mediaFile));
                //5. Return Uri
                return Uri.fromFile(mediaFile);


            }
            else{
                    return null;
            }

        }

        private boolean isExternalStorageAvailable() {
            String state = Environment.getExternalStorageState();
            boolean isAvailable = false;
            if(state.equals(Environment.MEDIA_MOUNTED)){
                isAvailable = true;

            }
            return isAvailable;
        }
    };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseUser mCurrentUser = ParseUser.getCurrentUser();
        if(mCurrentUser == null){
            navigateToLogin();
        }
        else{
            Log.d(TAG, mCurrentUser.getUsername());
        }



        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.action_edit_friends:
                Intent intent = new Intent(MainActivity.this, EditFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST){
                if(data == null){
                    Toast.makeText(MainActivity.this, getString(R.string.general_error),
                            Toast.LENGTH_LONG).show();
                }
                else{
                    mMediaUri = data.getData();
                }
                if(requestCode == PICK_VIDEO_REQUEST){
                    //make sure file size is less than 10 MB
                    int fileSize = 0;
                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(MainActivity.this, getString(R.string.error_opening_file),
                                Toast.LENGTH_LONG).show();
                        return;

                    }
                    catch (IOException e) {
                        Toast.makeText(MainActivity.this, getString(R.string.error_opening_file),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {/* Intentionally Blank */}

                    }
                    if(fileSize >= FILE_SIZE_LIMIT){
                        Toast.makeText(MainActivity.this, getString(R.string.error_video_file_size_too_large),
                                Toast.LENGTH_LONG).show();
                        return;

                    }

                }

            }
            else{
                // add it to the Gallery
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            Intent recipientsIntent = new Intent(MainActivity.this, RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);
            if(requestCode == TAKE_PHOTO_REQUEST || requestCode == PICK_PHOTO_REQUEST){
               recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, ParseConstants.KEY_IMAGE_TYPE );
            }
            else{
                recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, ParseConstants.KEY_VIDEO_TYPE);
            }
            startActivity(recipientsIntent);



        }
        else if(resultCode != RESULT_CANCELED){
            Toast.makeText(MainActivity.this,
                    getString(R.string.general_error),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }



}
