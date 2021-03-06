package com.rcorp.app.futurewallet;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import br.com.simplepass.loading_button_lib.interfaces.OnAnimationEndListener;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class AddTransaction extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    public String uid;
    ArrayList<FriendListModel>  friendListModels;
    ListView listView;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    EditText Edtname;
    HashMap<String,String> FriendHashMap;
    FriendListAdapter adapter;
    boolean  istransactionAdded,isOpponentSelected=false;
    EditText Edtamount, Edtreason;
    RadioRealButtonGroup radioRealButtonGroup;
    RadioRealButton HehastoGive, IhavetoGive;
    LinearLayout ll;
    CircularProgressButton addTransaction;
    String TAG="AddTransaction";
    String opponentUid;
    private Snackbar snackbar;
    private InternetStatusReciever internetStatusReciever;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference();
        uid=firebaseAuth.getUid();
        prefs= getSharedPreferences(getResources().getString(R.string.shared_pref_name), MODE_PRIVATE);
        editor=prefs.edit();
        listView=(ListView) findViewById(R.id.FriendListView);
        Edtname=(EditText) findViewById(R.id.Opponent);
        Edtamount=(EditText) findViewById(R.id.Amount);
        Edtreason=(EditText) findViewById(R.id.Reason);
        ll = (LinearLayout) findViewById(R.id.toHide);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_create_transaction);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        radioRealButtonGroup = (RadioRealButtonGroup)findViewById(R.id.Radio);
        HehastoGive=(RadioRealButton) findViewById(R.id.HehastoGive);
        IhavetoGive=(RadioRealButton) findViewById(R.id.IhavetoGive);
        addTransaction=(CircularProgressButton) findViewById(R.id.AddTransaction);
        addTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckInternet.isInternet==false)
                {
                    Toast.makeText(AddTransaction.this, "Internet not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                commitTransaction();
            }
        });
        friendListModels=new ArrayList<>();
        FriendHashMap = new HashMap<>();
        String jsonString =prefs.getString("friendMap", "Not found");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
            Iterator<String> keysItr = jsonObject.keys();
            while(keysItr.hasNext()) {
                String k = keysItr.next();
                String v = null;
                try {
                    v = (String) jsonObject.get(k);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                FriendHashMap.put(k,v);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

       final TextWatcher textwatcher=  new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(istransactionAdded==true)
                {
                    istransactionAdded=false;
                    addTransaction.setText("Add Transaction");
                    addTransaction.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    addTransaction.setBackgroundColor(Color.TRANSPARENT);
                }
                if(isOpponentSelected==true)
                    ll.setVisibility(View.GONE);
                String input =s.toString();
                friendListModels.clear();
                  if(s.toString().length()<=2)
                  {
                      if(adapter!=null)
                      adapter.clear();
                      return;

                  }
                  HashMap<String, String> filteredHashmap=new HashMap<>();
                  filteredHashmap=getFilteredFriendList(input);
                  if(filteredHashmap.size()==0)
                      friendListModels.add(new FriendListModel("Not found","404","Must be in your friend list"));
                  for(HashMap.Entry<String, String> entry: filteredHashmap.entrySet())
                  {
                      String name=entry.getKey();
                      String uid_phone[]=entry.getValue().split("_");
                      String uid=uid_phone[0];
                      String phone=uid_phone[1];
                      friendListModels.add(new FriendListModel(name,uid,phone));

                  }
                adapter= new FriendListAdapter(friendListModels,getApplicationContext());

                listView.setAdapter(adapter);
            }
        };
        final TextWatcher textwatcher2=  new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (istransactionAdded == true) {
                    istransactionAdded = false;
                    addTransaction.setText("Add Transaction");
                    addTransaction.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    addTransaction.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        };
        Edtamount.addTextChangedListener(textwatcher2);
        Edtreason.addTextChangedListener(textwatcher2);
        Edtname.addTextChangedListener(textwatcher);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendListModel friendListModel= friendListModels.get(position);
                if(friendListModel.getuid().equals("404"))
                    return;
                isOpponentSelected=true;
                Edtname.setText(friendListModel.getName());
                opponentUid=friendListModel.getuid();
                listView.setAdapter(null);
                ll.setVisibility(View.VISIBLE);

            }
        });
        snackbar=Snackbar.make(findViewById(android.R.id.content), "You are offline", Snackbar.LENGTH_INDEFINITE);
        internetStatusReciever=new InternetStatusReciever(snackbar);
        registerReceiver(internetStatusReciever, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        mInterstitialAd=new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial1));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }
    public HashMap<String,String> getFilteredFriendList(String s)
    {
        HashMap<String, String> filtered=new HashMap<>();
        for (HashMap.Entry<String, String> entry : FriendHashMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String uid_phone[]=value.split("_");
            if(key.startsWith(s.toUpperCase()) || uid_phone[1].startsWith(s) )
                 filtered.put(key,value);
            // ...
        }
        return filtered;
    }
public void commitTransaction() {
        if(istransactionAdded== true)
        {
            if(mInterstitialAd.isLoaded())
                mInterstitialAd.show();
            onBackPressed();
            return;
        }
        if(Edtname.getText().toString().length()==0)
        {
            Toast.makeText(AddTransaction.this, "Please provide name.", Toast.LENGTH_LONG).show();
            return;
        }
    String amt = Edtamount.getText().toString();
    int amount=0;

    if (amt == null)
    {
        Toast.makeText(AddTransaction.this, "Please provide amount", Toast.LENGTH_LONG).show();
        return;
    }
    else
    {
        try {
          amount=Integer.parseInt(amt);
        }
        catch (NumberFormatException e)
        {
            Toast.makeText(AddTransaction.this, "Amount invalid ", Toast.LENGTH_LONG).show();
            return;
        }
    }
    if((IhavetoGive.isChecked() ||HehastoGive.isChecked())==false)
    {
        Toast.makeText(AddTransaction.this, "Select the option down below", Toast.LENGTH_LONG).show();
        return;
    }
        addTransaction.startAnimation();
    Map<String,Object> transaction = new HashMap<>();
    if(IhavetoGive.isChecked()==true)
    {
        transaction.put("from", uid);
        transaction.put("to", opponentUid);

    }
    else
    {
        transaction.put("from", opponentUid);
        transaction.put("to", uid);

    }
    transaction.put("addedBy", uid);
    transaction.put("amount", amount);
    String reason=Edtreason.getText().toString();
    if(reason.length()==0)
    transaction.put("reason", "No reason");
    else
        transaction.put("reason", reason);
    long timeinMillis=System.currentTimeMillis();
    Log.d(TAG, transaction.toString());
    databaseReference.child("transactions").child(timeinMillis+"").updateChildren(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if(task.isSuccessful()) {
                Toast.makeText(AddTransaction.this, "Transaction added", Toast.LENGTH_SHORT).show();
                addTransaction.revertAnimation(new OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        addTransaction.setBackground(getResources().getDrawable(R.drawable.circular_border_shape));
                        addTransaction.setText("TAP TO CONTINUE");
                        istransactionAdded = true;
                    }
                });
            }
        }
    });
}
private Boolean navigateUp=false;
    @Override
    public boolean onSupportNavigateUp() {
        navigateUp=true;
        if(mInterstitialAd.isLoaded())
            mInterstitialAd.show();
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mInterstitialAd.isLoaded() ==true && navigateUp==false)
            mInterstitialAd.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(internetStatusReciever, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(internetStatusReciever);
    }
}
