package app.cloudcrm.tech.cloudcrm.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.airbrake.AirbrakeNotifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.TouchImageView;

public class GalleryActivity extends AppCompatActivity {


    String[] files;

    String defaultFile;

    static String currentFile;

    static TextView textView;

    static FloatingActionButton fab;

    public static ArrayList<String> filesList;

    public static ArrayList<String> titleList;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    public void setTitle(CharSequence title) {

        getSupportActionBar().setTitle(title);

    }

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        textView = (TextView) findViewById(R.id.textView);

        files = getIntent().getStringArrayExtra("FILES");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("");

        defaultFile = getIntent().getStringExtra("DEFAULT_FILE");

        if(files != null) {

            filesList = new ArrayList<>(Arrays.asList(files));

        }else{

            filesList = new ArrayList<>();

            filesList.add(defaultFile);

        }

        titleList = new ArrayList<String>(Arrays.asList(getIntent().getStringArrayExtra("TITLES")));


        for(String fileName: filesList){

            //Log.d("GALLERY", "File:"+fileName);

        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);



        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {



            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                currentFile = filesList.get(position);

                getSupportActionBar().setTitle(titleList.get(position));

                textView.setText(String.valueOf(position+1)+"/"+filesList.size());

                PlaceholderFragment current = (PlaceholderFragment)mSectionsPagerAdapter.getItem(position);


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        int set = filesList.indexOf(defaultFile);

        //Log.d("GALLERY", "SetToPos:"+String.valueOf(set-1));

        mViewPager.setCurrentItem(0);

        currentFile = filesList.get(0);

        setTitle(titleList.get(0));

        textView.setText(String.valueOf(1)+"/"+filesList.size());

        if(set > 0) {

            mViewPager.setCurrentItem(set);

            currentFile = filesList.get(set);

            setTitle(titleList.get(set));

            textView.setText(String.valueOf(set+1)+"/"+filesList.size());

        }


        fab = (FloatingActionButton) findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent share = new Intent(Intent.ACTION_SEND);

                    share.setType("image/jpeg");

                    File f = new File(currentFile);

                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));

                    startActivity(share);

                }catch (Exception e) {
                    e.getStackTrace();
                    return;
                }


            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent it = new Intent(Intent.ACTION_VIEW);

            it.setDataAndType(Uri.fromFile(new File(currentFile)), "image/jpeg");

            //Log.d("GALLERY", "open: "+currentFile);

            startActivity(it);

            return true;
        }

        finish();

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {



        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("FILE", filesList.get(sectionNumber));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);

            int pos = getArguments().getInt(ARG_SECTION_NUMBER);

            String fl = getArguments().getString("FILE");

            TouchImageView imageView;

            imageView = (TouchImageView) rootView.findViewById(R.id.imageView);

            try{

                final File file = new File(fl);

                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

                //Log.d("GALLERY", "setBitmap:"+currentFile);

                imageView.setImageBitmap(bmp);

            }catch (Exception e){

                e.printStackTrace();

            }

            return rootView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return filesList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return filesList.get(position);
        }

    }
}
