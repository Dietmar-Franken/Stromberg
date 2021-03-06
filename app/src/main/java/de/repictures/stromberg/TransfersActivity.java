package de.repictures.stromberg;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.repictures.stromberg.Adapters.TransferListAdapter;
import de.repictures.stromberg.AsyncTasks.GetTransfersAsyncTask;
import de.repictures.stromberg.Features.TransferDialogActivity;

public class TransfersActivity extends AppCompatActivity {

    private static final String TAG = "TransferActivity";
    @BindView(R.id.transfer_recycler) RecyclerView transferRecycler;
    @BindView(R.id.transfer_refresh) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.transfer_layout) CoordinatorLayout coordinatorLayout;
    SwipeRefreshLayout.OnRefreshListener refreshListener;
    RecyclerView.Adapter transferAdapter;
    String[][] transfers = new String[0][0];

    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfers);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_scan_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TransfersActivity.this, TransferDialogActivity.class);
                TransfersActivity.this.startActivityForResult(i, 1);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        transferRecycler.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        transferRecycler.setLayoutManager(linearLayoutManager);
        transferAdapter = new TransferListAdapter(TransfersActivity.this, transfers, false);
        transferRecycler.setAdapter(transferAdapter);
        transferRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold) && lastVisibleItem > 0) {
                    isLoading = true;
                    Log.d(TAG, "onScrolled: lastVisibleItem: " + lastVisibleItem);
                    loadMoreItems();
                }
            }
        });

        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sp_identifier), Context.MODE_PRIVATE);
                String savedAccountnumber = sharedPref.getString(getResources().getString(R.string.sp_accountnumber), "");
                GetTransfersAsyncTask asyncTask = new GetTransfersAsyncTask(TransfersActivity.this);
                refreshLayout.setRefreshing(true);
                transfers = new String[0][0];
                transferAdapter.notifyItemRangeRemoved(0, linearLayoutManager.getItemCount());
                asyncTask.execute(savedAccountnumber, String.valueOf(0));
            }
        };
        refreshLayout.setOnRefreshListener(refreshListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
        refreshListener.onRefresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data.getBooleanExtra("success", false)){
            Snackbar.make(coordinatorLayout, getResources().getString(R.string.transfer_success), Snackbar.LENGTH_SHORT).show();
        }
    }

    @UiThread
    public void updateRecycler(String[][] transfers, boolean itemsLeft){
        Log.d(TAG, "updateRecycler: with input: " + transfers);
        this.transfers = concat(this.transfers, transfers);
        transferAdapter = new TransferListAdapter(TransfersActivity.this, this.transfers, itemsLeft);
        transferRecycler.swapAdapter(transferAdapter, false);
        refreshLayout.setRefreshing(false);
        isLoading = !itemsLeft;
    }

    @UiThread
    public void updateRecycler(){
        Log.d(TAG, "updateRecycler: without input");
        transfers = new String[0][0];
        transferAdapter = new TransferListAdapter(TransfersActivity.this, transfers, false);
        transferRecycler.swapAdapter(transferAdapter, false);
        refreshLayout.setRefreshing(false);
        isLoading = false;
    }

    private void loadMoreItems() {
        Log.d(TAG, "loadMoreItems: triggered");
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.sp_identifier), Context.MODE_PRIVATE);
        String savedAccountnumber = sharedPref.getString(getResources().getString(R.string.sp_accountnumber), "");
        GetTransfersAsyncTask asyncTask = new GetTransfersAsyncTask(TransfersActivity.this);
        asyncTask.execute(savedAccountnumber, String.valueOf(totalItemCount - 1));
    }

    private String[][] concat(String[][] A, String[][] B) {
        int aLen = A.length;
        int bLen = B.length;
        int length;
        if (A.length > 0 && B.length > 0) length = A[0].length > B[0].length ? A[0].length : B[0].length;
        else if (B.length > 0 && A.length < 1) length = B[0].length;
        else if (A.length > 0 && B.length < 1) length = A[0].length;
        else length = 0;
        String[][] C= new String[aLen+bLen][length];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }
}
