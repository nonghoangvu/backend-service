package vn.vunh.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.vunh.common.Gender;
import vn.vunh.common.UserStatus;
import vn.vunh.common.UserType;
import vn.vunh.controller.request.AddressRequest;
import vn.vunh.controller.request.UserCreationRequest;
import vn.vunh.controller.request.UserPasswordRequest;
import vn.vunh.controller.request.UserUpdateRequest;
import vn.vunh.controller.response.UserPageResponse;
import vn.vunh.controller.response.UserResponse;
import vn.vunh.exception.ResourceNotFoundException;
import vn.vunh.model.UserEntity;
import vn.vunh.repository.AddressRepository;
import vn.vunh.repository.UserRepository;
import vn.vunh.service.impl.UserServiceImpl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit test cho service layer
@ExtendWith(MockitoExtension.class) // Sử dụng Mockito
class UserServiceTest {

    private UserService userService;

    private @Mock UserRepository userRepository;
    private @Mock AddressRepository addressRepository;
    private @Mock PasswordEncoder passwordEncoder;

    private static UserEntity javaTech;
    private static UserEntity johnDoe;

    @BeforeAll
    static void beforeAll() {
        // Dữ liệu giả lập
        javaTech = new UserEntity();
        javaTech.setId(1L);
        javaTech.setFirstName("Java");
        javaTech.setLastName("Tech");
        javaTech.setGender(Gender.MALE);
        javaTech.setBirthday(new Date());
        javaTech.setEmail("nonghoangvu04@gmail.com");
        javaTech.setPhone("0777049085");
        javaTech.setUsername("vunh");
        javaTech.setPassword("password");
        javaTech.setType(UserType.USER);
        javaTech.setStatus(UserStatus.ACTIVE);

        johnDoe = new UserEntity();
        johnDoe.setId(2L);
        johnDoe.setFirstName("John");
        johnDoe.setLastName("Doe");
        johnDoe.setGender(Gender.FEMALE);
        johnDoe.setBirthday(new Date());
        johnDoe.setEmail("johndoe@gmail.com");
        johnDoe.setPhone("0123456789");
        johnDoe.setUsername("johndoe");
        johnDoe.setPassword("password");
        johnDoe.setType(UserType.USER);
        johnDoe.setStatus(UserStatus.INACTIVE);
    }

    @BeforeEach
    void setUp() {
        // Khởi tạo lớp triển khai của UserService
        userService = new UserServiceImpl(userRepository, addressRepository, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testGetUserList_Success() {
        // Giả lập phương thức search của UserRepository
        Page<UserEntity> userPage = new PageImpl<>(List.of(javaTech, johnDoe));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // Gọi phương thức cần kiểm tra
        UserPageResponse result = userService.findAll(null, null, 0, 20);

        Assertions.assertNotNull(result);
        assertEquals(2, result.totalElements);
    }

    @Test
    void testSearchUser_Success() {
        // Giả lập phương thức search của UserRepository
        Page<UserEntity> userPage = new PageImpl<>(List.of(javaTech, johnDoe));
        when(userRepository.searchByKeyword(any(), any(Pageable.class))).thenReturn(userPage);

        // Gọi phương thức cần kiểm tra
        UserPageResponse result = userService.findAll("java", null, 0, 20);

        Assertions.assertNotNull(result);
        assertEquals(2, result.totalElements);
    }

    @Test
    void testGetUserList_Empty() {
        // Giả lập phương thức search của UserRepository
        Page<UserEntity> userPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // Gọi phương thức cần kiểm tra
        UserPageResponse result = userService.findAll(null, null, 0, 20);

        Assertions.assertNotNull(result);
        assertEquals(0, result.getUsers().size());
    }

    @Test
    void testGetUserById_Success() {
        // Giả lập hành vi của UserRepository
        when(userRepository.findById(1L)).thenReturn(Optional.of(javaTech));

        // Gọi phương thức cần kiểm tra
        UserResponse result = userService.findById(1L);

        Assertions.assertNotNull(result);
        assertEquals("vunh", result.getUsername());
    }

    @Test
    void testGetUserById_Failure() {
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> userService.findById(10L));
        assertEquals("User not found", thrown.getMessage());
    }

    @Test
    void testFindByUsername_Success() {
        // Giả lập hành vi của UserRepository
        when(userRepository.findByUsername("vunh")).thenReturn(javaTech);

        // Gọi phương thức cần kiểm tra
        UserResponse result = userService.findByUsername("vunh");
        Assertions.assertNotNull(result);
        assertEquals("vunh", result.getUsername());
    }

    @Test
    void testFindByEmail_Success() {
        // Giả lập hành vi của UserRepository
        when(userRepository.findByEmail("nonghoangvu04@gmail.com")).thenReturn(javaTech);

        // Gọi phương thức cần kiểm tra
        UserResponse result = userService.findByEmail("nonghoangvu04@gmail.com");

        Assertions.assertNotNull(result);
        assertEquals("nonghoangvu04@gmail.com", result.getEmail());
    }

    @Test
    void testSave_Success() {
        // Giả lập hành vi của UserRepository
        when(userRepository.save(any(UserEntity.class))).thenReturn(javaTech);

        UserCreationRequest userCreationRequest = new UserCreationRequest();
        userCreationRequest.setFirstName("Java");
        userCreationRequest.setLastName("Tech");
        userCreationRequest.setGender(Gender.MALE);
        userCreationRequest.setBirthday(new Date());
        userCreationRequest.setEmail("nonghoangvu04@gmail.com");
        userCreationRequest.setPhone("0777049085");
        userCreationRequest.setUsername("javatech");

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setApartmentNumber("ApartmentNumber");
        addressRequest.setFloor("Floor");
        addressRequest.setBuilding("Building");
        addressRequest.setStreetNumber("StreetNumber");
        addressRequest.setStreet("Street");
        addressRequest.setCity("City");
        addressRequest.setCountry("Country");
        addressRequest.setAddressType(1);
        userCreationRequest.setAddresses(List.of(addressRequest));

        // Gọi phương thức cần kiểm tra
        long result = userService.save(userCreationRequest);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(1L, result);
    }

    @Test
    void testUpdate_Success() {
        Long userId = 2L;

        UserEntity updatedUser = new UserEntity();
        updatedUser.setId(userId);
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");
        updatedUser.setGender(Gender.FEMALE);
        updatedUser.setBirthday(new Date());
        updatedUser.setEmail("janesmith@gmail.com");
        updatedUser.setPhone("0123456789");
        updatedUser.setUsername("janesmith");
        updatedUser.setType(UserType.USER);
        updatedUser.setStatus(UserStatus.ACTIVE);

        // Giả lập hành vi của UserRepository
        when(userRepository.findById(userId)).thenReturn(Optional.of(johnDoe));
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setId(userId);
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setGender(Gender.MALE);
        updateRequest.setBirthday(new Date());
        updateRequest.setEmail("janesmith@gmail.com");
        updateRequest.setPhone("0123456789");
        updateRequest.setUsername("janesmith");

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setApartmentNumber("ApartmentNumber");
        addressRequest.setFloor("Floor");
        addressRequest.setBuilding("Building");
        addressRequest.setStreetNumber("StreetNumber");
        addressRequest.setStreet("Street");
        addressRequest.setCity("City");
        addressRequest.setCountry("Country");
        addressRequest.setAddressType(1);
        updateRequest.setAddresses(List.of(addressRequest));

        // Gọi phương thức cần kiểm tra
        userService.update(updateRequest);

        UserResponse result = userService.findById(userId);

        assertEquals("janesmith", result.getUsername());
        assertEquals("janesmith@gmail.com", result.getEmail());
    }

    @Test
    void testChangePassword_Success() {
        Long userId = 2L;

        UserPasswordRequest request = new UserPasswordRequest();
        request.setId(userId);
        request.setPassword("newPassword");
        request.setConfirmPassword("newPassword");

        // Giả lập hành vi của repository và password encoder
        when(userRepository.findById(userId)).thenReturn(Optional.of(johnDoe));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedNewPassword");

        // Gọi phương thức cần kiểm tra
        userService.changePassword(request);

        // Kiểm tra mật khẩu được mã hóa và lưu
        assertEquals("encodedNewPassword", johnDoe.getPassword());
        verify(userRepository, times(1)).save(johnDoe);
        verify(passwordEncoder, times(1)).encode(request.getPassword());
    }

    @Test
    void testDeleteUser_Success() {
        // Chuẩn bị dữ liệu
        Long userId = 1L;

        // Giả lập hành vi repository
        when(userRepository.findById(userId)).thenReturn(Optional.of(javaTech));

        // Gọi phương thức cần kiểm tra
        userService.delete(userId);

        // Kiểm tra kết quả
        assertEquals(UserStatus.INACTIVE, javaTech.getStatus());
        verify(userRepository, times(1)).save(javaTech);
    }

    @Test
    void testUserNotFound_ThrowsException() {
        // Chuẩn bị dữ liệu
        Long userId = 1L;

        // Giả lập hành vi repository
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Gọi phương thức và kiểm tra ngoại lệ
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> userService.delete(userId));

        // Kiểm tra nội dung ngoại lệ
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}