/*
 * Author: Nong Hoang Vu || JavaTech
 * Facebook:https://facebook.com/NongHoangVu04
 * Github: https://github.com/JavaTech04
 * Youtube: https://www.youtube.com/@javatech04/?sub_confirmation=1
 */
package vn.vunh.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.vunh.common.UserStatus;
import vn.vunh.controller.request.UserCreationRequest;
import vn.vunh.controller.request.UserPasswordRequest;
import vn.vunh.controller.request.UserUpdateRequest;
import vn.vunh.controller.response.UserPageResponse;
import vn.vunh.controller.response.UserResponse;
import vn.vunh.exception.InvalidDataException;
import vn.vunh.exception.ResourceNotFoundException;
import vn.vunh.model.AddressEntity;
import vn.vunh.model.UserEntity;
import vn.vunh.model.elasticsearch.UserEntityDocument;
import vn.vunh.repository.AddressRepository;
import vn.vunh.repository.UserRepository;
import vn.vunh.repository.elasticsearch.UserEntityDocumentRepository;
import vn.vunh.service.EmailService;
import vn.vunh.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final AddressRepository addressRepository;

    private final PasswordEncoder passwordEncoder;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final UserEntityDocumentRepository entityDocumentRepository;

    private final EmailService emailService;

    @Override
    public UserPageResponse findAll(String keyword, String sort, int page, int size) {
        log.info("findAll start");

        // Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // column_nameS:asc|desc
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        int pageNo = 0;
        if (page > 0) {
            pageNo = page - 1;
        }

        // Paging
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        Page<UserEntity> entityPage;

        if (StringUtils.hasLength(keyword)) {
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = userRepository.searchByKeyword(keyword, pageable);
        } else {
            entityPage = userRepository.findAll(pageable);
        }

        return getUserPageResponse(page, size, entityPage);
    }

    @Override
    public UserResponse findById(Long id) {
        log.info("Find user by id: {}", id);

        UserEntity userEntity = getUserEntity(id);

        return UserResponse.builder()
                .id(id)
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .gender(userEntity.getGender())
                .birthday(userEntity.getBirthday())
                .username(userEntity.getUsername())
                .phone(userEntity.getPhone())
                .email(userEntity.getEmail())
                .build();
    }

    @Override
    public UserResponse findByUsername(String username) {
        return null;
    }

    @Override
    public UserResponse findByEmail(String email) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long save(UserCreationRequest req) {
        log.info("Saving user: {}", req);
        UserEntity userByEmail = userRepository.findByEmail(req.getEmail());
        if (userByEmail != null) {
            throw new InvalidDataException("Email already exists");
        }
        UserEntity user = new UserEntity();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setUsername(req.getUsername());
        user.setPassword("DUMMY");
        user.setType(req.getType());
        user.setStatus(UserStatus.NONE);
        UserEntity result = userRepository.save(user);
        log.info("Saved user: {}", user);

        if (result.getId() != null) {
            log.info("user id: {}", result.getId());
            List<AddressEntity> addresses = new ArrayList<>();
            req.getAddresses().forEach(address -> {
                AddressEntity addressEntity = new AddressEntity();
                addressEntity.setApartmentNumber(address.getApartmentNumber());
                addressEntity.setFloor(address.getFloor());
                addressEntity.setBuilding(address.getBuilding());
                addressEntity.setStreetNumber(address.getStreetNumber());
                addressEntity.setStreet(address.getStreet());
                addressEntity.setCity(address.getCity());
                addressEntity.setCountry(address.getCountry());
                addressEntity.setAddressType(address.getAddressType());
                addressEntity.setUserId(result.getId());
                addresses.add(addressEntity);
            });
            addressRepository.saveAll(addresses);
            log.info("Saved addresses: {}", addresses);
        }

        if (result.getId() != null) {
            // save to elasticsearch
            this.entityDocumentRepository.save(UserEntityDocument.builder()
                    .fullName(String.format("%s %s", user.getLastName(), user.getFirstName()))
                    .gender(user.getGender())
                    .birthday(user.getBirthday())
                    .username(user.getUsername())
                    .phone(user.getPhone())
                    .email(user.getEmail())
                    .build());
            log.info("Saved user to elasticsearch successfully");
        }

        sendEmail(user.getEmail(), String.format("%s %s", user.getFirstName(), user.getLastName()));
        return result.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateRequest req) {
        log.info("Updating user: {}", req);

        // Get user by id
        UserEntity user = getUserEntity(req.getId());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setGender(req.getGender());
        user.setBirthday(req.getBirthday());
        user.setPhone(req.getPhone());
        user.setUsername(req.getUsername());

        userRepository.save(user);
        log.info("Updated user: {}", user);

        // save address
        List<AddressEntity> addresses = new ArrayList<>();

        req.getAddresses().forEach(address -> {
            AddressEntity addressEntity = addressRepository.findByUserIdAndAddressType(user.getId(), address.getAddressType());
            if (addressEntity == null) {
                addressEntity = new AddressEntity();
            }
            addressEntity.setApartmentNumber(address.getApartmentNumber());
            addressEntity.setFloor(address.getFloor());
            addressEntity.setBuilding(address.getBuilding());
            addressEntity.setStreetNumber(address.getStreetNumber());
            addressEntity.setStreet(address.getStreet());
            addressEntity.setCity(address.getCity());
            addressEntity.setCountry(address.getCountry());
            addressEntity.setAddressType(address.getAddressType());
            addressEntity.setUserId(user.getId());

            addresses.add(addressEntity);
        });

        // save addresses
        addressRepository.saveAll(addresses);

        // Save to elasticsearch
        if(user.getId() != null) {
            UserEntityDocument userDocument = this.getUserEntityDocumentByEmail(user.getEmail());
            if(userDocument == null) {
                userDocument = new UserEntityDocument();
            }
            userDocument.setFullName(String.format("%s %s", user.getLastName(), user.getFirstName()));
            userDocument.setGender(user.getGender());
            userDocument.setBirthday(user.getBirthday());
            userDocument.setUsername(user.getUsername());
            userDocument.setPhone(user.getPhone());
            userDocument.setEmail(user.getEmail());
            this.entityDocumentRepository.save(userDocument);
            log.info("Updated user to elasticsearch successfully");
        }
        log.info("Updated addresses: {}", addresses);
    }

    @Override
    public void changePassword(UserPasswordRequest req) {
        log.info("Changing password for user: {}", req);

        // Get user by id
        UserEntity user = getUserEntity(req.getId());
        if (req.getPassword().equals(req.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(user);
        log.info("Changed password for user: {}", user);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user: {}", id);

        // Get user by id
        UserEntity user = getUserEntity(id);
        user.setStatus(UserStatus.INACTIVE);

        userRepository.save(user);

        if(user.getId() != null) {
            UserEntityDocument userEntityDocumentByEmail = this.getUserEntityDocumentByEmail(user.getEmail());
            log.info("Username = {} | Email = {} ", userEntityDocumentByEmail.getUsername(), userEntityDocumentByEmail.getEmail());
            this.entityDocumentRepository.delete(userEntityDocumentByEmail);
        }

        log.info("Deleted user id: {}", id);
    }

    @Override
    public List<UserEntityDocument> findAll() {
        List<UserEntityDocument> users = new ArrayList<>();
        this.entityDocumentRepository.findAll().forEach(users::add);
        System.out.println(users);
        return users;
    }

    /**
     * Get user by id
     *
     * @param id
     * @return
     */
    private UserEntity getUserEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserEntityDocument getUserEntityDocumentByEmail(String email) {
        log.info("Finding user by email: {}", email);

        return this.entityDocumentRepository.findUserEntityDocumentByEmail(email);
    }

    /**
     * Convert EserEntities to UserResponse
     *
     * @param page
     * @param size
     * @param userEntities
     * @return
     */
    private static UserPageResponse getUserPageResponse(int page, int size, Page<UserEntity> userEntities) {
        log.info("Convert User Entity Page");

        List<UserResponse> userList = userEntities.stream().map(entity -> UserResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .gender(entity.getGender())
                .birthday(entity.getBirthday())
                .username(entity.getUsername())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .build()
        ).toList();

        UserPageResponse response = new UserPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(userEntities.getTotalElements());
        response.setTotalPages(userEntities.getTotalPages());
        response.setUsers(userList);

        return response;
    }

    private void sendEmail(String to, String name) {
        kafkaTemplate.send("send-email", String.format("to=%s,name=%s", to, name));
        //try {
        //    emailService.sendEmailWithTemplate(to, name);
        //} catch (IOException e) {
        //    log.error(e.getMessage());
        //}
    }
}
