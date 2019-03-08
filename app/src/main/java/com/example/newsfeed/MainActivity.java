package com.example.newsfeed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.newsfeed.Adapter.ListSourceAdapter;
import com.example.newsfeed.Common.Common;
import com.example.newsfeed.Interface.NewsService;
import com.example.newsfeed.Model.WebSite;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mdrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private GoogleSignInClient mGoogleSignInClient;


    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    RecyclerView listWebsite;
    RecyclerView.LayoutManager layoutManager;
    NewsService mService;
    ListSourceAdapter adapter;
    SpotsDialog dialog;
    SwipeRefreshLayout swipeLayout;

    View mHeaderView;
    NavigationView mNavigationView;
    TextView mName,mEmail;
    ImageView mPic;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get data from intent
        String iName,iEmail,iPhoto;
        Intent i=getIntent();
        iName=i.getStringExtra("p_name");
        iEmail=i.getStringExtra("p_email");
        iPhoto=i.getStringExtra("p_photo");

        //initialize drawer
        mdrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        mToggle= new ActionBarDrawerToggle(this,mdrawerLayout,R.string.open,R.string.close);


       // NavigationView
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        // NavigationView Header
        mHeaderView =  mNavigationView.getHeaderView(0);

        // View
       mName = (TextView) mHeaderView.findViewById(R.id.name);
       mEmail= (TextView) mHeaderView.findViewById(R.id.email);
       mPic=(ImageView) mHeaderView.findViewById(R.id.pPic);

       if(iName!=null)
       {
          // Set username & email
           mName.setText(iName);

           Log.d("username", iName);

       }
       if(iEmail!=null)
       {
           mEmail.setText(iEmail);
           Log.d("useremail", iEmail);
       }
        if(iPhoto != null){
           Log.d("photoUrl", iPhoto);
           Picasso.get()
                   .load(iPhoto)
                   .resize(88,60)
                   .centerCrop()
                   .into(mPic);

           //Alternate with Glide
//            Glide.with(getApplicationContext()).load(personPhotoUrl)
//                    .thumbnail(0.5f)
//                    .crossFade()
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(imgProfilePic);
        //set data display in view
        }else
        {
            Log.d("TAG", "Null ");
        }

        mdrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init cache
        Paper.init(this);

        //init service
        mService= Common.getNewsService();

        //init view
        swipeLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadWebsiteSources(true);
            }
        });


        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]


        listWebsite=(RecyclerView)findViewById(R.id.list_sources);
        listWebsite.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        listWebsite.setLayoutManager(layoutManager);
        dialog= new SpotsDialog(this);

        loadWebsiteSources(false);


        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Log.d("Navmenu", "clicked");
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        int id = menuItem.getItemId();

                        switch (id) {
                            case R.id.nav_account:
                                Toast.makeText(getApplicationContext(), "My Account", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.nav_settings:
                                Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.nav_logout: {
                                signOut();
                                return true;
                            }
                        }
                        // close drawer when item is tapped
                        mdrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(false);
                    }
                });
    }

    private void loadWebsiteSources(boolean isRefreshed) {
        if(!isRefreshed){
            String cache=Paper.book().read("cache");

            if(cache!=null && !cache.isEmpty())//if cache is there
            {
                WebSite webSite=new Gson().fromJson(cache,WebSite.class);//convert cache from Json to Object
                // Log.d("CACHE ", cache);
                //Log.d("RESPONSE ", webSite.getSources().get(0).getName().toString());
                adapter=new ListSourceAdapter(getBaseContext(),webSite);
                adapter.notifyDataSetChanged();
                listWebsite.setAdapter(adapter);

            } else//if cache not there
            {
                dialog.show();
                //fetch new data
                mService.getSources().enqueue(new Callback<WebSite>() {
                    @Override
                    public void onResponse(Call<WebSite> call, Response<WebSite> response) {
                        // Log.d("RESPONSE: ", response.body().toString());
                        adapter=new ListSourceAdapter(getBaseContext(),response.body());
                        adapter.notifyDataSetChanged();
                        listWebsite.setAdapter(adapter);
                        //save to cache
                        Paper.book().write("cache",new Gson().toJson(response.body()));

                        dialog.dismiss();

                    }

                    @Override
                    public void onFailure(Call<WebSite> call, Throwable t) {

                    }
                });

            }

        } else //if from swipe to refresh
        {
            swipeLayout.setRefreshing(true);
            //fetch new data
            mService.getSources().enqueue(new Callback<WebSite>() {
                @Override
                public void onResponse(Call<WebSite> call, Response<WebSite> response) {
                    adapter=new ListSourceAdapter(getBaseContext(),response.body());
                    adapter.notifyDataSetChanged();
                    listWebsite.setAdapter(adapter);
                    //save to cache
                    Paper.book().write("cache",new Gson().toJson(response.body()));

                    //Dismiss Refresh progressing
                    swipeLayout.setRefreshing(false);


                }

                @Override
                public void onFailure(Call<WebSite> call, Throwable t) {

                }
            });

        }


    }



    private void updateUI(boolean isLogin){
        if(isLogin)
        {
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent=new Intent(MainActivity.this,Login.class);
            startActivity(intent);
            finish();
            //sign_in.setVisibility(View.VISIBLE);
        }
    }
}
