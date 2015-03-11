package com.teamtreehouse.ribbitfinal1;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NY on 3/10/2015.
 */
public class InboxFragment extends android.support.v4.app.ListFragment {
    public static final String TAG = InboxFragment.class.getSimpleName();

    protected List<ParseObject> mMessages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ParseQuery<ParseObject>query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                if(e == null){
                    //Success - we found messages
                    mMessages = messages;
                    int i = 0;
                    String[] usernames = new String[mMessages.size()];
                    for(ParseObject message : messages){
                        usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }
                    if( getListView().getAdapter() == null){
                        MessageAdapter adapter = new MessageAdapter(getListView().getContext(),
                                mMessages);
                        // ArrayAdapter<String>adapter = new ArrayAdapter<String>(getListView().getContext(),
                        //        android.R.layout.simple_list_item_1,
                        //        usernames);
                        setListAdapter(adapter);
                    }
                    else{
                        //refill the adapter!
                        ((MessageAdapter)getListView().getAdapter()).refill(mMessages);

                    }

                }

            }
        });


    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
        Uri fileUri = Uri.parse(file.getUrl());
        if(messageType.equals(ParseConstants.KEY_IMAGE_TYPE)){
            //view the image
            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        }
        else{
            //view the video
            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.setDataAndType(fileUri, "video/*");
            startActivity(intent);
        }

        //Delete it!

        List<String> ids = message.getList(ParseConstants.KEY_RECIPIENT_IDS);

        if(ids.size() == 1){
            //last recipient - delete the whole thing!
            message.deleteInBackground();

        }
        else{
            //remove the recipient and save
            ids.remove(ParseUser.getCurrentUser().getObjectId());

            ArrayList<String> idsToRemove = new ArrayList<String >();
            ids.add(ParseUser.getCurrentUser().getObjectId());

            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove);
            message.saveInBackground();


        }
    }
}
