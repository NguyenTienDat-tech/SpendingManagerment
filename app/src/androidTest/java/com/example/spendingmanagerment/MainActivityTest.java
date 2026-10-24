package com.example.spendingmanagerment;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.spendingmanagerment.ui.home_user.activitymain.MainActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Test public void moreShowsExcelPopupAndKeepsCurrentTab() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.statsFragment)).perform(click());
            onView(withId(R.id.more)).perform(click());
            onView(withText(R.string.app_export_excel)).check(matches(isDisplayed()));
            onView(withText(R.string.app_import_excel)).check(matches(isDisplayed()));
            onView(withText(R.string.app_clear_data)).check(matches(isDisplayed()));
            pressBack();
            onView(withId(R.id.pieChart)).check(matches(isDisplayed()));
            scenario.onActivity(activity -> assertEquals(R.id.statsFragment,
                    ((com.google.android.material.bottomnavigation.BottomNavigationView) activity.findViewById(R.id.bottomNav)).getSelectedItemId()));
        }
    }

    @Test public void clearDataRequiresConfirmationAndCanBeCancelled() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.more)).perform(click());
            onView(withText(R.string.app_clear_data)).perform(click());
            onView(withText(R.string.confirm_clear_data)).check(matches(isDisplayed()));
            onView(withText(R.string.cancel)).perform(click());
            onView(withId(R.id.transactionList)).check(matches(isDisplayed()));
        }
    }

    @Test public void bottomNavigationOpensAllThreeFragments() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.transactionList)).check(matches(isDisplayed()));
            onView(withId(R.id.statsFragment)).perform(click());
            onView(withId(R.id.pieChart)).check(matches(isDisplayed()));
            onView(withId(R.id.settingFragment)).perform(click());
            onView(withId(R.id.switch_dark_mode)).check(matches(isDisplayed()));
            onView(withId(R.id.transactionFragment)).perform(click());
            onView(withId(R.id.floatingActionButton)).check(matches(isDisplayed()));
        }
    }

    @Test public void editorRetainsDraftAcrossActivityRecreation() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.floatingActionButton)).perform(click());
            onView(withId(R.id.amount)).perform(scrollTo(), replaceText("250000"));
            onView(withId(R.id.note)).perform(scrollTo(), replaceText("draft retained"));
            closeSoftKeyboard();
            scenario.recreate();
            onView(withId(R.id.amount)).check(matches(withText("250000")));
            onView(withId(R.id.note)).check(matches(withText("draft retained")));
        }
    }
}
