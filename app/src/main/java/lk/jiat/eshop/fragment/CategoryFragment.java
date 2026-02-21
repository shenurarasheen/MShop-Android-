package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.CategoryAdapter;
import lk.jiat.eshop.data.CategorySamples;
import lk.jiat.eshop.databinding.FragmentCategoryBinding;
import lk.jiat.eshop.model.Category;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(), 3));

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        //CategorySamples.saveCategories(db);


        db.collection("categories").get()
                .addOnCompleteListener(task -> {
                    List<Category> categories = task.getResult().toObjects(Category.class);
                    adapter = new CategoryAdapter(categories, category -> {

                        Bundle bundle = new Bundle();
                        bundle.putString("categoryId", category.getCategoryId());

                        ListingFragment fragment = new ListingFragment();
                        fragment.setArguments(bundle);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.recyclerViewCategories.setAdapter(adapter);
                });
    }
}