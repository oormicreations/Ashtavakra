package in.oormi.ashtavakragita;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ResourceShow extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from new_activity.xml
        setContentView(R.layout.resources_activity);

        Button mbuttonblog = (Button) findViewById(R.id.buttonBlog);
        mbuttonblog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://pureexperiences.blogspot.com"));
                startActivity(intent);
            }
        });
        Button mbuttonyt = (Button) findViewById(R.id.buttonyt);
        mbuttonyt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.youtube.com/channel/UCh7r8sc97Tzf1mV2TqNZm2A"));
                startActivity(intent);
            }
        });
        Button mbuttonmeet = (Button) findViewById(R.id.buttonmeetup);
        mbuttonmeet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.meetup.com/Pure-Experiences-Online-Satsang/"));
                startActivity(intent);
            }
        });
        Button mbuttonmore = (Button) findViewById(R.id.buttonmore);
        mbuttonmore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("http://pureexperiences.blogspot.com/p/tools.html"));
                startActivity(intent);
            }
        });
    }
}
