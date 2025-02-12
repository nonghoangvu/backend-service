/*
 * Author: Nong Hoang Vu || JavaTech
 * Facebook:https://facebook.com/NongHoangVu04
 * Github: https://github.com/JavaTech04
 * Youtube: https://www.youtube.com/@javatech04/?sub_confirmation=1
 */
package vn.vunh.model.elasticsearch;

import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import vn.vunh.common.Gender;

import java.util.Date;

@Getter
@Setter
@Builder
@Document(indexName = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntityDocument {
    @Id
    private String id;

    private String fullName;

    private Gender gender;

    @Temporal(TemporalType.DATE)
    private Date birthday;

    private String username;

    private String email;

    private String phone;
}
