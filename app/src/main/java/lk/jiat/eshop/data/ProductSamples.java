package lk.jiat.eshop.data;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.eshop.model.Product;

public class ProductSamples {

    public static void saveProducts(FirebaseFirestore db) {

        List<Product> products = new ArrayList<>();

        products.add(Product.builder()
                .productId("prod1")
                .title("Remote Control Car")
                .description("Battery powered remote control car for kids")
                .price(29.99)
                .categoryId("cat1")
                .images(null)
                .stockCount(12)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod2")
                .title("Plush Teddy Bear")
                .description("Soft plush teddy bear, 30cm height")
                .price(15.5)
                .categoryId("cat1")
                .images(null)
                .stockCount(5)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod3")
                .title("Building Blocks Set")
                .description("100-piece colorful building blocks")
                .price(22.0)
                .categoryId("cat1")
                .images(null)
                .stockCount(0)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod4")
                .title("Decorative Cushion")
                .description("Set of 2 decorative cushions for sofa")
                .price(24.75)
                .categoryId("cat2")
                .images(null)
                .stockCount(20)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod5")
                .title("Wall Clock")
                .description("Modern wall clock with silent movement")
                .price(18.0)
                .categoryId("cat2")
                .images(null)
                .stockCount(7)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod6")
                .title("Garden Rake")
                .description("Lightweight aluminum garden rake")
                .price(12.99)
                .categoryId("cat3")
                .images(null)
                .stockCount(3)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod7")
                .title("Patio Planter")
                .description("Ceramic planter pot for patios and balconies")
                .price(34.5)
                .categoryId("cat3")
                .images(null)
                .stockCount(100)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod8")
                .title("Knife Set")
                .description("5-piece stainless steel kitchen knife set")
                .price(49.99)
                .categoryId("cat4")
                .images(null)
                .stockCount(45)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod9")
                .title("Non-stick Frying Pan")
                .description("24cm non-stick frying pan with heat-resistant handle")
                .price(27.0)
                .categoryId("cat4")
                .images(null)
                .stockCount(6)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod10")
                .title("Bluetooth Speaker")
                .description("Portable Bluetooth speaker with 12h battery life")
                .price(59.99)
                .categoryId("cat5")
                .images(null)
                .stockCount(9)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod11")
                .title("USB-C Power Bank")
                .description("10000mAh power bank with fast charging")
                .price(25.5)
                .categoryId("cat5")
                .images(null)
                .stockCount(11)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod12")
                .title("Car Phone Mount")
                .description("Universal car phone mount for dashboards and vents")
                .price(9.99)
                .categoryId("cat6")
                .images(null)
                .stockCount(2)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod13")
                .title("LED Headlight Bulbs")
                .description("Pair of LED replacement headlight bulbs")
                .price(44.0)
                .categoryId("cat6")
                .images(null)
                .stockCount(15)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod14")
                .title("Gold-plated Necklace")
                .description("Elegant gold-plated necklace with pendant")
                .price(89.99)
                .categoryId("cat7")
                .images(null)
                .stockCount(60)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod15")
                .title("Wrist Watch")
                .description("Water-resistant wrist watch with leather strap")
                .price(120.0)
                .categoryId("cat7")
                .images(null)
                .stockCount(8)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod16")
                .title("Tennis Racket")
                .description("Lightweight graphite tennis racket")
                .price(79.5)
                .categoryId("cat8")
                .images(null)
                .stockCount(1)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod17")
                .title("Football (Soccer Ball)")
                .description("Official size and weight football for training")
                .price(19.99)
                .categoryId("cat8")
                .images(null)
                .stockCount(30)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod18")
                .title("Kitchen Towels")
                .description("Set of 3 absorbent kitchen towels")
                .price(8.25)
                .categoryId("cat4")
                .images(null)
                .stockCount(25)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod19")
                .title("Smart Light Bulb")
                .description("Wi-Fi enabled smart light bulb with color control")
                .price(14.99)
                .categoryId("cat5")
                .images(null)
                .stockCount(4)
                .status(true)
                .build());

        products.add(Product.builder()
                .productId("prod20")
                .title("Garden Gloves")
                .description("Durable gloves for gardening and yard work")
                .price(6.5)
                .categoryId("cat3")
                .images(null)
                .stockCount(50)
                .status(true)
                .build());

        WriteBatch batch = db.batch();

        for (Product p: products) {
            DocumentReference ref = db.collection("products").document();
            batch.set(ref, p);
        }

        batch.commit();

    }
}

