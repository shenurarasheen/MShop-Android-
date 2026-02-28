package lk.jiat.eshop.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.FragmentCheckoutBinding;
import lk.jiat.eshop.listener.FirestoreCallback;
import lk.jiat.eshop.model.CartItem;
import lk.jiat.eshop.model.Order;
import lk.jiat.eshop.model.Product;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private double total;
    private boolean paymentActive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);

        binding.shippingLayoutBtn.setOnClickListener(v -> {
            if (binding.shippingLayoutBody.getVisibility() == View.GONE) {
                binding.shippingLayoutBody.setVisibility(View.VISIBLE);
                binding.shippingLayoutBtn.setRotation(180f);
            } else {
                binding.shippingLayoutBody.setVisibility(View.GONE);
                binding.shippingLayoutBtn.setRotation(0f);
            }
        });

        binding.billingLayoutBtn.setOnClickListener(v -> {
            if (binding.billingLayoutBody.getVisibility() == View.GONE) {
                binding.billingLayoutBody.setVisibility(View.VISIBLE);
                binding.billingLayoutBtn.setRotation(180f);
            } else {
                binding.billingLayoutBody.setVisibility(View.GONE);
                binding.billingLayoutBtn.setRotation(0f);
            }
        });

        binding.shippingDetailsBillingAddressCheckout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.billingLayout.setVisibility(View.GONE);
            } else {
                binding.billingLayout.setVisibility(View.VISIBLE);
                binding.billingLayoutBody.setVisibility(View.VISIBLE);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        double shippingCost = 400;


        getCartItems(cartItems -> {

            ArrayList<String> productIds = new ArrayList<>();

            cartItems.forEach(cartItem -> {
                productIds.add(cartItem.getProductId());
            });

            getProductsById(productIds, data -> {

                double subTotal = 0;

                for (CartItem cartItem : cartItems) {
                    Product product = data.get(cartItem.getProductId());
                    if (product != null) {
                        subTotal += product.getPrice() * cartItem.getQuantity();
                    }
                }

                total = subTotal + shippingCost;

                binding.checkoutSubtotal.setText(String.format(Locale.US, "LKR %,.2f", subTotal));
                binding.checkoutShipping.setText(String.format(Locale.US, "LKR %,.2f", shippingCost));
                binding.checkoutTotal.setText(String.format(Locale.US, "LKR %,.2f", total));
                paymentActive = true;
            });
        });


        binding.checkoutProceedBtn.setOnClickListener(v -> {

            if (paymentActive) {
                InitRequest req = new InitRequest();
                req.setSandBox(true);

                req.setMerchantId("1225083");
                req.setMerchantSecret("MzA2NDQzNjIwNDE0NjcxOTM5OTU0MTcyMDg3MzQwMzYwMjEwODQzNQ==");
                req.setCurrency("LKR");
                req.setAmount(total);
                req.setOrderId("ESOI-002");
                req.setItemsDescription("Order Description");

                req.getCustomer().setFirstName(binding.shippingDetailsFirstName.getText().toString());
                req.getCustomer().setLastName(binding.shippingDetailsLastName.getText().toString());
                req.getCustomer().setEmail(binding.shippingDetailsEmail.getText().toString());
                req.getCustomer().setPhone(binding.shippingDetailsMobile.getText().toString());
                req.getCustomer().getAddress().setAddress(binding.shippingDetailsAddressLine1.getText().toString() + " " + binding.shippingDetailsAddressLine2.getText().toString());
                req.getCustomer().getAddress().setCity(binding.shippingDetailsCity.getText().toString());
                req.getCustomer().getAddress().setCountry("Sri Lanka");

                req.setNotifyUrl("https://synapse.requestcatcher.com/"); //request to this url to notify if it success or not

                Intent intent = new Intent(getActivity(), PHMainActivity.class);
                intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

                payhereLauncher.launch(intent);

            }

        });

    }


    private void getCartItems(FirestoreCallback<List<CartItem>> callback) {

        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("cart")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {

                            List<CartItem> cartItems = qds.toObjects(CartItem.class);
                            callback.onCallback(cartItems);

                        }
                    }
                });

    }

    private void getProductsById(List<String> productIds, FirestoreCallback<Map<String, Product>> callback) {

        Map<String, Product> products = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            callback.onCallback(products);
            return;
        }

        db.collection("products")
                .whereIn("productId", productIds)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        Map<String, Product> productMap = new HashMap<>();

                        qds.getDocuments().forEach(ds -> {
                            Product product = ds.toObject(Product.class);
                            if (product != null) {
                                products.put(product.getProductId(), product);
                            }
                        });

                        callback.onCallback(products);

                    }
                });
    }

    private final ActivityResultLauncher<Intent> payhereLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == PHMainActivity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        PHResponse<StatusResponse> response =
                                (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                        if (response != null && response.isSuccess()) {

                            StatusResponse statusResponse = response.getData();

                            //save order to firestrore
                            saveOrder(statusResponse);

                            Log.i("PAYHERE", "Payment Success!");

                        } else {
                            Log.e("PAYHERE", response.getData().getMessage());
                        }
                    }
                } else if (result.getResultCode() == PHMainActivity.RESULT_CANCELED) {
                    Log.e("PAYHERE", "Payment Cancelled!");
                }

            });



    private void saveOrder(StatusResponse statusResponse) {
        getCartItems(cartItems -> {

            String uid = auth.getCurrentUser().getUid();

            Order order = new Order();
            order.setOrderId(String.valueOf(System.currentTimeMillis()));
            order.setUserId(uid);
            order.setTotalAmount(total);
            order.setStatus("PAID");
            order.setOrderDate(Timestamp.now().toDate().getTime());
            order.setStatusResponse(statusResponse);

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

            /// ////
            ArrayList<String> productIds = new ArrayList<>();
            cartItems.forEach(cartItem -> {
                productIds.add(cartItem.getProductId());
            });

            List<Order.OrderItem> orderItems = new ArrayList<>();

            getProductsById(productIds, data -> {

                for (CartItem cartItem : cartItems) {
                    Product product = data.get(cartItem.getProductId());

                    if (product != null) {
                        List<Order.OrderItem.Attribute> attributes = new ArrayList<>();

                        for (CartItem.Attribute at : cartItem.getAttributes()) {
                            Order.OrderItem.Attribute attribute = Order.OrderItem.Attribute.builder()
                                    .name(at.getName())
                                    .value(at.getValue())
                                    .build();
                            attributes.add(attribute);
                        }

                        Order.OrderItem orderItem = Order.OrderItem.builder()
                                .productId(cartItem.getProductId())
                                .unitPrice(product.getPrice())
                                .quantity(cartItem.getQuantity())
                                .attributes(attributes)
                                .build();

                        orderItems.add(orderItem);

                        //Add order items to order object
                        order.setOrderItems(orderItems);
                    }

                }
                db.collection("orders").document()
                        .set(order)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getContext(), "Oder Saved", Toast.LENGTH_SHORT).show();

                                db.collection("users").document(uid).collection("cart")
                                        .get()
                                        .addOnSuccessListener(qds -> {
                                            qds.getDocuments().forEach(ds -> {
                                                ds.getReference().delete();
                                            });
                                        });

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new HomeFragment())
                                        .commit();
                            }
                        });

            });


        });
    }
}
