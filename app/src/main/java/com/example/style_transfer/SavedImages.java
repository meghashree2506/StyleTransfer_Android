package com.example.style_transfer;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavedImages#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedImages extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SavedImages() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SavedImages.
     */
    // TODO: Rename and change types and number of parameters
    public static SavedImages newInstance(String param1, String param2) {
        SavedImages fragment = new SavedImages();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        View view=getView();
        ArrayList<String> imagelist;
        RecyclerView recyclerView;
        StorageReference root;
        ProgressBar progressBar;
        ImageAdapter adapter;


        imagelist=new ArrayList<>();
        recyclerView=view.findViewById(R.id.recyclerview);
        adapter=new ImageAdapter(imagelist,view.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(null));
        progressBar=view.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
//            recyclerView.setAdapter(adapter);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference listRef = FirebaseStorage.getInstance().getReference().child("images/users/"+user.getUid().toString());
        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference file:listResult.getItems()){
                    file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final boolean add = imagelist.add(uri.toString());
                            Log.d("Itemvalue",uri.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            recyclerView.setAdapter(adapter);
//                                adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        ArrayList<String> imagelist;
        RecyclerView recyclerView;
        StorageReference root;
        ProgressBar progressBar;
        ImageAdapter adapter;


        imagelist=new ArrayList<>();
        recyclerView=view.findViewById(R.id.recyclerview);
        adapter=new ImageAdapter(imagelist,view.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(null));
        progressBar=view.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
//            recyclerView.setAdapter(adapter);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference listRef = FirebaseStorage.getInstance().getReference().child("images/users/"+user.getUid().toString());
        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference file:listResult.getItems()){
                    file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final boolean add = imagelist.add(uri.toString());
                            Log.d("Itemvalue",uri.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            recyclerView.setAdapter(adapter);
//                                adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);


                        }
                    });
                }
            }
        });




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_saved_images, container, false);


    }

}