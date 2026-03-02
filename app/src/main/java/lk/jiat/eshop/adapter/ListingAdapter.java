package lk.jiat.eshop.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.Category;
import lk.jiat.eshop.model.Product;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private List<Product> products;

    private OnProductClickListener listener;
    private FirebaseStorage storage;

    public ListingAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
        storage = FirebaseStorage.getInstance();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("LKR " + product.getPrice());


        storage.getReference("/product-images/" + product.getProductId())
                .listAll()
                .addOnSuccessListener(listResult -> {

                    if (listResult != null && !listResult.getItems().isEmpty()) {
                        listResult.getItems().get(0).getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    Glide.with(holder.itemView.getContext())
                                            .load(uri)
                                            .centerCrop()
                                            .into(holder.productImage);
                                });
                    }
                });


        holder.productImage.setOnClickListener(v -> {

            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
            v.startAnimation(animation);

            if (listener != null) {
                listener.onProductClick(product);
            }
        });

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle, productPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.listing_item_image);
            productTitle = itemView.findViewById(R.id.listing_item_name);
            productPrice = itemView.findViewById(R.id.listing_item_price);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}
