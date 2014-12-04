package bangz.smartmute;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;

import bangz.smartmute.content.RulesColumns;

/**
 * A placeholder fragment containing a simple view.
 */
public class RulelistFragment extends ListFragment
    implements LoaderManager.LoaderCallbacks {

    private static final String TAG = "RulelistFragment";
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private SimpleCursorAdapter mAdapter ;
    private static final String[] PROJECTION = {RulesColumns._ID,
            RulesColumns.NAME,
            RulesColumns.ACTIVATED,
            RulesColumns.RULETYPE,
            RulesColumns.CONDITION,
            RulesColumns.SECONDCONDITION,
            RulesColumns.RINGMODE,
            RulesColumns.DESCRIPTION
    };

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static RulelistFragment newInstance(int sectionNumber) {
        RulelistFragment fragment = new RulelistFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RulelistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize CursorAdapter
        String[] columns = {
                RulesColumns.RULETYPE,
                RulesColumns.NAME,
                RulesColumns.DESCRIPTION,
                RulesColumns.ACTIVATED
        };
        int [] listitemids = {R.id.RuleIcon,R.id.RuleName,R.id.Detail,R.id.RuleOnOff};

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.rulelist_item,null,columns,listitemids,0
        ) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                int idxName = cursor.getColumnIndex(RulesColumns.NAME);
                int idxRuleType = cursor.getColumnIndex(RulesColumns.RULETYPE);
                int idxCondition = cursor.getColumnIndex(RulesColumns.CONDITION);
                int idxSecondCondition = cursor.getColumnIndex(RulesColumns.SECONDCONDITION);
                int idxActivited = cursor.getColumnIndex(RulesColumns.ACTIVATED);
                int idxDescrip = cursor.getColumnIndex(RulesColumns.DESCRIPTION);

                String name = cursor.getString(idxName);
                TextView v = (TextView)view.findViewById(R.id.RuleName);
                v.setText(name);

                int[] ruletypeiconids = {
                        0,
                        R.drawable.ic_location,
                        R.drawable.ic_wifi,
                        R.drawable.ic_clock};
                int ruletype = cursor.getInt(idxRuleType);
                Log.d(TAG, "RULETYPE = "+ruletype);
                ImageView imgv = (ImageView)view.findViewById(R.id.RuleIcon);
                imgv.setImageResource(ruletypeiconids[ruletype]);

                v = (TextView)view.findViewById(R.id.Detail);
                String descript = cursor.getString(idxDescrip);
                if(ruletype == RulesColumns.RT_TIME || ruletype == RulesColumns.RT_WIFI) {
                    v.setText(descript);
                } else {
                    if (descript.isEmpty() == false) {
                        v.setText(descript);
                    } else {
                        //TODO convert condition string to easy read text
                        String strcondition = cursor.getString(idxCondition);
                        v.setText(strcondition);
                    }
                }

                Switch ruleonoff = (Switch)view.findViewById(R.id.RuleOnOff);
                int activeted = cursor.getInt(idxActivited);
                ruleonoff.setChecked(activeted != 0);
            }

        };

        setListAdapter(mAdapter);
        LoaderManager lm = getLoaderManager();
        lm.initLoader(1, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rulelist, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((RulelistActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String selection = new String();
        return new CursorLoader(getActivity(),RulesColumns.CONTENT_URI,PROJECTION, selection, null,null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mAdapter.swapCursor((Cursor)data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }



}
