package lk.jiat.eshop.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.CartItem;
import lk.jiat.eshop.model.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;

    private OnProductClickListener listener;

    public CartAdapter(List<CartItem> cartItems, OnProductClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereEqualTo("productId",cartItem.getProductId()).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot qds) {
                                if (!qds.isEmpty()) {

                                    Product product = qds.getDocuments().get(0).toObject(Product.class);

                                    holder.cartItemTitle.setText(product.getTitle());
                                    holder.cartItemPrice.setText("LKR "+ product.getPrice());
                                    holder.cartItemQuantity.setText(String.valueOf(cartItem.getQuantity()));

                                    Glide.with(holder.itemView.getContext())
                                            .load(product.getImages().get(0))
                                            .centerCrop()
                                            .into(holder.cartItemImage);


//                                    holder.cartItemImage.setOnClickListener(v -> {
//
//                                        Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
//                                        v.startAnimation(animation);
//
//                                        if (listener != null) {
//                                            listener.onProductClick(product);
//                                        }
//                                    });
                                }
                            }
                        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImage;
        TextView cartItemTitle, cartItemPrice, cartItemQuantity;
        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImage = itemView.findViewById(R.id.item_cart_image);
            cartItemTitle = itemView.findViewById(R.id.item_cart_title);
            cartItemPrice = itemView.findViewById(R.id.item_cart_price);
            cartItemQuantity = itemView.findViewById(R.id.item_cart_quantity);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}
