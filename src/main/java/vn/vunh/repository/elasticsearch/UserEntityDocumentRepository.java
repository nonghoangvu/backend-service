/*
 * Author: Nong Hoang Vu || JavaTech
 * Facebook:https://facebook.com/NongHoangVu04
 * Github: https://github.com/JavaTech04
 * Youtube: https://www.youtube.com/@javatech04/?sub_confirmation=1
 */
package vn.vunh.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import vn.vunh.model.elasticsearch.UserEntityDocument;

@Repository
public interface UserEntityDocumentRepository extends ElasticsearchRepository<UserEntityDocument, String> {
    UserEntityDocument findUserEntityDocumentByEmail(String email);
}
