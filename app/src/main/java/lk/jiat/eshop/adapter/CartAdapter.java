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
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Locale;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.CartItem;
import lk.jiat.eshop.model.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;

    private OnQuantityChangeListener changeListener;
    private OnRemoveListener removeListener;

    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.changeListener = listener;
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.removeListener = listener;
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
        db.collection("products").whereEqualTo("productId", cartItem.getProductId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {

                            int currentPosition = holder.getAbsoluteAdapterPosition();
                            if (currentPosition == RecyclerView.NO_POSITION) {
                                return;
                            }

                            Product product = qds.getDocuments().get(0).toObject(Product.class);

                            holder.cartItemTitle.setText(product.getTitle());
                            holder.cartItemPrice.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice()));
                            holder.cartItemQuantity.setText(String.valueOf(cartItem.getQuantity()));

                            Glide.with(holder.itemView.getContext())
                                    .load(product.getImages().get(0))
                                    .centerCrop()
                                    .into(holder.cartItemImage);

                            holder.btnPlus.setOnClickListener(v -> {
                                if (product.getStockCount() > cartItem.getQuantity()) {
                                    cartItem.setQuantity(cartItem.getQuantity() + 1);
                                    notifyItemChanged(currentPosition);
                                    if (changeListener != null) {
                                        changeListener.onChanged(cartItem);
                                    }
                                }
                            });

                            holder.btnMinus.setOnClickListener(v -> {
                                if (cartItem.getQuantity() > 1) {
                                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                                    notifyItemChanged(currentPosition);
                                    if (changeListener != null) {
                                        changeListener.onChanged(cartItem);
                                    }
                                }
                            });

                            holder.btnRemove.setOnClickListener(v -> {
                                if (removeListener != null) {
                                    removeListener.onRemoved(currentPosition);
                                }
                            });


                        }
                    }
                });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImage, btnRemove;
        TextView cartItemTitle, cartItemPrice, cartItemQuantity;

        AppCompatButton btnPlus, btnMinus;

        @SuppressLint("WrongViewCast")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImage = itemView.findViewById(R.id.item_cart_image);
            cartItemTitle = itemView.findViewById(R.id.item_cart_title);
            cartItemPrice = itemView.findViewById(R.id.item_cart_price);
            cartItemQuantity = itemView.findViewById(R.id.item_cart_quantity);
            btnPlus = itemView.findViewById(R.id.item_cart_plus_btn);
            btnMinus = itemView.findViewById(R.id.item_cart_minus_btn);
            btnRemove = itemView.findViewById(R.id.item_cart_remove);
        }
    }

    public interface OnQuantityChangeListener {
        void onChanged(CartItem cartItem);
    }

    public interface OnRemoveListener {
        void onRemoved(int position);
    }
}
