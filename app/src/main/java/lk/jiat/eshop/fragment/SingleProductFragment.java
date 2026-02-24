package lk.jiat.eshop.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.ProductSliderAdapter;
import lk.jiat.eshop.adapter.TopSellingSectionAdapter;
import lk.jiat.eshop.databinding.FragmentSingleProductBinding;
import lk.jiat.eshop.model.Product;

public class SingleProductFragment extends Fragment {

    private FragmentSingleProductBinding binding;
    private String productId;
    private int qty = 1;
    private int avbQty;

    private Map<String, ChipGroup> attributeGroups = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSingleProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        //Load product details

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            Product product = qds.getDocuments().get(0).toObject(Product.class);

                            ProductSliderAdapter adapter = new ProductSliderAdapter(product.getImages());
                            binding.productImageSlider.setAdapter(adapter);

                            binding.dotsIndicator.attachTo(binding.productImageSlider);

                            binding.singleProductTitle.setText(product.getTitle());
                            binding.singleProductRating.setRating(product.getRating());
                            binding.singleProductPrice.setText("LKR " + product.getPrice());
                            binding.productDetailsAvbQty.setText(String.valueOf(product.getStockCount()));

                            avbQty = product.getStockCount();

                            if (product.getAttributes() != null) {
                                product.getAttributes().forEach(attribute -> {
                                    renderAttributes(attribute, binding.singleProductAttributeContainer);
                                });
                            }
                        }
                    }

                });


        //set listeners for increment and decrement qty btns

        binding.incrementQtyBtn.setOnClickListener(v -> {
            if (qty < avbQty) {
                qty ++;
                binding.singleProductQty.setText(String.valueOf(qty));
            }
        });

        binding.decrementQtyBtn.setOnClickListener(v -> {
            if (qty > 1) {
                qty --;
                binding.singleProductQty.setText(String.valueOf(qty));
            }
        });


        loadTopSellProducts();

        binding.singleProductAddToCartBtn.setOnClickListener(v -> {
            getFinalSelections();
        });
    }

    private void loadTopSellProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .whereNotEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            List<Product> products = qds.toObjects(Product.class);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

                            binding.productDetailsTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);

                            TopSellingSectionAdapter adapter = new TopSellingSectionAdapter(products, product -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("productId", product.getProductId());

                                SingleProductFragment fragment = new SingleProductFragment();
                                fragment.setArguments(bundle);

                                // Add the transaction to the back stack so Back returns to the listing
                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            binding.productDetailsTopSellSection.itemSectionTitle.setText("Top Selling Products");
                            binding.productDetailsTopSellSection.itemSectionContainer.setAdapter(adapter);
                        }
                    }
                });
    }


    private void renderAttributes(Product.Attribute attribute, ViewGroup container) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        //Create label
        TextView label = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                100,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        label.setLayoutParams(layoutParams);

        label.setText(attribute.getName());

        row.addView(label);

        //Create Options
        ChipGroup group = new ChipGroup(getContext());

        group.setSelectionRequired(true);
        group.setSingleSelection(true);

        attribute.getValues().forEach(value -> {
            Chip chip = new Chip(getContext());
            chip.setCheckable(true);
            chip.setTag(value);

            if (attribute.getType().equals("color")) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(value)));
            } else {
                chip.setText(value);
            }

            group.addView(chip);
        });

        row.addView(group);

        container.addView(row);

        attributeGroups.put(attribute.getName(), group);

    }

    private void getFinalSelections() {

        StringBuilder result = new StringBuilder("Selected: \n");

        for(Map.Entry<String, ChipGroup> entry: attributeGroups.entrySet()) {
            String attributeName = entry.getKey();
            ChipGroup chipGroup = entry.getValue();

            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != -1) {
                Chip chip = getView().findViewById(checkedChipId);
                String value = chip.getTag().toString();

                result.append(attributeName).append(": ").append(value);

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Restore bottom navigation visibility when this fragment's view is destroyed
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation_view);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }
        // avoid leaking the binding
        binding = null;
    }
}
