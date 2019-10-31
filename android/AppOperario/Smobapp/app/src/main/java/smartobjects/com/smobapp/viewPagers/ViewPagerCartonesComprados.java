package smartobjects.com.smobapp.viewPagers;

import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Andres Rubiano on 03/02/2016.
 */
public class ViewPagerCartonesComprados extends FragmentPagerAdapter {


    public ViewPagerCartonesComprados(android.support.v4.app.FragmentManager fm) {
        super(fm);
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
//            // Return a FragmentItemsMaster (defined as a static inner class below).
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
//                case 0:
//                    return "SECTION 1";
//                case 1:
//                    return "SECTION 2";
//                case 2:
//                    return "SECTION 3";
        }
        return null;
    }
}

