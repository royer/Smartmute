package com.bangz.lib.ui.donebar;

import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public abstract class DoneBarActivity extends ActionBarActivity {

    private View viewDoneCancelView ;
    private View viewDoneBarView;

    private int oldDisplayOptions ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewDoneCancelView = createCustomDoneCancelView(
                donecancelViewLayoutId(),doneButtonId(),cancelButtonId());

        viewDoneBarView = createCustomDoneView(doneViewLayoutId(),doneButtonId());

        oldDisplayOptions = getSupportActionBar().getDisplayOptions() ;
    }

    /**
     * create a custom view include both "Done" and "Cancel" button
     * @param layout
     * @param doneId
     * @param cancelId
     * @return
     */
    protected View createCustomDoneCancelView(int layout, int doneId, int cancelId) {

        final LayoutInflater inflater = (LayoutInflater)getSupportActionBar().
                getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layout, null);
        view.findViewById(doneId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClicked();
            }
        });
        view.findViewById(cancelId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelButtonClicked();
            }
        });


        return view ;
    }

    /**
     * create only Done button bar
     */
    protected View createCustomDoneView(int layout, int doneId) {

        final LayoutInflater inflater = (LayoutInflater)getSupportActionBar().
                getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layout, null) ;
        view.findViewById(doneId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClicked();
            }
        });
        return view;

    }

    public void onDoneButtonClicked() {}
    public void onCancelButtonClicked() {}

    /**
     * Override this method client can provide himself two buttons layout.
     * @return The include two buttons view layout resource id.
     */
    protected int donecancelViewLayoutId() {
        return R.layout.actionbar_custom_view_done_cancel;
    }

    /**
     * Override this method client can provide himself only done button layout.
     * @return only Done button view layout resource id.
     */
    protected int doneViewLayoutId() {
        return R.layout.actionbar_custom_view_done;
    }

    /**
     * if client provide himself layout, he must override this mathod to provide the done button id.
     * @return the Done button id.
     */
    protected int doneButtonId() {
        return R.id.actionbar_done;
    }

    /**
     * if client provide himslef layout, he must override this method to provide the cancel
     * button id.
     * @return the Cancel button id.
     */
    protected int cancelButtonId() {
        return R.id.actionbar_cancel;
    }

    public void showDoneCancelBar(boolean bShow) {
        showBar(viewDoneCancelView, bShow);
    }
    public void showDoneBar(boolean bShow) {
        showBar(viewDoneBarView, bShow);
    }

    private void showBar(View view, boolean bShow) {

        final ActionBar actionBar = getSupportActionBar() ;
        if (bShow) {
            oldDisplayOptions = actionBar.getDisplayOptions();
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_HOME
                            |ActionBar.DISPLAY_SHOW_CUSTOM
                            |ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(view,
                    new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));

        } else {
            actionBar.setDisplayOptions(oldDisplayOptions);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_done_bar, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
