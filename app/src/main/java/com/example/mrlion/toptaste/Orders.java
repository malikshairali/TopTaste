package com.example.mrlion.toptaste;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.api.BooleanResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Orders extends AppCompatActivity {

    private RecyclerView rvOrdersList;
    private DatabaseReference fb_refLatestOrders,fb_refConfirmedOrders;
    private Button btnLatestOrders, btnConfirmedOrders;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference refOrder;
    public static boolean bHide;

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        loadOrders(fb_refLatestOrders);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(Orders.this,Login.class);
                    startActivity(intent);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                }
            }
        };

        fb_refLatestOrders = FirebaseDatabase.getInstance().getReference().child("Orders_HomeDelivery");
        bHide = false;
        btnLatestOrders = (Button)findViewById(R.id.btnLatestOrders);
        btnConfirmedOrders = (Button)findViewById(R.id.btnConfirmedOrders);

        rvOrdersList = (RecyclerView)findViewById(R.id.rvOrdersList);
        rvOrdersList.setLayoutManager(new LinearLayoutManager(this));
        rvOrdersList.setNestedScrollingEnabled(false);

        btnLatestOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLatestOrders.setBackgroundColor(Color.parseColor("#F9FBE7"));
                btnLatestOrders.setTextColor(Color.BLACK);
                btnConfirmedOrders.setBackgroundColor(Color.parseColor("#004D40"));
                btnConfirmedOrders.setTextColor(Color.WHITE);
                fb_refLatestOrders = FirebaseDatabase.getInstance().getReference().child("Orders_HomeDelivery");
                loadOrders(fb_refLatestOrders);
                bHide = false;
            }
        });
        btnConfirmedOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLatestOrders.setBackgroundColor(Color.parseColor("#004D40"));
                btnLatestOrders.setTextColor(Color.WHITE);
                btnConfirmedOrders.setBackgroundColor(Color.parseColor("#F9FBE7"));
                btnConfirmedOrders.setTextColor(Color.BLACK);
                fb_refConfirmedOrders = FirebaseDatabase.getInstance().getReference().child("ConfirmedOrders_HomeDelivery");
                loadOrders(fb_refConfirmedOrders);
                bHide = true;
            }
        });
        if(!checkSendSMSpermission()){
            ActivityCompat.requestPermissions(Orders.this,
                    new String[]{Manifest.permission.SEND_SMS},1);
        }
    }

    private boolean checkSendSMSpermission(){
        String permission = "android.permission.SEND_SMS";
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Do Nothing
                }else{
                    finish();
                }
            }
        }
    }

    private void loadOrders(DatabaseReference fb_refOrders) {
        FirebaseRecyclerAdapter<Order,Orders.OrdersHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Order, Orders.OrdersHolder>
                (Order.class,R.layout.row_order,Orders.OrdersHolder.class,fb_refOrders) {
            @Override
            protected void populateViewHolder(final OrdersHolder viewHolder, Order model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setPhoneNumber(model.getPhoneNumber());
                viewHolder.setLocation(model.getLocation());
                viewHolder.setItemsOrdered(model.getItemsOrdered());

                refOrder = getRef(position);
                final DatabaseReference refPhoneNumber = refOrder.child("PhoneNumber");
                viewHolder.btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String sDeliveryTime = viewHolder.etDeliveryTime.getText().toString();
                        if(!TextUtils.isEmpty(sDeliveryTime)) {
                            refPhoneNumber.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    moveLatestToConfirmed(refOrder,fb_refConfirmedOrders.push());
//                                    String sPhoneNumber = dataSnapshot.getValue().toString();
//                                    SmsManager smsManager = SmsManager.getDefault();
//                                    smsManager.sendTextMessage(sPhoneNumber, null, "You order is confirmed." +
//                                            " It will reach your destination in about "+sDeliveryTime+" minutes.", null, null);
                                    Toast.makeText(getApplication(), "Message sent!", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Please enter estimated delivery time"
                                    ,Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };

        rvOrdersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class OrdersHolder extends RecyclerView.ViewHolder{

        View mView;
        private Button btnConfirmOrder;
        private EditText etDeliveryTime;
        private LinearLayout llOrders;

        public OrdersHolder(View itemView) {
            super(itemView);

            mView = itemView;
            btnConfirmOrder = (Button) mView.findViewById(R.id.btnConfirmOrder);
            etDeliveryTime = (EditText) mView.findViewById(R.id.etDeliveryTime);
            llOrders = (LinearLayout) mView.findViewById(R.id.llOrders);
            if(bHide){
                llOrders.setVisibility(View.GONE);
            }
            else{
                llOrders.setVisibility(View.VISIBLE);
            }
        }

        public void setName(String name){
            TextView tvName = (TextView)mView.findViewById(R.id.tvName);
            tvName.setText(name);
        }
        public void setPhoneNumber(String phoneNumber){
            TextView tvPhoneNumber = (TextView)mView.findViewById(R.id.tvPhoneNumber);
            tvPhoneNumber.setText(phoneNumber);
        }
        public void setLocation(String location){
            TextView tvLoction = (TextView)mView.findViewById(R.id.tvLocation);
            tvLoction.setText(location);
        }
        public void setItemsOrdered(String itemsOrdered){
            TextView tvItemsOrdered = (TextView)mView.findViewById(R.id.tvItemsOrdered);
            tvItemsOrdered.setText(itemsOrdered);
        }
    }
    public void moveLatestToConfirmed(final DatabaseReference fromPath, final DatabaseReference toPath) {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError != null) {
                            System.out.println("Copy failed");
                        } else {
                            System.out.println("Success");
                            refOrder.setValue(null);
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.signoutmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signOut) {
            mAuth.signOut();
//            Intent intent = new Intent(Orders.this,Login.class);
//            startActivity(intent);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
