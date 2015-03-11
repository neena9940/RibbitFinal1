package com.teamtreehouse.ribbitfinal1;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;


public class RecipientsActivity extends ListActivity {
    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected List<ParseUser> mFriends;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;

    protected MenuItem mSendMenuItem;
    protected Uri mMediaUrl;
    protected String mMediaType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_recipients);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mMediaUrl = getIntent().getData();
       // mMediaType = getIntent().getStringExtra(ParseConstants.KEY_FILE_TYPE);
        mMediaType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);



        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setProgressBarIndeterminateVisibility(true);
        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if(e == null){
                    //success
                    mFriends = friends;
                    int i = 0;
                    String[] usernames = new String[mFriends.size()];
                    for(ParseUser friend : friends){
                        usernames[i] = friend.getUsername();
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(RecipientsActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    setListAdapter(adapter);
                }
                else{
                    Log.e(TAG, e.getMessage());
                }

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_send:
                ParseObject message = createMessage();
                if(message == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.error_selecting_file))
                            .setTitle(getString(R.string.error_selecting_file_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    send(message);
                    finish();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    //success
                    Toast.makeText(RecipientsActivity.this, getString(R.string.message_sent_label),
                            Toast.LENGTH_LONG).show();

                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this)
                            .setMessage(getString(R.string.error_sending_file))
                            .setTitle(getString(R.string.error_selecting_file_title))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });


    }

    private ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);

        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientsIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mMediaType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(RecipientsActivity.this, mMediaUrl);
        if(fileBytes == null){
            return null;
        }
        else{
            if(mMediaType.equals(ParseConstants.KEY_IMAGE_TYPE)){
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }
            String filename = FileHelper.getFileName(RecipientsActivity.this, mMediaUrl, mMediaType);

            ParseFile file = new ParseFile(filename, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);


        }


        return message;

    }

    private ArrayList<String> getRecipientsIds() {
       ArrayList<String> recipientIds = new ArrayList<String>();
        for(int i=0; i<getListView().getCount();i++){
            if(getListView().isItemChecked(i)){
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if(l.getCheckedItemCount()>0){
            mSendMenuItem.setVisible(true);
        }
        else{
            mSendMenuItem.setVisible(false);
        }
    }
}
