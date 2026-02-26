package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.FragmentCheckoutBinding;
import lk.jiat.eshop.model.CartItem;
import lk.jiat.eshop.model.Order;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = auth.getCurrentUser().getUid();

        binding.checkoutProceedBtn.setOnClickListener(v -> {

            db.collection("users")
                    .document(uid)
                    .collection("cart")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot qds) {
                            List<CartItem> cartItems = qds.toObjects(CartItem.class);

                            Order order = new Order();
                            order.setOrderId(String.valueOf(System.currentTimeMillis()));
                            order.setUserId(uid);

                            String shipping_firstName = binding.shippingDetailsFirstName.getText().toString();
                            String shipping_lastName = binding.shippingDetailsLastName.getText().toString();
                            String shipping_email = binding.shippingDetailsEmail.getText().toString();
                            String shipping_mobile = binding.shippingDetailsMobile.getText().toString();
                            String shipping_address1 = binding.shippingDetailsAddressLine1.getText().toString();
                            String shipping_address2 = binding.shippingDetailsAddressLine2.getText().toString();
                            String shipping_city = binding.shippingDetailsCity.getText().toString();
                            String shipping_postCode = binding.shippingDetailsPostalCode.getText().toString();

                            Order.Address shippingAddress = Order.Address.builder()
                                    .firstName(shipping_firstName)
                                    .lastName(shipping_lastName)
                                    .email(shipping_email)
                                    .mobile(shipping_mobile)
                                    .address1(shipping_address1)
                                    .address2(shipping_address2)
                                    .city(shipping_city)
                                    .postCode(shipping_postCode)
                                    .build();

                            order.setShippingAddress(shippingAddress);

                            boolean isBillingSameAsShipping = binding.shippingDetailsBillingAddressCheckout.isChecked();

                            if (!isBillingSameAsShipping) {
                                String billing_firstName = binding.billingDetailsFirstName.getText().toString();
                                String billing_lastName = binding.billingDetailsLastName.getText().toString();
                                String billing_email = binding.billingDetailsEmail.getText().toString();
                                String billing_mobile = binding.billingDetailsMobile.getText().toString();
                                String billing_address1 = binding.billingDetailsAddressLine1.getText().toString();
                                String billing_address2 = binding.billingDetailsAddressLine2.getText().toString();
                                String billing_city = binding.billingDetailsCity.getText().toString();
                                String billing_postCode = binding.billingDetailsPostalCode.getText().toString();

                                Order.Address billingAddress = Order.Address.builder()
                                        .firstName(billing_firstName)
                                        .lastName(billing_lastName)
                                        .email(billing_email)
                                        .mobile(billing_mobile)
                                        .address1(billing_address1)
                                        .address2(billing_address2)
                                        .city(billing_city)
                                        .postCode(billing_postCode)
                                        .build();

                                order.setBillingAddress(billingAddress);
                            }

                            List<Order.OrderItem> orderItems = new ArrayList<>();

                            for (CartItem cartItem: cartItems) {

                                List<Order.OrderItem.Attribute> attributes = new ArrayList<>();


                                for(CartItem.Attribute at: cartItem.getAttributes()){
                                    Order.OrderItem.Attribute attribute = Order.OrderItem.Attribute.builder()
                                            .name(at.getName())
                                            .value(at.getValue())
                                            .build();
                                    attributes.add(attribute);
                                }

                                Order.OrderItem orderItem = Order.OrderItem.builder()
                                        .productId(cartItem.getProductId())
                                        .unitPrice(0)
                                        .quantity(cartItem.getQuantity())
                                        .attributes(attributes)
                                        .build();

                                orderItems.add(orderItem);
                            }


                            order.setOrderItems(orderItems);

                            db.collection("orders").document()
                                    .set(order)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(getContext(), "Oder Saved", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    });
        });

    }
}