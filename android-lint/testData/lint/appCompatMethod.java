package test.pkg;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

@SuppressWarnings("UnusedDeclaration")
public class AppCompatTest extends AppCompatActivity {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void test() {
        <warning descr="Should use `getSupportActionBar` instead of `getActionBar` name">get<caret>ActionBar()</warning>;
    }
}