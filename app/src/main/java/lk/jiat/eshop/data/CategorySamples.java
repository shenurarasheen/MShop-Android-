package lk.jiat.eshop.data;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import lk.jiat.eshop.model.Category;

public class CategorySamples {

    public static void saveCategories(FirebaseFirestore db) {

        Category c1 = new Category("cat1", "Toys", "");
        Category c2 = new Category("cat2", "Home", "");
        Category c3 = new Category("cat3", "Garden", "");
        Category c4 = new Category("cat4", "Kitchen", "");
        Category c5 = new Category("cat5", "Electronic Items", "");
        Category c6 = new Category("cat6", "Automobile", "");
        Category c7 = new Category("cat7", "Jewellery & Watches", "");
        Category c8 = new Category("cat8", "Sports", "");

        List<Category> cats = List.of(c1, c2, c3, c4, c5, c6, c7, c8);

        WriteBatch batch = db.batch();


        for (Category c: cats) {
            DocumentReference ref = db.collection("categories").document();
            batch.set(ref, c);
        }

        batch.commit();
    }
}
