package com.muhaiminurabir.todolist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.muhaiminurabir.todolist.DATABASE_MODEL.TASK;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TODO_LIST_PAGE extends AppCompatActivity {

    FirebaseUser currentUser;
    @BindView(R.id.add_task)
    FloatingActionButton addTask;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.task_list)
    RecyclerView taskList;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    public static String TAG = "TO DO LIST";
    Context context;

    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo__list__page);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        context = TODO_LIST_PAGE.this;
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, currentUser.getEmail());
        get(currentUser.getEmail());
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        taskList.setLayoutManager(linearLayoutManager);
        getTaskList(currentUser.getEmail());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        adapter.startListening();
        //(currentUser);
    }

    @OnClick(R.id.add_task)
    public void onViewClicked() {
        add(currentUser.getEmail());
    }

    public void add(String s) {
        Map<String, Object> user = new HashMap<>();
        user.put("title", "Ada");
        user.put("update", "0");

// Add a new document with a generated ID
        db.collection(s)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void get(String s) {
        /*db.collection(s)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });*/
        db.collection(s)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d("TAG", "New Msg: " + dc.getDocument().toObject(TASK.class));
                                    break;
                                case MODIFIED:
                                    Log.d("TAG", "Modified Msg: " + dc.getDocument().toObject(TASK.class));
                                    break;
                                case REMOVED:
                                    Log.d("TAG", "Removed Msg: " + dc.getDocument().toObject(TASK.class));
                                    break;
                            }
                        }

                    }
                });
    }

    private void getTaskList(final String s) {
        Query query = db.collection(s);

        FirestoreRecyclerOptions<TASK> response = new FirestoreRecyclerOptions.Builder<TASK>().setQuery(query, TASK.class).build();

        adapter = new FirestoreRecyclerAdapter<TASK, TaskHolder>(response) {
            @Override
            public void onBindViewHolder(final TaskHolder holder, final int position, final TASK model) {
                progressBar.setVisibility(View.GONE);
                holder.taskName.setText(model.getTitle());
                if (model.getUpdate().equalsIgnoreCase("0")) {
                    holder.task_status.setChecked(false);
                } else {
                    holder.task_status.setChecked(true);
                }
                holder.task_edit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, getSnapshots().getSnapshot(position).getReference().getId() + "");
                        task_edit_alert(s,getSnapshots().getSnapshot(position).getReference().getId() + "",model);
                    }
                });
                holder.task_status.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, getSnapshots().getSnapshot(position).getReference().getId() + "");
                        if (holder.task_status.isChecked()){
                            UpdateData(s,getSnapshots().getSnapshot(position).getReference().getId() + "",new TASK(model.getTitle(),"1"));
                        }else if (!holder.task_status.isChecked()){
                            UpdateData(s,getSnapshots().getSnapshot(position).getReference().getId() + "",new TASK(model.getTitle(),"0"));
                        }
                    }
                });
            }

            @Override
            public TaskHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.task_row, group, false);

                return new TaskHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        taskList.setAdapter(adapter);
    }

    public class TaskHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.task_title)
        TextView taskName;
        @BindView(R.id.task_status)
        AppCompatCheckBox task_status;
        @BindView(R.id.task_show)
        CardView task_edit;

        public TaskHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void task_edit_alert(final String s,final String collection,final TASK hint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.edit_task_string));
// I'm using fragment here so I'm using getView() to provide ViewGroup
// but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(context).inflate(R.layout.edit_task_layout,null);
// Set up the input
        final EditText input =viewInflated.findViewById(R.id.edit_input);
        input.setText(hint.getTitle());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

// Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //m_Text = input.getText().toString();
                UpdateData(s,collection,new TASK(input.getText().toString(),hint.getUpdate()));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void UpdateData(String s , String c, TASK t) {
        DocumentReference contact = db.collection(s).document(c);
        contact.update("title", t.getTitle());
        contact.update("update", t.getUpdate()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Updated Successfully",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}
