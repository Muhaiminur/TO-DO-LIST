package com.muhaiminurabir.todolist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.muhaiminurabir.todolist.DATABASE_MODEL.TASK;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TODO_LIST_PAGE extends AppCompatActivity implements ActionMode.Callback {
    public static String TAG = "TO DO LIST";
    @BindView(R.id.add_task)
    FloatingActionButton addTask;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.task_list)
    RecyclerView taskList;
    @BindView(R.id.day)
    TextView day;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    Context context;
    LinearLayoutManager linearLayoutManager;
    //i created List of int type to store id of data, you can create custom class type data according to your need.
    List<String> selectedIds = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirestoreRecyclerAdapter adapter;
    private ActionMode actionMode;
    private boolean isMultiSelect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo__list__page);
        //this is for boilerplate
        ButterKnife.bind(this);
        context = TODO_LIST_PAGE.this;
        //getting current day
        Calendar sCalendar = Calendar.getInstance();
        String dayLongName = sCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        day.setText(dayLongName);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, currentUser.getEmail());

        //checking change information real time
        get(currentUser.getEmail());

        //recyclerview work for showing to do list
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        taskList.setLayoutManager(linearLayoutManager);

        //read current to do list
        getTaskList(currentUser.getEmail());

        //multiple select item work
        taskList.addOnItemTouchListener(new RecyclerItemClickListener(this, taskList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect) {
                    //if multiple selection is enabled then select item on single click else perform normal click on item.
                    multiSelect(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    selectedIds = new ArrayList<>();
                    isMultiSelect = true;

                    if (actionMode == null) {
                        actionMode = startActionMode(TODO_LIST_PAGE.this); //show ActionMode.
                    }
                }

                multiSelect(position);
            }
        }));
    }

    //checking inside onstart lifecycle that user exist or not
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        adapter.startListening();
        //(currentUser);
    }

    //add new task
    @OnClick(R.id.add_task)
    public void onViewClicked() {
        //add new to do list
        add_new_alert();
    }

    //doing new to do list
    public void add(String s, String t) {
        Map<String, Object> user = new HashMap<>();
        user.put("title", t);
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

    //geting real time update of data
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

    //getting task list
    private void getTaskList(final String s) {
        //List<String> selectedIds = new ArrayList<>();
        Query query = db.collection(s);

        FirestoreRecyclerOptions<TASK> response = new FirestoreRecyclerOptions.Builder<TASK>().setQuery(query, TASK.class).build();

        adapter = new FirestoreRecyclerAdapter<TASK, TaskHolder>(response) {
            @Override
            public void onBindViewHolder(final TaskHolder holder, final int position, final TASK model) {
                progressBar.setVisibility(View.GONE);

                //setting to do list value
                holder.taskName.setText(model.getTitle());

                //setting to do list done or not
                if (model.getUpdate().equalsIgnoreCase("0")) {
                    holder.task_status.setChecked(false);
                } else {
                    holder.task_status.setChecked(true);
                }

                //appoach for to do list edit
                holder.task_edit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, getSnapshots().getSnapshot(position).getReference().getId() + "");
                        task_edit_alert(s, getSnapshots().getSnapshot(position).getReference().getId() + "", model);
                    }
                });

                //appoach for status change
                holder.task_status.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.d(TAG, getSnapshots().getSnapshot(position).getReference().getId() + "");
                        if (holder.task_status.isChecked()) {
                            UpdateData(s, getSnapshots().getSnapshot(position).getReference().getId() + "", new TASK(model.getTitle(), "1"));
                        } else if (!holder.task_status.isChecked()) {
                            UpdateData(s, getSnapshots().getSnapshot(position).getReference().getId() + "", new TASK(model.getTitle(), "0"));
                        }
                    }
                });

                /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(), "Position is " + position, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });*/

                //color change when bulk edit
                if (selectedIds.contains(model.getTitle())) {
                    holder.itemView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.itemView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
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

        //setting adapter
        adapter.notifyDataSetChanged();
        taskList.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    // to do list new add view
    public void add_new_alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.new_task_string));
// I'm using fragment here so I'm using getView() to provide ViewGroup
// but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(context).inflate(R.layout.edit_task_layout, null);
// Set up the input
        final EditText input = viewInflated.findViewById(R.id.edit_input);
        input.setHint(getString(R.string.new_task_string));
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

// Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                add(currentUser.getEmail(), input.getText().toString());
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

    //to do list edit alert view
    public void task_edit_alert(final String s, final String collection, final TASK hint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.edit_task_string));
// I'm using fragment here so I'm using getView() to provide ViewGroup
// but you can provide here any other instance of ViewGroup from your Fragment / Activity
        View viewInflated = LayoutInflater.from(context).inflate(R.layout.edit_task_layout, null);
// Set up the input
        final EditText input = viewInflated.findViewById(R.id.edit_input);
        input.setText(hint.getTitle());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

// Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //m_Text = input.getText().toString();
                UpdateData(s, collection, new TASK(input.getText().toString(), hint.getUpdate()));
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

    //updating data
    private void UpdateData(String s, String c, TASK t) {
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

    //bulk delete  select
    private void multiSelect(int position) {
        TASK data = (TASK) adapter.getItem(position);
        if (data != null) {
            if (actionMode != null) {
                if (selectedIds.contains(data.getTitle()))
                    selectedIds.remove(data.getTitle());
                else
                    selectedIds.add(data.getTitle());

                if (selectedIds.size() > 0)
                    actionMode.setTitle(String.valueOf(selectedIds.size())); //show selected item count on action mode.
                else {
                    actionMode.setTitle(""); //remove item count from action mode.
                    actionMode.finish(); //hide action mode.
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_multi_select, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete:
                CollectionReference citiesRef = db.collection(currentUser.getEmail());

                for (String DocId : selectedIds) {
                    Query query = citiesRef.whereEqualTo("title", DocId);
                    query.get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot documentSnapshots) {
                                    // ...

                                    // Get the last visible document
                                    DocumentSnapshot lastVisible = documentSnapshots.getDocuments()
                                            .get(0);
                                    Log.d("delete", lastVisible.getId());
                                    db.collection(currentUser.getEmail()).document(lastVisible.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error deleting document", e);
                                                }
                                            });
                                }
                            });


                    //Toast.makeText(this, "Selected items are :"+query.get().getResult().getDocuments().get(0).getId(), Toast.LENGTH_SHORT).show();
                }
                actionMode = null;
                isMultiSelect = false;
                selectedIds = new ArrayList<>();
                selectedIds.clear();
                adapter.notifyDataSetChanged();
                if (mode != null) {
                    mode.finish();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        isMultiSelect = false;
        selectedIds = new ArrayList<>();
        selectedIds.clear();
        adapter.notifyDataSetChanged();
    }

    //creating view
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
}
