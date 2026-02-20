package lk.jiat.eshop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicUrl;

}
