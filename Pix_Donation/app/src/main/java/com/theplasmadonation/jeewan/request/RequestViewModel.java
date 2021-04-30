package com.theplasmadonation.jeewan.request;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.theplasmadonation.jeewan.databinding.FragmentDonateBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RequestViewModel extends ViewModel {



    @Override
    protected void onCleared() {
        super.onCleared();
    }

    String name, reqtype, bgroup, amount, date, hospitalname, city, phoneno, description;
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    final  String TAG="Request View Model";


    public RequestViewModel(String name, String req_type, String blood_group, String amount, String date, String hospital_name,
                            String city, String phone_number, String description) {
        this.name = name;
        this.reqtype = req_type;
        this.bgroup = blood_group;
        this.amount = amount;
        this.date = date;
        this.hospitalname = hospital_name;
        this.city = city;
        this.phoneno = phone_number;
        this.description = description;

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    MutableLiveData<Boolean> reqdataPushed = new MutableLiveData<>();
    MutableLiveData<List<RequestModel>> reqDataListFetched = new MutableLiveData<>();


    //method to store request in database
    public MutableLiveData<Boolean> getReqDataPushed() {
        firebaseFirestore.collection("Requests").document((mAuth.getUid())).set(new RequestModel(amount, bgroup, city, date, description,
                hospitalname, name, phoneno, reqtype)).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete()) {
                            reqdataPushed.setValue(true);
                        } else {
                            reqdataPushed.setValue(false);
                        }
                    }
                });

        return reqdataPushed;
    }

    //method to retrieve request list from firstore
    public MutableLiveData<List<RequestModel>> getReqDataList() {
        final List<RequestModel> answer = new ArrayList<>();

        firebaseFirestore.collection("Requests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        Log.d("firedase",documentSnapshot.getId());
                        if(!(documentSnapshot.getId().equals(mAuth.getCurrentUser().getUid()))) {
                            RequestModel requestModel = documentSnapshot.toObject(RequestModel.class);
                            answer.add(requestModel);
                        }
                    }
                    reqDataListFetched.setValue(answer);
                }
            }
        });


        return reqDataListFetched;
    }

    //method to retrieve request list with criteria from firstore
    public MutableLiveData<List<RequestModel>> getReqDataListWithCriteria(String searchcriteria, String searchkeyword) {
        final List<RequestModel> list=new ArrayList<>();

        if ("Search by City".equals(searchcriteria)) {
           firebaseFirestore.collection("Requests").orderBy("city").startAt(searchkeyword.toUpperCase())
                   .endAt(searchkeyword.toUpperCase() + "\uf8ff").get()
                   .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<QuerySnapshot> task) {
                           if(task.isSuccessful()){
                               try {
                                   for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                       if (!(queryDocumentSnapshot.getId().equals(mAuth.getCurrentUser().getUid()))) {
                                           list.add(queryDocumentSnapshot.toObject(RequestModel.class));
                                       }
                                   }
                                   reqDataListFetched.setValue(list);
                               }
                               catch(Exception e){
                                   Log.d(TAG, "onComplete: null " +list);
                               }
                           }
                       }
                   });
        }
        else if ("Search by Blood group".equals(searchcriteria)) {
            firebaseFirestore.collection("Requests").orderBy("blood_group").startAt(searchkeyword.toUpperCase())
                    .endAt(searchkeyword.toUpperCase() + "\uf8ff").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    if (!(queryDocumentSnapshot.getId().equals(mAuth.getCurrentUser().getUid()))) {
                                        list.add(queryDocumentSnapshot.toObject(RequestModel.class));
                                    }
                                }
                                reqDataListFetched.setValue(list);
                            }
                        }
                    });
        }
        return reqDataListFetched;
    }
}
